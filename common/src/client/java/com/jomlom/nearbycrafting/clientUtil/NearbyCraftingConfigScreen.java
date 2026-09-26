package com.jomlom.nearbycrafting.clientUtil;

import com.jomlom.nearbycrafting.CraftingMode;
import com.jomlom.nearbycrafting.platform.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class NearbyCraftingConfigScreen extends Screen {

    private static final int COLUMN_WIDTH = 150;
    private static final int COLUMN_GAP = 10;
    private static final int ROW_HEIGHT = 20;
    private static final int LABEL_HEIGHT = 12;
    private static final int MAX_VALUE = 50;

    private final Screen parent;
    private final List<RangeField> rangeFields = new ArrayList<>();

    public NearbyCraftingConfigScreen(Screen parent) {
        super(Component.translatable("nearbycrafting.config.title"));
        this.parent = parent;
    }

    private record Range(Component label, int value, int min, IntConsumer setter) {}

    private record RangeField(EditBox box, int min, IntConsumer setter) {}

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void init() {
        this.rangeFields.clear();

        int leftX = this.width / 2 - COLUMN_WIDTH - COLUMN_GAP / 2;
        int rightX = this.width / 2 + COLUMN_GAP / 2;
        int y = 32;

        this.addRenderableWidget(this.modeButton(leftX, y));
        y += ROW_HEIGHT + 8;

        boolean connected = Services.CONFIG.mode() == CraftingMode.CONNECTED;

        Range tableRange = connected
                ? new Range(Component.translatable("nearbycrafting.config.depth"), Services.CONFIG.craftingTableDepth(), 1, Services.CONFIG::setCraftingTableDepth)
                : new Range(Component.translatable("nearbycrafting.config.reach"), Services.CONFIG.craftingTableReach(), 0, Services.CONFIG::setCraftingTableReach);
        y = this.addCategory(leftX, rightX, y, Component.translatable("nearbycrafting.config.craftingTable"),
                Services.CONFIG.craftingTableCanReach(), Services.CONFIG::setCraftingTableCanReach, tableRange);

        y += 16;

        Range playerRange = connected
                ? null
                : new Range(Component.translatable("nearbycrafting.config.reach"), Services.CONFIG.craftingPlayerReach(), 0, Services.CONFIG::setCraftingPlayerReach);
        this.addCategory(leftX, rightX, y, Component.translatable("nearbycrafting.config.playerInventoryCrafting"),
                Services.CONFIG.craftingPlayerCanReach(), Services.CONFIG::setCraftingPlayerCanReach, playerRange);

        this.addRenderableWidget(Button.builder(Component.translatable("nearbycrafting.config.containerBlocks"), button -> {
                    this.applyRangeValues();
                    this.minecraft.setScreen(new NearbyCraftingBlockTogglesScreen(this));
                })
                .pos(leftX, this.height - 52)
                .size(COLUMN_WIDTH * 2 + COLUMN_GAP, ROW_HEIGHT)
                .build());

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                .pos(this.width / 2 - 100, this.height - 28)
                .size(200, ROW_HEIGHT)
                .build());
    }

    private CycleButton<CraftingMode> modeButton(int x, int y) {
        return CycleButton.<CraftingMode>builder(mode -> Component.translatable(modeKey(mode)))
                .withValues(CraftingMode.values())
                .withInitialValue(Services.CONFIG.mode())
                .withTooltip(mode -> Tooltip.create(Component.translatable(modeKey(mode) + ".tooltip")))
                .create(x, y, COLUMN_WIDTH * 2 + COLUMN_GAP, ROW_HEIGHT, Component.translatable("nearbycrafting.config.mode"), (button, mode) -> {
                    this.applyRangeValues();
                    Services.CONFIG.setMode(mode);
                    this.rebuildWidgets();
                });
    }

    private static String modeKey(CraftingMode mode) {
        return "nearbycrafting.config.mode." + mode.name().toLowerCase();
    }

    private int addCategory(int leftX, int rightX, int y, Component title, boolean enabled, Consumer<Boolean> onToggle, Range range) {
        this.addRenderableWidget(new StringWidget(leftX, y, COLUMN_WIDTH * 2 + COLUMN_GAP, LABEL_HEIGHT, title, this.font));
        y += LABEL_HEIGHT + 6;

        if (range != null) {
            this.addRenderableWidget(new StringWidget(rightX, y, COLUMN_WIDTH, LABEL_HEIGHT, range.label(), this.font));
        }
        y += LABEL_HEIGHT + 2;

        this.addRenderableWidget(enabledToggle(leftX, y, enabled, onToggle));

        if (range != null) {
            EditBox box = new EditBox(this.font, rightX, y, COLUMN_WIDTH, ROW_HEIGHT, range.label());
            box.setValue(String.valueOf(range.value()));
            this.addRenderableWidget(box);
            this.rangeFields.add(new RangeField(box, range.min(), range.setter()));
        }

        return y + ROW_HEIGHT;
    }

    private static CycleButton<Boolean> enabledToggle(int x, int y, boolean value, Consumer<Boolean> onToggle) {
        return CycleButton.<Boolean>builder(enabled -> enabled
                        ? Component.translatable("nearbycrafting.config.enabled").withStyle(ChatFormatting.GREEN)
                        : Component.translatable("nearbycrafting.config.disabled").withStyle(ChatFormatting.RED))
                .withValues(true, false)
                .withInitialValue(value)
                .displayOnlyValue()
                .create(x, y, COLUMN_WIDTH, ROW_HEIGHT, CommonComponents.EMPTY, (button, newValue) -> onToggle.accept(newValue));
    }

    private void applyRangeValues() {
        for (RangeField field : this.rangeFields) {
            try {
                int value = Integer.parseInt(field.box().getValue().trim());
                field.setter().accept(Math.max(field.min(), Math.min(MAX_VALUE, value)));
            } catch (NumberFormatException ignored) {
            }
        }
    }

    @Override
    public void onClose() {
        this.applyRangeValues();
        Services.CONFIG.save();
        this.minecraft.setScreen(this.parent);
    }
}
