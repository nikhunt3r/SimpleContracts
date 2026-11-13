package com.simplecontracts.registry;

import com.simplecontracts.SimpleContracts;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(SimpleContracts.MOD_ID);

    public static final DeferredHolder<Item, Item> CONTRACT_BOARD_ITEM =
            ITEMS.register("contract_board",
                    () -> new BlockItem(ModBlocks.CONTRACT_BOARD.get(), new Item.Properties()));

    private ModItems() {
    }
}
