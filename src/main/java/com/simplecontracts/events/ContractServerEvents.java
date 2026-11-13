package com.simplecontracts.events;

import com.simplecontracts.SimpleContracts;
import com.simplecontracts.data.ContractData;
import com.simplecontracts.network.ContractNetworkSync;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = SimpleContracts.MOD_ID)
public final class ContractServerEvents {
    private ContractServerEvents() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        long overworldDay = Math.floorDiv(server.overworld().getDayTime(), 24_000L);

        ContractData data = ContractData.get(server);
        data.refreshOnlinePlayers(overworldDay, server.getPlayerList().getPlayers());

        // Send the current active list when a player opens the world/session.
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.tickCount % 40 == 0) {
                ContractNetworkSync.sync(player, data);
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

        if (contractId == null) {
            return;
        }

        ContractData data = ContractData.get(player.getServer());
        if (data.advanceProgress(player.getUUID(), contractId, 1)) {
            ContractNetworkSync.sync(player, data);
            if (player.containerMenu instanceof com.simplecontracts.menu.ContractBoardMenu menu) {
                menu.refreshFromServer();
            }
        }
    }
}
