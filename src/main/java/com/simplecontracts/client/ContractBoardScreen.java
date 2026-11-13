package com.simplecontracts.client;

import com.simplecontracts.menu.ContractBoardMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class ContractBoardScreen extends AbstractContainerScreen<ContractBoardMenu> {
    public ContractBoardScreen(
            ContractBoardMenu menu,
            Inventory playerInventory,
            Component title
    ) {
        super(menu, playerInventory, title);

        this.imageWidth = 240;
        this.imageHeight = 166;
        this.titleLabelX = 10;
        this.titleLabelY = 9;
    }

    @Override
    protected void init() {
        super.init();

        this.addRenderableWidget(
                Button.builder(
                                Component.translatable("gui.done"),
                                button -> this.onClose()
                        )
                        .bounds(
                                this.leftPos + this.imageWidth - 76,
                                this.topPos + this.imageHeight - 27,
                                66,
                                20
                        )
                        .build()
        );
    }

    @Override
    protected void renderBg(
            GuiGraphics graphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {
        int x = this.leftPos;
        int y = this.topPos;
        int right = x + this.imageWidth;
        int bottom = y + this.imageHeight;

        // Crisp vanilla-style beveled panel. No blur and no scaled textures.
        graphics.fill(x, y, right, bottom, 0xFF373737);
        graphics.fill(x + 1, y + 1, right - 1, bottom - 1, 0xFFFFFFFF);
        graphics.fill(x + 2, y + 2, right - 2, bottom - 2, 0xFF555555);
        graphics.fill(x + 3, y + 3, right - 3, bottom - 3, 0xFFC6C6C6);

        // Header area.
        graphics.fill(x + 6, y + 6, right - 6, y + 26, 0xFF8B8B8B);
        graphics.fill(x + 6, y + 6, right - 6, y + 7, 0xFF555555);
        graphics.fill(x + 6, y + 6, x + 7, y + 26, 0xFF555555);

        // Empty content area, reserved for generated contracts.
        graphics.fill(x + 10, y + 34, right - 10, bottom - 34, 0xFF8B8B8B);
        graphics.fill(x + 11, y + 35, right - 11, bottom - 35, 0xFFBDBDBD);
    }

    @Override
    protected void renderLabels(
            GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        // Draw only the board title. AbstractContainerScreen's default labels
        // also draw the inventory caption, which this screen does not use.
        graphics.drawString(
                this.font,
                Component.translatable("screen.simplecontracts.contract_board"),
                10,
                12,
                0x404040,
                false
        );

        Component emptyMessage =
                Component.translatable("screen.simplecontracts.contract_board.empty");
        Component hintMessage =
                Component.translatable("screen.simplecontracts.contract_board.hint");

        graphics.drawCenteredString(
                this.font,
                emptyMessage,
                this.imageWidth / 2,
                76,
                0x404040
        );
        graphics.drawCenteredString(
                this.font,
                hintMessage,
                this.imageWidth / 2,
                91,
                0x555555
        );
    }
}
