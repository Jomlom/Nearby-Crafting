package com.jomlom.nearbycrafting.clientUtil;

import com.jomlom.nearbycrafting.platform.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class NearbyCraftingConfigScreen extends Screen {

    private static final int COLUMN_WIDTH = 150;
    private static final int COLUMN_GAP = 10;
    private static final int ROW_HEIGHT = 20;
    private static final int LABEL_HEIGHT = 12;

    private final Screen parent;

    private EditBox craftingTableReachBox;
    private EditBox craftingPlayerReachBox;

    public NearbyCraftingConfigScreen(Screen parent) {
        super(Component.translatable("nearbycrafting.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int leftX = this.width / 2 - COLUMN_WIDTH - COLUMN_GAP / 2;
        int rightX = this.width / 2 + COLUMN_GAP / 2;
        int y = 40;

        y = this.addCategory(leftX, rightX, y, Component.translatable("nearbycrafting.config.craftingTable"),
                Services.CONFIG.craftingTableCanReach(), Services.CONFIG::setCraftingTableCanReach,
                Services.CONFIG.craftingTableReach(), box -> this.craftingTableReachBox = box);

        y += 16;

        this.addCategory(leftX, rightX, y, Component.translatable("nearbycrafting.config.playerInventoryCrafting"),
                Services.CONFIG.craftingPlayerCanReach(), Services.CONFIG::setCraftingPlayerCanReach,
                Services.CONFIG.craftingPlayerReach(), box -> this.craftingPlayerReachBox = box);

        this.addRenderableWidget(Button.builder(Component.translatable("nearbycrafting.config.containerBlocks"), button -> {
                    this.applyReachValues();
                    this.minecraft.setScreenAndShow(new NearbyCraftingBlockTogglesScreen(this));
                })
                .pos(leftX, this.height - 52)
                .size(COLUMN_WIDTH * 2 + COLUMN_GAP, ROW_HEIGHT)
                .build());

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                .pos(this.width / 2 - 100, this.height - 28)
                .size(200, ROW_HEIGHT)
                .build());
    }

    private int addCategory(int leftX, int rightX, int y, Component title, boolean enabled, Consumer<Boolean> onToggle,
                             int reach, Consumer<EditBox> reachBoxSetter) {
        this.addRenderableWidget(new StringWidget(leftX, y, COLUMN_WIDTH * 2 + COLUMN_GAP, LABEL_HEIGHT, title, this.font));
        y += LABEL_HEIGHT + 6;

        this.addRenderableWidget(new StringWidget(rightX, y, COLUMN_WIDTH, LABEL_HEIGHT,
                Component.translatable("nearbycrafting.config.reach"), this.font));
        y += LABEL_HEIGHT + 2;

        this.addRenderableWidget(enabledToggle(leftX, y, enabled, onToggle));

        EditBox reachBox = new EditBox(this.font, rightX, y, COLUMN_WIDTH, ROW_HEIGHT,
                Component.translatable("nearbycrafting.config.reach"));
        reachBox.setValue(String.valueOf(reach));
        this.addRenderableWidget(reachBox);
        reachBoxSetter.accept(reachBox);

        return y + ROW_HEIGHT;
    }

    private static CycleButton<Boolean> enabledToggle(int x, int y, boolean value, Consumer<Boolean> onToggle) {
        return CycleButton.<Boolean>builder(enabled -> enabled
                        ? Component.translatable("nearbycrafting.config.enabled").withStyle(ChatFormatting.GREEN)
                        : Component.translatable("nearbycrafting.config.disabled").withStyle(ChatFormatting.RED), value)
                .withValues(true, false)
                .displayOnlyValue()
                .create(x, y, COLUMN_WIDTH, ROW_HEIGHT, CommonComponents.EMPTY, (button, newValue) -> onToggle.accept(newValue));
    }

    private void applyReachValues() {
        applyReach(this.craftingTableReachBox, Services.CONFIG::setCraftingTableReach);
        applyReach(this.craftingPlayerReachBox, Services.CONFIG::setCraftingPlayerReach);
    }

    private static void applyReach(EditBox box, IntConsumer setter) {
        try {
            int value = Integer.parseInt(box.getValue().trim());
            setter.accept(Math.max(0, Math.min(50, value)));
        } catch (NumberFormatException ignored) {
        }
    }

    @Override
    public void onClose() {
        this.applyReachValues();
        Services.CONFIG.save();
        this.minecraft.setScreenAndShow(this.parent);
    }
}
