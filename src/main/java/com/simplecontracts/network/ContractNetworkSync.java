package com.simplecontracts.network;

import com.simplecontracts.data.ContractData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public final class ContractNetworkSync {
    private ContractNetworkSync() {
    }

    public static void sync(ServerPlayer player, ContractData data) {
        List<ActiveContractsPayload.HudContract> active = data.getActiveContracts(player.getUUID())
                .stream()
                .map(contract -> new ActiveContractsPayload.HudContract(
                        contract.id(),
                        contract.progress(),
                        contract.target()
                ))
                .toList();

        PacketDistributor.sendToPlayer(player, new ActiveContractsPayload(active));
    }
}
