package com.simplecontracts.menu;

import com.simplecontracts.data.ContractData;
import com.simplecontracts.network.ContractNetworkSync;
import com.simplecontracts.registry.ModBlocks;
import com.simplecontracts.registry.ModMenus;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.UUID;

public final class ContractBoardMenu extends AbstractContainerMenu {
    public static final int CONTRACT_COUNT = 3;
    private static final int VALUES_PER_CONTRACT = 7;
    private static final int DATA_COUNT = CONTRACT_COUNT * VALUES_PER_CONTRACT;

    private final ContainerLevelAccess access;
    private final ContainerData contractData;
    private final ContractData serverData;
    private final UUID playerId;

    public ContractBoardMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL,
                new SimpleContainerData(DATA_COUNT), null, null);
    }

    public ContractBoardMenu(
            int containerId,
            Inventory playerInventory,
            ContainerLevelAccess access,
            ContractData serverData,
            UUID playerId,
            List<ContractData.ContractEntry> contracts
    ) {
        this(containerId, playerInventory, access, createData(contracts), serverData, playerId);
    }

    private ContractBoardMenu(
            int containerId,
            Inventory playerInventory,
            ContainerLevelAccess access,
            ContainerData contractData,
            ContractData serverData,
            UUID playerId
    ) {
        super(ModMenus.CONTRACT_BOARD.get(), containerId);
        if (contractData.getCount() != DATA_COUNT) {
            throw new IllegalArgumentException("Contract board requires " + DATA_COUNT + " data values");
        }
        this.access = access;
        this.contractData = contractData;
        this.serverData = serverData;
        this.playerId = playerId;
        this.addDataSlots(contractData);
    }

    public int getContractType(int slot) { return contractData.get(index(slot, 0)); }
    public int getContractTarget(int slot) { return contractData.get(index(slot, 1)); }
    public int getContractProgress(int slot) { return contractData.get(index(slot, 2)); }

    public long getContractReward(int slot) {
        int offset = index(slot, 3);
        return Integer.toUnsignedLong(contractData.get(offset))
                | ((long) contractData.get(offset + 1) << 32);
    }

    public boolean isContractClaimed(int slot) { return contractData.get(index(slot, 5)) != 0; }
    public boolean isContractAccepted(int slot) { return contractData.get(index(slot, 6)) != 0; }
    public boolean isContractCompleted(int slot) { return getContractProgress(slot) >= getContractTarget(slot); }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (serverData == null || playerId == null || !(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        if (!stillValid(player)) {
            return false;
        }

        boolean changed;
        if (buttonId >= 0 && buttonId < CONTRACT_COUNT) {
            changed = serverData.acceptContract(playerId, buttonId);
        } else if (buttonId >= 10 && buttonId < 10 + CONTRACT_COUNT) {
            changed = serverData.claimReward(serverPlayer, buttonId - 10);
        } else {
            return false;
        }

        if (!changed) {
            return false;
        }

        writeData(serverData.getContracts(playerId));
        ContractNetworkSync.sync(serverPlayer, serverData);
        return true;
    }

    public void refreshFromServer() {
        if (serverData != null && playerId != null) {
            writeData(serverData.getContracts(playerId));
        }
    }

    private void writeData(List<ContractData.ContractEntry> contracts) {
        for (int slot = 0; slot < CONTRACT_COUNT; slot++) {
            int offset = index(slot, 0);
            if (slot >= contracts.size()) {
                for (int i = 0; i < VALUES_PER_CONTRACT; i++) contractData.set(offset + i, 0);
                continue;
            }
            ContractData.ContractEntry contract = contracts.get(slot);
            contractData.set(offset, contractTypeIndex(contract.id()));
            contractData.set(offset + 1, contract.target());
            contractData.set(offset + 2, contract.progress());
            long reward = Math.max(0L, contract.reward());
            contractData.set(offset + 3, (int) reward);
            contractData.set(offset + 4, (int) (reward >>> 32));
            contractData.set(offset + 5, contract.claimed() ? 1 : 0);
            contractData.set(offset + 6, contract.accepted() ? 1 : 0);
        }
    }

    private static int index(int slot, int valueOffset) {
        if (slot < 0 || slot >= CONTRACT_COUNT) throw new IndexOutOfBoundsException("Contract slot: " + slot);
        return slot * VALUES_PER_CONTRACT + valueOffset;
    }

    private static ContainerData createData(List<ContractData.ContractEntry> contracts) {
        SimpleContainerData data = new SimpleContainerData(DATA_COUNT);
        for (int slot = 0; slot < Math.min(CONTRACT_COUNT, contracts.size()); slot++) {
            ContractData.ContractEntry contract = contracts.get(slot);
            int offset = index(slot, 0);
            data.set(offset, contractTypeIndex(contract.id()));
            data.set(offset + 1, contract.target());
            data.set(offset + 2, contract.progress());
            long reward = Math.max(0L, contract.reward());
            data.set(offset + 3, (int) reward);
            data.set(offset + 4, (int) (reward >>> 32));
            data.set(offset + 5, contract.claimed() ? 1 : 0);
            data.set(offset + 6, contract.accepted() ? 1 : 0);
        }
        return data;
    }

    private static int contractTypeIndex(String id) {
        return switch (id) {
            case "zombie_hunter" -> 0;
            case "skeleton_hunter" -> 1;
            case "iron_miner" -> 2;
            case "wheat_farmer" -> 3;
            case "fisher" -> 4;
            case "explorer" -> 5;
            default -> -1;
        };
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, ModBlocks.CONTRACT_BOARD.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        return ItemStack.EMPTY;
    }
}
