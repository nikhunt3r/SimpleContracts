package com.simplecontracts.client;

import com.simplecontracts.network.ActiveContractsPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class ContractHud {
    private static List<ActiveContractsPayload.HudContract> contracts = List.of();

    private ContractHud() {
    }

    public static void setContracts(List<ActiveContractsPayload.HudContract> updated) {
        contracts = List.copyOf(updated);
    }

    public static void render(GuiGraphics graphics) {
        if (contracts.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui || minecraft.player == null) {
            return;
        }

        int x = 8;
        int y = 8;
        int width = 150;
        int availableHeight = Math.max(14, minecraft.getWindow().getGuiScaledHeight() - y - 14);
        int rowHeight = Math.max(14, Math.min(28, availableHeight / contracts.size()));
        int height = 8 + contracts.size() * rowHeight;

        graphics.fill(x, y, x + width, y + height, 0xA0000000);
        graphics.fill(x, y, x + width, y + 1, 0xFF555555);

        graphics.drawString(
                minecraft.font,
                Component.translatable("hud.simplecontracts.active_contracts"),
                x + 6,
                y + 5,
                0xFFFFFF,
                false
        );

        int rowY = y + 17;
        for (ActiveContractsPayload.HudContract contract : contracts) {
            Component name = Component.translatable(
                    "contract.simplecontracts." + contract.id() + ".title"
            );
            graphics.drawString(minecraft.font, name, x + 6, rowY, 0xFFE6B3, false);

            String progress = contract.progress() + "/" + contract.target();
            graphics.drawString(minecraft.font, progress, x + width - 6 - minecraft.font.width(progress),
                    rowY, 0xFFFFFF, false);

            int barX = x + 6;
            int barY = rowY + Math.max(9, rowHeight - 8);
            int barWidth = width - 12;
            graphics.fill(barX, barY, barX + barWidth, barY + 3, 0xFF444444);
            int filled = contract.target() <= 0 ? 0
                    : barWidth * Math.min(contract.progress(), contract.target()) / contract.target();
            graphics.fill(barX, barY, barX + filled, barY + 3, 0xFF55AA55);
            rowY += rowHeight;
        }
    }
}
