package com.simplecontracts.menu;

import com.simplecontracts.registry.ModBlocks;
import com.simplecontracts.registry.ModMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;

public final class ContractBoardMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;

    // Client-side constructor. The server supplies the real block access.
    public ContractBoardMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    // Server-side constructor.
    public ContractBoardMenu(
            int containerId,
            Inventory playerInventory,
            ContainerLevelAccess access
    ) {
        super(ModMenus.CONTRACT_BOARD.get(), containerId);
        this.access = access;
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(
                this.access,
                player,
                ModBlocks.CONTRACT_BOARD.get()
        );
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        return ItemStack.EMPTY;
    }
}
