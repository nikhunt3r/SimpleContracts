package com.simplecontracts.block;

import com.simplecontracts.data.ContractData;
import com.simplecontracts.menu.ContractBoardMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class ContractBoardBlock extends Block {
    public ContractBoardBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider(
                (containerId, playerInventory, player) -> {
                    if (!(player instanceof ServerPlayer serverPlayer)) {
                        throw new IllegalStateException("Contract board menu must be created on the server");
                    }

                    long refreshDay = Math.floorDiv(level.getDayTime(), 24_000L);
                    return new ContractBoardMenu(
                            containerId,
                            playerInventory,
                            ContainerLevelAccess.create(level, pos),
                            ContractData.get(serverPlayer.getServer())
                                    .getOrCreateContracts(serverPlayer.getUUID(), refreshDay)
                    );
                },
                Component.translatable("screen.simplecontracts.contract_board")
        );
    }

    @Override
    public InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hit
    ) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(getMenuProvider(state, level, pos));
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
