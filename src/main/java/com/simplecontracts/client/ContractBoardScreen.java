package com.simplecontracts.client;

import com.simplecontracts.menu.ContractBoardMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class ContractBoardScreen extends AbstractContainerScreen<ContractBoardMenu> {
    private final Button[] acceptButtons = new Button[ContractBoardMenu.CONTRACT_COUNT];

    private static final String[] CONTRACT_KEYS = {
            "zombie_hunter", "skeleton_hunter", "iron_miner",
            "wheat_farmer", "fisher", "explorer"
    };

    public ContractBoardScreen(ContractBoardMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 280;
        this.imageHeight = 178;
        this.titleLabelX = 10;
        this.titleLabelY = 9;
    }

    @Override
    protected void init() {
        super.init();

        for (int slot = 0; slot < ContractBoardMenu.CONTRACT_COUNT; slot++) {
            final int contractIndex = slot;
            this.acceptButtons[slot] = this.addRenderableWidget(
                    Button.builder(
                                    Component.translatable("screen.simplecontracts.contract_board.accept"),
                                    button -> this.minecraft.gameMode.handleInventoryButtonClick(
                                            this.menu.containerId,
                                            this.menu.isContractAccepted(contractIndex)
                                                    && this.menu.isContractCompleted(contractIndex)
                                                    ? 10 + contractIndex : contractIndex
                                    )
                            )
                            .bounds(
                                    this.leftPos + 196,
                                    this.topPos + 39 + slot * 39,
                                    72,
                                    20
                            )
                            .build()
            );
        }

        this.addRenderableWidget(
                Button.builder(Component.translatable("gui.done"), button -> this.onClose())
                        .bounds(this.leftPos + this.imageWidth - 76,
                                this.topPos + this.imageHeight - 27, 66, 20)
                        .build()
        );
        updateAcceptButtons();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateAcceptButtons();
    }

    private void updateAcceptButtons() {
        for (int slot = 0; slot < ContractBoardMenu.CONTRACT_COUNT; slot++) {
            Button button = this.acceptButtons[slot];
            if (button == null) {
                continue;
            }

            boolean accepted = this.menu.isContractAccepted(slot);
            boolean claimed = this.menu.isContractClaimed(slot);
            boolean completed = this.menu.isContractCompleted(slot);
            button.active = this.menu.getContractType(slot) >= 0
                    && (!accepted || (completed && !claimed));
            String label = !accepted
                    ? "screen.simplecontracts.contract_board.accept"
                    : claimed
                    ? "screen.simplecontracts.contract_board.claimed"
                    : completed
                    ? "screen.simplecontracts.contract_board.claim"
                    : "screen.simplecontracts.contract_board.accepted";
            button.setMessage(Component.translatable(label));
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        int right = x + this.imageWidth;
        int bottom = y + this.imageHeight;

        graphics.fill(x, y, right, bottom, 0xFF373737);
        graphics.fill(x + 1, y + 1, right - 1, bottom - 1, 0xFFFFFFFF);
        graphics.fill(x + 2, y + 2, right - 2, bottom - 2, 0xFF555555);
        graphics.fill(x + 3, y + 3, right - 3, bottom - 3, 0xFFC6C6C6);
        graphics.fill(x + 6, y + 6, right - 6, y + 26, 0xFF8B8B8B);
        graphics.fill(x + 6, y + 6, right - 6, y + 7, 0xFF555555);
        graphics.fill(x + 6, y + 6, x + 7, y + 26, 0xFF555555);

        for (int slot = 0; slot < ContractBoardMenu.CONTRACT_COUNT; slot++) {
            int panelY = y + 32 + slot * 39;
            graphics.fill(x + 9, panelY, right - 9, panelY + 36, 0xFF777777);
            graphics.fill(x + 10, panelY + 1, right - 10, panelY + 35, 0xFFE0E0E0);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font,
                Component.translatable("screen.simplecontracts.contract_board"),
                10, 12, 0x404040, false);

        for (int slot = 0; slot < ContractBoardMenu.CONTRACT_COUNT; slot++) {
            int type = this.menu.getContractType(slot);
            if (type < 0 || type >= CONTRACT_KEYS.length) {
                continue;
            }

            int rowY = 34 + slot * 39;
            String key = CONTRACT_KEYS[type];
            Component title = Component.translatable("contract.simplecontracts." + key + ".title");
            Component description = Component.translatable("contract.simplecontracts." + key + ".description");
            Component details = Component.translatable(
                    "screen.simplecontracts.contract_board.details",
                    this.menu.getContractProgress(slot),
                    this.menu.getContractTarget(slot),
                    this.menu.getContractReward(slot)
            );

            graphics.drawString(this.font, title, 15, rowY, 0x202020, false);
            graphics.drawString(this.font, description, 15, rowY + 10, 0x404040, false);
            graphics.drawString(this.font, details, 15, rowY + 21, 0x404040, false);
        }
    }
}
