package com.simplecontracts.events;

import com.simplecontracts.SimpleContracts;
import com.simplecontracts.data.ContractData;
import com.simplecontracts.menu.ContractBoardMenu;
import com.simplecontracts.network.ContractNetworkSync;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.List;

@EventBusSubscriber(modid = SimpleContracts.MOD_ID)
public final class ContractCommands {
    private ContractCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("contracts")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("refresh")
                                .executes(context -> {
                                    var source = context.getSource();
                                    MinecraftServer server = source.getServer();
                                    List<ServerPlayer> onlinePlayers = server.getPlayerList().getPlayers();
                                    long overworldDay = Math.floorDiv(
                                            server.overworld().getDayTime(),
                                            24_000L
                                    );

                                    ContractData data = ContractData.get(server);
                                    int refreshedPlayers = data.forceRefresh(overworldDay, onlinePlayers);

                                    for (ServerPlayer player : onlinePlayers) {
                                        ContractNetworkSync.sync(player, data);
                                        if (player.containerMenu instanceof ContractBoardMenu menu) {
                                            menu.refreshFromServer();
                                        }
                                    }

                                    source.sendSuccess(
                                            () -> Component.translatable(
                                                    "commands.simplecontracts.refresh.success",
                                                    refreshedPlayers
                                            ),
                                            false
                                    );
                                    return 1;
                                })
                        )
        );
    }
}
