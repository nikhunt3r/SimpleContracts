package com.simplecontracts.events;

import com.simplecontracts.SimpleContracts;
import com.simplecontracts.data.ContractData;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = SimpleContracts.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class ContractServerEvents {
    private ContractServerEvents() {
    }

    /**
     * The refresh boundary is shared by the whole server: the overworld day.
     * Every player's task list is generated independently at that boundary.
     */
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        long overworldDay = Math.floorDiv(server.overworld().getDayTime(), 24_000L);

        ContractData.get(server).refreshOnlinePlayers(
                overworldDay,
                server.getPlayerList().getPlayers()
        );
    }
}
