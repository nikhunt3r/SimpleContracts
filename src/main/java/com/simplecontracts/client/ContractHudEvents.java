package com.simplecontracts.client;

import com.simplecontracts.SimpleContracts;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = SimpleContracts.MOD_ID, value = Dist.CLIENT)
public final class ContractHudEvents {
    private ContractHudEvents() {
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        ContractHud.render(event.getGuiGraphics());
    }
}
