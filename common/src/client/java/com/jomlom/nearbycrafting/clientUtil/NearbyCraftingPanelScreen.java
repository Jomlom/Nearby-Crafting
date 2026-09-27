package com.jomlom.nearbycrafting.clientUtil;

import com.jomlom.nearbycrafting.client.NearbyItemsPanelConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class NearbyCraftingPanelScreen extends Screen {

    private static final int BOX_WIDTH = 150;
    private static final int RESET_WIDTH = 50;
    private static final int COLUMN_GAP = 6;
    private static final int ROW_WIDTH = BOX_WIDTH + COLUMN_GAP + RESET_WIDTH;
    private static final int ROW_HEIGHT = 20;
    private static final int LABEL_HEIGHT = 12;
    private static final int MAX_OFFSET = 1000;

    private final Screen parent;

    private EditBox offsetXBox;
    private EditBox offsetYBox;

    public NearbyCraftingPanelScreen(Screen parent) {
        super(Component.translatable("nearbycrafting.panelConfig.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int boxX = this.width / 2 - ROW_WIDTH / 2;
        int resetX = boxX + BOX_WIDTH + COLUMN_GAP;
        int y = 32;

        this.addRenderableWidget(CycleButton.<Boolean>builder(enabled -> enabled
                        ? Component.translatable("nearbycrafting.config.enabled").withStyle(ChatFormatting.GREEN)
                        : Component.translatable("nearbycrafting.config.disabled").withStyle(ChatFormatting.RED), NearbyItemsPanelConfig.enabled())
                .withValues(true, false)
                .displayOnlyValue()
                .create(boxX, y, ROW_WIDTH, ROW_HEIGHT, Component.translatable("nearbycrafting.panelConfig.enabled"),
                        (button, enabled) -> NearbyItemsPanelConfig.setEnabled(enabled)));
        y += ROW_HEIGHT + 12;

        this.addRenderableWidget(new StringWidget(boxX, y, ROW_WIDTH, LABEL_HEIGHT, Component.translatable("nearbycrafting.panelConfig.offsetX"), this.font));
        y += LABEL_HEIGHT + 2;
        this.offsetXBox = new EditBox(this.font, boxX, y, BOX_WIDTH, ROW_HEIGHT, Component.translatable("nearbycrafting.panelConfig.offsetX"));
        this.offsetXBox.setValue(String.valueOf(NearbyItemsPanelConfig.offsetX()));
        this.addRenderableWidget(this.offsetXBox);
        this.addRenderableWidget(Button.builder(Component.translatable("nearbycrafting.panelConfig.reset"), button -> this.offsetXBox.setValue("0"))
                .pos(resetX, y)
                .size(RESET_WIDTH, ROW_HEIGHT)
                .build());
        y += ROW_HEIGHT + 8;

        this.addRenderableWidget(new StringWidget(boxX, y, ROW_WIDTH, LABEL_HEIGHT, Component.translatable("nearbycrafting.panelConfig.offsetY"), this.font));
        y += LABEL_HEIGHT + 2;
        this.offsetYBox = new EditBox(this.font, boxX, y, BOX_WIDTH, ROW_HEIGHT, Component.translatable("nearbycrafting.panelConfig.offsetY"));
        this.offsetYBox.setValue(String.valueOf(NearbyItemsPanelConfig.offsetY()));
        this.addRenderableWidget(this.offsetYBox);
        this.addRenderableWidget(Button.builder(Component.translatable("nearbycrafting.panelConfig.reset"), button -> this.offsetYBox.setValue("0"))
                .pos(resetX, y)
                .size(RESET_WIDTH, ROW_HEIGHT)
                .build());

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                .pos(this.width / 2 - 100, this.height - 28)
                .size(200, ROW_HEIGHT)
                .build());
    }

    private void applyOffsets() {
        NearbyItemsPanelConfig.setOffsetX(parseOffset(this.offsetXBox.getValue(), NearbyItemsPanelConfig.offsetX()));
        NearbyItemsPanelConfig.setOffsetY(parseOffset(this.offsetYBox.getValue(), NearbyItemsPanelConfig.offsetY()));
    }

    private static int parseOffset(String value, int fallback) {
        try {
            return Math.max(-MAX_OFFSET, Math.min(MAX_OFFSET, Integer.parseInt(value.trim())));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    @Override
    public void onClose() {
        this.applyOffsets();
        NearbyItemsPanelConfig.save();
        this.minecraft.setScreenAndShow(this.parent);
    }
}
