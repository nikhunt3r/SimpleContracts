package com.simplecontracts.events;

import com.simplecontracts.SimpleContracts;
import com.simplecontracts.data.ContractData;
import com.simplecontracts.menu.ContractBoardMenu;
import com.simplecontracts.network.ContractNetworkSync;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.tags.BlockTags;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = SimpleContracts.MOD_ID)
public final class ContractServerEvents {
    private ContractServerEvents() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        long overworldDay = Math.floorDiv(server.overworld().getDayTime(), 24_000L);
        ContractData.get(server).refreshOnlinePlayers(overworldDay, server.getPlayerList().getPlayers());
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ContractNetworkSync.sync(player, ContractData.get(player.getServer()));
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.tickCount % 20 == 0) {
            advanceExploration(player);
        }
    }

    private static void advanceExploration(ServerPlayer player) {
        ContractData data = ContractData.get(player.getServer());
        if (data.recordExploration(player)) {
            ContractNetworkSync.sync(player, data);
            if (player.containerMenu instanceof ContractBoardMenu menu) {
                menu.refreshFromServer();
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }

        LivingEntity victim = event.getEntity();
        String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType()).toString();
        String contractId = switch (entityId) {
            case "minecraft:zombie" -> "zombie_hunter";
            case "minecraft:skeleton" -> "skeleton_hunter";
            default -> null;
        };

        if (contractId != null) {
            advance(player, contractId, 1);
        }
    }

    @SubscribeEvent
    public static void onBlockBroken(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }

        if (event.getState().is(BlockTags.IRON_ORES)) {
            advance(player, "iron_miner", 1);
            return;
        }

        if (event.getState().is(Blocks.WHEAT)
                && event.getState().getValue(CropBlock.AGE) == CropBlock.MAX_AGE) {
            advance(player, "wheat_farmer", 1);
        }
    }

    @SubscribeEvent
    public static void onItemFished(ItemFishedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        boolean caughtFish = event.getDrops().stream().anyMatch(ContractServerEvents::isFish);
        if (caughtFish) {
            advance(player, "fisher", 1);
        }
    }

    private static boolean isFish(ItemStack stack) {
        return stack.is(Items.COD)
                || stack.is(Items.SALMON)
                || stack.is(Items.TROPICAL_FISH)
                || stack.is(Items.PUFFERFISH);
    }

    private static void advance(ServerPlayer player, String contractId, int amount) {
        ContractData data = ContractData.get(player.getServer());
        if (!data.advanceProgress(player.getUUID(), contractId, amount)) {
            return;
        }

        ContractNetworkSync.sync(player, data);
        if (player.containerMenu instanceof ContractBoardMenu menu) {
            menu.refreshFromServer();
        }
    }
}
