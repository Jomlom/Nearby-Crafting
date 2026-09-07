package com.jomlom.nearbycrafting.clientUtil;

import com.jomlom.nearbycrafting.platform.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class NearbyCraftingBlockTogglesScreen extends Screen {

    private static final List<String> GROUPABLE_PREFIXES = List.of(
            "white_", "orange_", "magenta_", "light_blue_", "yellow_", "lime_", "pink_", "gray_",
            "light_gray_", "cyan_", "purple_", "blue_", "brown_", "green_", "red_", "black_",
            "oak_", "spruce_", "birch_", "jungle_", "acacia_", "dark_oak_", "mangrove_", "cherry_", "bamboo_", "crimson_", "warped_", "pale_oak_",
            "waxed_exposed_", "waxed_weathered_", "waxed_oxidized_", "waxed_", "exposed_", "weathered_", "oxidized_"
    );

    private static final Set<String> WORKSTATION_BLOCKS = Set.of(
            "furnace", "blast_furnace", "smoker", "brewing_stand", "campfire", "soul_campfire", "lectern"
    );

    private static final Set<String> FUNCTIONAL_BLOCKS = Set.of(
            "jukebox", "hopper", "dispenser", "dropper", "crafter"
    );

    private final Screen parent;
    private final Set<String> expandedGroups;
    private double scrollAmount;

    private BlockList list;

    public NearbyCraftingBlockTogglesScreen(Screen parent) {
        this(parent, new HashSet<>(), 0);
    }

    private NearbyCraftingBlockTogglesScreen(Screen parent, Set<String> expandedGroups, double scrollAmount) {
        super(Component.translatable("nearbycrafting.config.containerBlocks"));
        this.parent = parent;
        this.expandedGroups = expandedGroups;
        this.scrollAmount = scrollAmount;
    }

    @Override
    protected void init() {
        this.list = new BlockList(this.minecraft, this.width, this.height, 32, this.height - 32, 24);
        this.addRenderableWidget(this.list);

        Map<String, Map<String, Boolean>> toggles = new TreeMap<>(Services.CONFIG.containerBlockToggles());
        for (Map.Entry<String, Map<String, Boolean>> namespaceEntry : toggles.entrySet()) {
            String namespace = namespaceEntry.getKey();
            this.list.addRow(new StringWidget(0, 0, 310, this.font.lineHeight, Component.literal(namespace), this.font));
            this.addBlockSection(namespace, new ArrayList<>(new TreeMap<>(namespaceEntry.getValue()).keySet()));
        }

        this.list.setScrollAmount(this.scrollAmount);

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, button -> this.onClose())
                .pos(this.width / 2 - 100, this.height - 28)
                .size(200, 20)
                .build());
    }

    private void addBlockSection(String namespace, List<String> blockIds) {
        Map<String, List<String>> groups = new TreeMap<>();
        List<String> ungrouped = new ArrayList<>();
        List<String> workstations = new ArrayList<>();
        List<String> functional = new ArrayList<>();

        for (String blockId : blockIds) {
            String path = blockId.substring(blockId.indexOf(':') + 1);
            if (WORKSTATION_BLOCKS.contains(path)) {
                workstations.add(blockId);
                continue;
            }
            if (FUNCTIONAL_BLOCKS.contains(path)) {
                functional.add(blockId);
                continue;
            }

            String family = family(path);
            if (family != null) {
                groups.computeIfAbsent(family, key -> new ArrayList<>()).add(blockId);
            } else {
                ungrouped.add(blockId);
            }
        }

        groups.entrySet().removeIf(entry -> {
            if (entry.getValue().size() < 2) {
                ungrouped.addAll(entry.getValue());
                return true;
            }
            return false;
        });

        for (Map.Entry<String, List<String>> group : groups.entrySet()) {
            String baseBlockId = namespace + ":" + group.getKey();
            if (ungrouped.remove(baseBlockId)) {
                group.getValue().add(baseBlockId);
            }
        }

        if (workstations.size() >= 2) {
            groups.put("workstations", workstations);
        } else {
            ungrouped.addAll(workstations);
        }

        if (functional.size() >= 2) {
            groups.put("functional", functional);
        } else {
            ungrouped.addAll(functional);
        }

        for (Map.Entry<String, List<String>> group : groups.entrySet()) {
            this.addGroupRows(namespace, group.getKey(), group.getValue());
        }

        for (String blockId : ungrouped) {
            this.addBlockRow(namespace, blockId, blockId.substring(blockId.indexOf(':') + 1));
        }
    }

    private static String family(String path) {
        for (String prefix : GROUPABLE_PREFIXES) {
            if (path.startsWith(prefix) && path.length() > prefix.length()) {
                return path.substring(prefix.length());
            }
        }
        return null;
    }

    private void addGroupRows(String namespace, String family, List<String> blockIds) {
        String groupKey = namespace + ":" + family;
        boolean expanded = this.expandedGroups.contains(groupKey);
        boolean allEnabled = blockIds.stream().allMatch(id -> Services.CONFIG.isContainerBlockEnabled(namespace, id));

        CycleButton<Boolean> expandToggle = CycleButton.<Boolean>builder(value ->
                        Component.literal((value ? "v " : "> ") + family + " (" + blockIds.size() + ")"))
                .withValues(true, false)
                .withInitialValue(expanded)
                .displayOnlyValue()
                .create(0, 0, 150, 20, Component.literal(family), (button, value) -> this.openWithGroup(groupKey, value));

        CycleButton<Boolean> allToggle = CycleButton.<Boolean>builder(value -> Component.literal("All: ").append(value
                        ? Component.translatable("nearbycrafting.config.enabled").withStyle(ChatFormatting.GREEN)
                        : Component.translatable("nearbycrafting.config.disabled").withStyle(ChatFormatting.RED)))
                .withValues(true, false)
                .withInitialValue(allEnabled)
                .displayOnlyValue()
                .create(0, 0, 150, 20, Component.literal(family), (button, value) -> {
                    for (String blockId : blockIds) {
                        Services.CONFIG.setContainerBlockEnabled(namespace, blockId, value);
                    }
                    Services.CONFIG.save();
                    this.minecraft.setScreen(new NearbyCraftingBlockTogglesScreen(this.parent, new HashSet<>(this.expandedGroups), this.list.getScrollAmount()));
                });

        this.list.addRow(expandToggle, allToggle);

        if (expanded) {
            String suffix = "_" + family;
            for (String blockId : blockIds) {
                String path = blockId.substring(blockId.indexOf(':') + 1);
                String label = path.endsWith(suffix) ? path.substring(0, path.length() - suffix.length()) : path;
                this.addBlockRow(namespace, blockId, label);
            }
        }
    }

    private void openWithGroup(String groupKey, boolean expanded) {
        Set<String> next = new HashSet<>(this.expandedGroups);
        if (expanded) {
            next.add(groupKey);
        } else {
            next.remove(groupKey);
        }
        this.minecraft.setScreen(new NearbyCraftingBlockTogglesScreen(this.parent, next, this.list.getScrollAmount()));
    }

    private void addBlockRow(String namespace, String blockId, String label) {
        boolean enabled = Services.CONFIG.isContainerBlockEnabled(namespace, blockId);

        CycleButton<Boolean> toggle = CycleButton.<Boolean>builder(value -> value
                        ? Component.translatable("nearbycrafting.config.enabled").withStyle(ChatFormatting.GREEN)
                        : Component.translatable("nearbycrafting.config.disabled").withStyle(ChatFormatting.RED))
                .withValues(true, false)
                .withInitialValue(enabled)
                .create(0, 0, 310, 20, Component.literal(label),
                        (button, value) -> Services.CONFIG.setContainerBlockEnabled(namespace, blockId, value));

        this.list.addRow(toggle);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        Services.CONFIG.save();
        this.minecraft.setScreen(this.parent);
    }

    private static class BlockList extends ContainerObjectSelectionList<BlockList.Row> {

        BlockList(Minecraft minecraft, int width, int height, int y0, int y1, int itemHeight) {
            super(minecraft, width, height, y0, y1, itemHeight);
        }

        void addRow(AbstractWidget widget) {
            this.addEntry(new Row(List.of(widget)));
        }

        void addRow(AbstractWidget left, AbstractWidget right) {
            this.addEntry(new Row(List.of(left, right)));
        }

        @Override
        public int getRowWidth() {
            return 320;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.getRowLeft() + this.getRowWidth() + 4;
        }

        static class Row extends ContainerObjectSelectionList.Entry<Row> {

            private final List<AbstractWidget> children;

            Row(List<AbstractWidget> children) {
                this.children = children;
            }

            @Override
            public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height,
                                int mouseX, int mouseY, boolean hovering, float partialTick) {
                int x = left;
                int widgetWidth = this.children.size() == 1 ? width : (width - 4) / 2;
                for (AbstractWidget widget : this.children) {
                    widget.setPosition(x, top);
                    widget.setWidth(widgetWidth);
                    widget.render(guiGraphics, mouseX, mouseY, partialTick);
                    x += widgetWidth + 4;
                }
            }

            @Override
            public List<? extends net.minecraft.client.gui.components.events.GuiEventListener> children() {
                return this.children;
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return this.children;
            }
        }
    }
}
