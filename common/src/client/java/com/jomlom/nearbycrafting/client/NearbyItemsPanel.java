package com.jomlom.nearbycrafting.client;

import com.jomlom.nearbycrafting.mixin.client.RecipeBookComponentAccessor;
import com.jomlom.nearbycrafting.mixin.client.RecipeBookPageAccessor;
import com.jomlom.recipebookaccess.network.ClientItemsReciever;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class NearbyItemsPanel {

    public static final int BUTTON_SIZE = 12;

    private static final int SLOTS = 5;
    private static final int SLOT_SIZE = 18;
    private static final int ITEM_INSET = 1;
    private static final int PADDING = 5;
    private static final int TITLE_TOP = 4;
    private static final int TITLE_HEIGHT = 10;
    private static final int SIDE_WIDTH = 14;
    private static final int PANEL_WIDTH = PADDING * 2 + SLOTS * SLOT_SIZE + SIDE_WIDTH;
    private static final int PANEL_HEIGHT = TITLE_TOP + TITLE_HEIGHT + SLOT_SIZE + PADDING;
    private static final int PANEL_GAP = 2;
    private static final int SLOTS_END = PADDING + SLOTS * SLOT_SIZE;
    private static final int ELLIPSIS_OFFSET_X = 2;
    private static final int ELLIPSIS_OFFSET_Y = 5;

    private static final int SCALED_COUNT_FROM = 100;
    private static final float COUNT_SCALE = 0.75F;
    private static final int SCALED_COUNT_OFFSET_Y = 12;
    private static final float SCALED_COUNT_Z = 200.0F;

    private static final int BLACK = 0xFF000000;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int PANEL_BODY = 0xFFC6C6C6;
    private static final int PANEL_SHADOW = 0xFF555555;
    private static final int SLOT_BODY = 0xFF8B8B8B;
    private static final int SLOT_SHADOW = 0xFF373737;
    private static final int TITLE_COLOR = 0xFF404040;

    private static final Component TITLE = Component.translatable("nearbycrafting.panel.title");

    private static List<ItemStack> totalsSource;
    private static List<NearbyItem> totals = List.of();

    private static final class NearbyItem {
        private final ItemStack stack;
        private int count;

        private NearbyItem(ItemStack stack, int count) {
            this.stack = stack.copyWithCount(1);
            this.count = count;
        }
    }

    public static int buttonX(int leftPos) {
        return leftPos + SLOTS_END + 1;
    }

    public static int buttonY(int topPos) {
        return topPos - PANEL_HEIGHT - PANEL_GAP + TITLE_TOP;
    }

    public static void extract(GuiGraphics graphics, Font font, Inventory inventory, int leftPos, int topPos, @Nullable List<Ingredient> requirements) {
        List<NearbyItem> items = visibleItems(inventory, requirements);

        int x = leftPos;
        int y = topPos - PANEL_HEIGHT - PANEL_GAP;
        drawPanel(graphics, x, y, PANEL_WIDTH, PANEL_HEIGHT);
        graphics.drawString(font, TITLE, x + PADDING, y + TITLE_TOP, TITLE_COLOR, false);

        int slotY = y + TITLE_TOP + TITLE_HEIGHT;
        for (int i = 0; i < SLOTS; i++) {
            int slotX = x + PADDING + i * SLOT_SIZE;
            drawSlot(graphics, slotX, slotY);
            if (i < items.size()) {
                drawItem(graphics, font, items.get(i), slotX + ITEM_INSET, slotY + ITEM_INSET);
            }
        }
        if (items.size() > SLOTS) {
            graphics.drawString(font, "...", x + SLOTS_END + ELLIPSIS_OFFSET_X, slotY + ELLIPSIS_OFFSET_Y, TITLE_COLOR, false);
        }
    }

    public static @Nullable List<Ingredient> requirements(RecipeBookComponent<?> component, RecipeBookMenu menu) {
        RecipeBookComponentAccessor componentAccess = (RecipeBookComponentAccessor) component;
        RecipeButton hovered = ((RecipeBookPageAccessor) componentAccess.nearbycrafting$getRecipeBookPage()).nearbycrafting$getHoveredButton();
        if (hovered != null) {
            return requirements(hovered.getCollection(), hovered.getCurrentRecipe());
        }
        if (hasPlacedItems(menu)) {
            return requirements(componentAccess.nearbycrafting$getLastRecipeCollection(), componentAccess.nearbycrafting$getLastRecipe());
        }
        return null;
    }

    private static @Nullable List<Ingredient> requirements(@Nullable RecipeCollection collection, @Nullable RecipeDisplayId id) {
        if (collection == null || id == null) {
            return null;
        }
        return collection.getRecipes().stream()
                .filter(entry -> entry.id().equals(id))
                .findFirst()
                .flatMap(RecipeDisplayEntry::craftingRequirements)
                .orElse(null);
    }

    private static boolean hasPlacedItems(RecipeBookMenu menu) {
        return menu instanceof AbstractCraftingMenu craftingMenu && craftingMenu.getInputGridSlots().stream().anyMatch(slot -> slot.hasItem());
    }

    private static List<NearbyItem> visibleItems(Inventory inventory, @Nullable List<Ingredient> requirements) {
        List<NearbyItem> items = new ArrayList<>(totals(inventory));
        if (requirements != null) {
            items.removeIf(item -> requirements.stream().noneMatch(ingredient -> ingredient.test(item.stack)));
        }
        items.sort(Comparator.comparingInt((NearbyItem item) -> item.count).reversed());
        return items;
    }

    private static List<NearbyItem> totals(Inventory inventory) {
        List<ItemStack> source = ClientItemsReciever.getItemStacks();
        if (source == totalsSource) {
            return totals;
        }

        List<NearbyItem> items = new ArrayList<>();
        for (ItemStack stack : source) {
            addCount(items, stack, stack.getCount());
        }
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            addCount(items, stack, -stack.getCount());
        }
        items.removeIf(item -> item.count <= 0);

        totalsSource = source;
        totals = items;
        return items;
    }

    private static void addCount(List<NearbyItem> items, ItemStack stack, int delta) {
        if (stack.isEmpty()) {
            return;
        }
        for (NearbyItem item : items) {
            if (ItemStack.isSameItemSameComponents(item.stack, stack)) {
                item.count += delta;
                return;
            }
        }
        if (delta > 0) {
            items.add(new NearbyItem(stack, delta));
        }
    }

    private static void drawItem(GuiGraphics graphics, Font font, NearbyItem item, int x, int y) {
        graphics.renderItem(item.stack, x, y);
        if (item.count < SCALED_COUNT_FROM) {
            graphics.renderItemDecorations(font, item.stack, x, y, item.count == 1 ? null : Integer.toString(item.count));
            return;
        }
        graphics.renderItemDecorations(font, item.stack, x, y, "");
        Component count = Component.literal(formatCount(item.count));
        int width = Math.round(font.width(count) * COUNT_SCALE);
        drawScaledText(graphics, font, count, x + SLOT_SIZE - ITEM_INSET - width, y + SCALED_COUNT_OFFSET_Y - ITEM_INSET, WHITE, true);
    }

    private static String formatCount(int count) {
        if (count < 1000) {
            return Integer.toString(count);
        }
        if (count < 10000) {
            return count / 1000 + "." + count % 1000 / 100 + "k";
        }
        return count < 1000000 ? count / 1000 + "k" : count / 1000000 + "m";
    }

    private static void drawScaledText(GuiGraphics graphics, Font font, Component text, int x, int y, int color, boolean shadow) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, SCALED_COUNT_Z);
        graphics.pose().scale(COUNT_SCALE, COUNT_SCALE, 1.0F);
        graphics.drawString(font, text, 0, 0, color, shadow);
        graphics.pose().popPose();
    }

    private static void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, SLOT_BODY);
        graphics.fill(x, y, x + SLOT_SIZE - 1, y + 1, SLOT_SHADOW);
        graphics.fill(x, y, x + 1, y + SLOT_SIZE - 1, SLOT_SHADOW);
        graphics.fill(x + 1, y + SLOT_SIZE - 1, x + SLOT_SIZE, y + SLOT_SIZE, WHITE);
        graphics.fill(x + SLOT_SIZE - 1, y + 1, x + SLOT_SIZE, y + SLOT_SIZE, WHITE);
    }

    private static void drawPanel(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x + 1, y, x + width - 1, y + 1, BLACK);
        graphics.fill(x, y + 1, x + width, y + height - 1, BLACK);
        graphics.fill(x + 1, y + height - 1, x + width - 1, y + height, BLACK);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, PANEL_BODY);
        graphics.fill(x + 1, y + 1, x + width - 2, y + 3, WHITE);
        graphics.fill(x + 1, y + 1, x + 3, y + height - 2, WHITE);
        graphics.fill(x + 2, y + height - 3, x + width - 1, y + height - 1, PANEL_SHADOW);
        graphics.fill(x + width - 3, y + 2, x + width - 1, y + height - 1, PANEL_SHADOW);
        graphics.fill(x + 3, y + 3, x + width - 3, y + height - 3, PANEL_BODY);
    }
}
