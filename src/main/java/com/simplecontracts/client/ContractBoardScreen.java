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
                                            this.menu.containerId, contractIndex
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
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        for (int slot = 0; slot < ContractBoardMenu.CONTRACT_COUNT; slot++) {
            Button button = this.acceptButtons[slot];
            if (button == null) continue;
            boolean accepted = this.menu.isContractAccepted(slot);
            button.active = !accepted && this.menu.getContractType(slot) >= 0;
            button.setMessage(Component.translatable(
                    accepted ? "screen.simplecontracts.contract_board.accepted"
                            : "screen.simplecontracts.contract_board.accept"
            ));
        }
