package com.simplecontracts.client;

import com.simplecontracts.menu.ContractBoardMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class ContractBoardScreen extends AbstractContainerScreen<ContractBoardMenu> {
    private final Button[] contractButtons = new Button[ContractBoardMenu.CONTRACT_COUNT];
    private Button previousButton;
    private Button nextButton;

    private static final String[] CONTRACT_KEYS = {
            "zombie_hunter", "skeleton_hunter", "iron_miner", "wheat_farmer", "fisher",
            "explorer", "spider_hunter", "woodcutter", "stone_miner", "creeper_hunter",
            "drowned_hunter", "gold_miner", "root_farmer", "enderman_hunter",
            "diamond_miner", "redstone_miner", "slime_hunter", "blaze_hunter",
            "witch_hunter", "emerald_miner", "obsidian_miner", "wither_skeleton_hunter",
            "guardian_hunter", "ancient_debris_miner", "warden_hunter"
    };

    public ContractBoardScreen(ContractBoardMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 280;
        imageHeight = 210;
        titleLabelX = 10;
        titleLabelY = 9;
    }

    @Override
    protected void init() {
        super.init();
        previousButton = addRenderableWidget(Button.builder(Component.literal("<"),
                        b -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 100))
                .bounds(leftPos + 10, topPos + 30, 20, 18).build());
        nextButton = addRenderableWidget(Button.builder(Component.literal(">"),
                        b -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 101))
                .bounds(leftPos + imageWidth - 30, topPos + 30, 20, 18).build());

        for (int slot = 0; slot < ContractBoardMenu.CONTRACT_COUNT; slot++) {
            final int index = slot;
            contractButtons[slot] = addRenderableWidget(Button.builder(
                            Component.translatable("screen.simplecontracts.contract_board.accept"),
                            b -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId,
                                    menu.isContractAccepted(index) && menu.isContractCompleted(index)
                                            ? 10 + index : index))
                    .bounds(leftPos + 196, topPos + 67 + slot * 39, 72, 20).build());
        }

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
                .bounds(leftPos + imageWidth - 76, topPos + imageHeight - 25, 66, 20).build());
        updateButtons();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateButtons();
    }

    private void updateButtons() {
        int level = menu.getSelectedLevel();
        previousButton.active = level > 1;
        nextButton.active = level < menu.getPlayerLevel();
        for (int slot = 0; slot < contractButtons.length; slot++) {
            Button button = contractButtons[slot];
            boolean exists = menu.getContractType(slot) >= 0;
            boolean accepted = menu.isContractAccepted(slot);
            boolean claimed = menu.isContractClaimed(slot);
            boolean completed = menu.isContractCompleted(slot);
            button.active = exists && (!accepted || (completed && !claimed));
            String label = !accepted ? "screen.simplecontracts.contract_board.accept"
                    : claimed ? "screen.simplecontracts.contract_board.claimed"
                    : completed ? "screen.simplecontracts.contract_board.claim"
                    : "screen.simplecontracts.contract_board.accepted";
            button.setMessage(Component.translatable(label));
        }
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos, right = x + imageWidth, bottom = y + imageHeight;
        g.fill(x, y, right, bottom, 0xFF373737);
        g.fill(x + 1, y + 1, right - 1, bottom - 1, 0xFFFFFFFF);
        g.fill(x + 2, y + 2, right - 2, bottom - 2, 0xFF555555);
        g.fill(x + 3, y + 3, right - 3, bottom - 3, 0xFFC6C6C6);
        g.fill(x + 6, y + 6, right - 6, y + 26, 0xFF8B8B8B);
        g.fill(x + 6, y + 6, right - 6, y + 7, 0xFF555555);
        g.fill(x + 6, y + 6, x + 7, y + 26, 0xFF555555);
        for (int slot = 0; slot < ContractBoardMenu.CONTRACT_COUNT; slot++) {
            int py = y + 60 + slot * 39;
            g.fill(x + 9, py, right - 9, py + 36, 0xFF777777);
            g.fill(x + 10, py + 1, right - 10, py + 35, 0xFFE0E0E0);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, Component.translatable("screen.simplecontracts.contract_board"),
                10, 12, 0x404040, false);
        Component page = Component.translatable("screen.simplecontracts.contract_board.level",
                menu.getSelectedLevel(),
                Component.translatable("screen.simplecontracts.contract_board.level_name." + menu.getSelectedLevel()));
        g.drawCenteredString(font, page, imageWidth / 2, 34, 0x202020);
        Component reputation = Component.translatable("screen.simplecontracts.contract_board.reputation",
                menu.getReputation(), menu.getPlayerLevel(), menu.getActiveCount(), menu.getMaxActiveContracts());
        g.drawString(font, reputation, 36, 48, 0x303030, false);

        for (int slot = 0; slot < ContractBoardMenu.CONTRACT_COUNT; slot++) {
            int type = menu.getContractType(slot);
            if (type < 0 || type >= CONTRACT_KEYS.length) continue;
            int rowY = 62 + slot * 39;
            String key = CONTRACT_KEYS[type];
            Component title = Component.translatable("contract.simplecontracts." + key + ".title");
            Component description = Component.translatable("contract.simplecontracts." + key + ".description");
            Component details = Component.translatable("screen.simplecontracts.contract_board.details",
                    menu.getContractProgress(slot), menu.getContractTarget(slot), menu.getContractReward(slot));
            g.drawString(font, title, 15, rowY, 0x202020, false);
            g.drawString(font, description, 15, rowY + 10, 0x404040, false);
            g.drawString(font, details, 15, rowY + 21, 0x404040, false);
        }
    }

}
