package com.simplecontracts.menu;

import com.simplecontracts.config.SimpleContractsConfig;
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
    private static final int META_OFFSET = CONTRACT_COUNT * VALUES_PER_CONTRACT;
    private static final int DATA_COUNT = META_OFFSET + 4;

    private final ContainerLevelAccess access;
    private final ContainerData data;
    private final ContractData serverData;
    private final UUID playerId;

    public ContractBoardMenu(int id, Inventory inventory) {
        this(id, inventory, ContainerLevelAccess.NULL, new SimpleContainerData(DATA_COUNT), null, null);
    }

    public ContractBoardMenu(int id, Inventory inventory, ContainerLevelAccess access,
                             ContractData serverData, UUID playerId,
                             List<ContractData.ContractEntry> contracts) {
        this(id, inventory, access, createData(contracts), serverData, playerId);
    }

    private ContractBoardMenu(int id, Inventory inventory, ContainerLevelAccess access,
                              ContainerData data, ContractData serverData, UUID playerId) {
        super(ModMenus.CONTRACT_BOARD.get(), id);
        if (data.getCount() != DATA_COUNT) throw new IllegalArgumentException("Invalid contract data size");
        this.access = access;
        this.data = data;
        this.serverData = serverData;
        this.playerId = playerId;
        if (serverData != null && playerId != null) {
            writeData(serverData.getContracts(playerId, 1), 1);
        }
        addDataSlots(data);
    }

    public int getContractType(int slot) { return data.get(index(slot, 0)); }
    public int getContractTarget(int slot) { return data.get(index(slot, 1)); }
    public int getContractProgress(int slot) { return data.get(index(slot, 2)); }
    public long getContractReward(int slot) {
        int offset = index(slot, 3);
        return Integer.toUnsignedLong(data.get(offset)) | ((long)data.get(offset + 1) << 32);
    }
    public boolean isContractClaimed(int slot) { return data.get(index(slot, 5)) != 0; }
    public boolean isContractAccepted(int slot) { return data.get(index(slot, 6)) != 0; }
    public boolean isContractCompleted(int slot) { return getContractProgress(slot) >= getContractTarget(slot); }
    public int getSelectedLevel() { return data.get(META_OFFSET); }
    public int getReputation() { return data.get(META_OFFSET + 1); }
    public int getPlayerLevel() { return data.get(META_OFFSET + 2); }
    public int getActiveCount() { return data.get(META_OFFSET + 3); }
    public int getMaxActiveContracts() { return SimpleContractsConfig.getMaxActiveContracts(); }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (serverData == null || playerId == null || !(player instanceof ServerPlayer serverPlayer)
                || !stillValid(player)) return false;

        int level = getSelectedLevel();
        if (buttonId == 100 || buttonId == 101) {
            int next = level + (buttonId == 100 ? -1 : 1);
            if (next < 1 || next > serverData.getPlayerLevel(playerId)) return false;
            writeData(serverData.getContracts(playerId, next), next);
            return true;
        }

        boolean changed;
        if (buttonId >= 0 && buttonId < CONTRACT_COUNT) {
            changed = serverData.acceptContract(playerId, level, buttonId);
        } else if (buttonId >= 10 && buttonId < 10 + CONTRACT_COUNT) {
            changed = serverData.claimReward(serverPlayer, level, buttonId - 10);
        } else {
            return false;
        }

        if (!changed) return false;
        writeData(serverData.getContracts(playerId, level), level);
        ContractNetworkSync.sync(serverPlayer, serverData);
        return true;
    }

    public void refreshFromServer() {
        if (serverData != null && playerId != null) {
            int level = Math.min(getSelectedLevel(), serverData.getPlayerLevel(playerId));
            level = Math.max(1, level);
            writeData(serverData.getContracts(playerId, level), level);
        }
    }

    private void writeData(List<ContractData.ContractEntry> contracts, int level) {
        for (int slot = 0; slot < CONTRACT_COUNT; slot++) {
            int offset = index(slot, 0);
            if (slot >= contracts.size()) {
                for (int i = 0; i < VALUES_PER_CONTRACT; i++) data.set(offset + i, -1);
                continue;
            }
            ContractData.ContractEntry c = contracts.get(slot);
            data.set(offset, contractTypeIndex(c.id()));
            data.set(offset + 1, c.target());
            data.set(offset + 2, c.progress());
            data.set(offset + 3, (int)c.reward());
            data.set(offset + 4, (int)(c.reward() >>> 32));
            data.set(offset + 5, c.claimed() ? 1 : 0);
            data.set(offset + 6, c.accepted() ? 1 : 0);
        }
        data.set(META_OFFSET, level);
        data.set(META_OFFSET + 1, serverData == null || playerId == null ? 0 : serverData.getReputation(playerId));
        data.set(META_OFFSET + 2, serverData == null || playerId == null ? 1 : serverData.getPlayerLevel(playerId));
        data.set(META_OFFSET + 3, serverData == null || playerId == null ? 0 : serverData.getActiveContracts(playerId).size());
    }

    private static ContainerData createData(List<ContractData.ContractEntry> contracts) {
        SimpleContainerData data = new SimpleContainerData(DATA_COUNT);
        for (int slot = 0; slot < CONTRACT_COUNT; slot++) {
            int offset = index(slot, 0);
            if (slot >= contracts.size()) {
                for (int i = 0; i < VALUES_PER_CONTRACT; i++) data.set(offset + i, -1);
                continue;
            }
            ContractData.ContractEntry c = contracts.get(slot);
            data.set(offset, contractTypeIndex(c.id()));
            data.set(offset + 1, c.target());
            data.set(offset + 2, c.progress());
            data.set(offset + 3, (int)c.reward());
            data.set(offset + 4, (int)(c.reward() >>> 32));
            data.set(offset + 5, c.claimed() ? 1 : 0);
            data.set(offset + 6, c.accepted() ? 1 : 0);
        }
        data.set(META_OFFSET, 1);
        return data;
    }

    private static int index(int slot, int field) {
        if (slot < 0 || slot >= CONTRACT_COUNT) throw new IndexOutOfBoundsException("Contract slot: " + slot);
        return slot * VALUES_PER_CONTRACT + field;
    }

    private static int contractTypeIndex(String id) {
        return switch (id) {
            case "zombie_hunter" -> 0;
            case "skeleton_hunter" -> 1;
            case "iron_miner" -> 2;
            case "wheat_farmer" -> 3;
            case "fisher" -> 4;
            case "explorer" -> 5;
            case "spider_hunter" -> 6;
            case "woodcutter" -> 7;
            case "stone_miner" -> 8;
            case "creeper_hunter" -> 9;
            case "drowned_hunter" -> 10;
            case "gold_miner" -> 11;
            case "root_farmer" -> 12;
            case "enderman_hunter" -> 13;
            case "diamond_miner" -> 14;
            case "redstone_miner" -> 15;
            case "slime_hunter" -> 16;
            case "blaze_hunter" -> 17;
            case "witch_hunter" -> 18;
            case "emerald_miner" -> 19;
            case "obsidian_miner" -> 20;
            case "wither_skeleton_hunter" -> 21;
            case "guardian_hunter" -> 22;
            case "ancient_debris_miner" -> 23;
            case "warden_hunter" -> 24;
            default -> -1;
        };
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(access, player, ModBlocks.CONTRACT_BOARD.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) { return ItemStack.EMPTY; }
}
