package com.simplecontracts.network;

import com.simplecontracts.SimpleContracts;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record ActiveContractsPayload(List<HudContract> contracts) implements CustomPacketPayload {
    public static final Type<ActiveContractsPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(SimpleContracts.MOD_ID, "active_contracts")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ActiveContractsPayload> STREAM_CODEC =
            StreamCodec.of(ActiveContractsPayload::encode, ActiveContractsPayload::decode);

    private static final int MAX_CONTRACTS = 3;

    public ActiveContractsPayload {
        contracts = List.copyOf(contracts);
        if (contracts.size() > MAX_CONTRACTS) {
            throw new IllegalArgumentException("Too many active contracts in HUD payload");
        }
    }

    private static void encode(RegistryFriendlyByteBuf buffer, ActiveContractsPayload payload) {
        buffer.writeVarInt(payload.contracts().size());
        for (HudContract contract : payload.contracts()) {
            buffer.writeUtf(contract.id(), 64);
            buffer.writeVarInt(contract.progress());
            buffer.writeVarInt(contract.target());
        }
    }

    private static ActiveContractsPayload decode(RegistryFriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        if (size < 0 || size > MAX_CONTRACTS) {
            throw new IllegalArgumentException("Invalid active contract count: " + size);
        }

        List<HudContract> contracts = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            String id = buffer.readUtf(64);
            int progress = buffer.readVarInt();
            int target = buffer.readVarInt();
            contracts.add(new HudContract(id, progress, target));
        }

        return new ActiveContractsPayload(contracts);
    }

    public void handleClient(IPayloadContext context) {
        context.enqueueWork(() -> com.simplecontracts.client.ContractHud.setContracts(contracts()));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record HudContract(String id, int progress, int target) {
    }
}
