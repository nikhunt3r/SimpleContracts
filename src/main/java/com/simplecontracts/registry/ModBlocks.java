package com.simplecontracts.registry;

import com.simplecontracts.SimpleContracts;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, SimpleContracts.MOD_ID);

    public static final DeferredHolder<Block, Block> CONTRACT_BOARD =
            BLOCKS.register("contract_board",
                    () -> new Block(BlockBehaviour.Properties.of().strength(2.5F)));

    private ModBlocks() {
    }
}
