package com.simplecontracts.network;

import com.simplecontracts.SimpleContracts;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ContractNetwork {
    private ContractNetwork() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ContractNetwork::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                ActiveContractsPayload.TYPE,
                ActiveContractsPayload.STREAM_CODEC,
                ActiveContractsPayload::handleClient
        );
    }
}
