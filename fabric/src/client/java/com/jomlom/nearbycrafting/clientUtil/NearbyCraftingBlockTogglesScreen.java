package com.jomlom.nearbycrafting.clientUtil;

import com.jomlom.nearbycrafting.platform.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class NearbyCraftingBlockTogglesScreen extends OptionsSubScreen {

    private static final Method GET_SCROLL_AMOUNT_METHOD = resolveMethod(new String[]{"scrollAmount", "getScrollAmount"});
    private static final Method SET_SCROLL_AMOUNT_METHOD = resolveMethod(new String[]{"setScrollAmount"}, double.class);

    private static Method resolveMethod(String[] names, Class<?>... paramTypes) {
        for (String name : names) {
            try {
                return OptionsList.class.getMethod(name, paramTypes);
            } catch (NoSuchMethodException ignored) {
            }
        }
        return null;
    }

    private double currentScrollAmount() {
        if (GET_SCROLL_AMOUNT_METHOD == null) {
            return 0;
        }
        try {
            return (double) GET_SCROLL_AMOUNT_METHOD.invoke(this.list);
        } catch (ReflectiveOperationException e) {
            return 0;
        }
    }

    private void applyScrollAmount(double amount) {
        if (SET_SCROLL_AMOUNT_METHOD == null) {
            return;
        }
        try {
            SET_SCROLL_AMOUNT_METHOD.invoke(this.list, amount);
        } catch (ReflectiveOperationException ignored) {
        }
    }

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

    private final Set<String> expandedGroups;
    private final double scrollAmount;

    public NearbyCraftingBlockTogglesScreen(Screen parent) {
        this(parent, new HashSet<>(), 0);
    }

    private NearbyCraftingBlockTogglesScreen(Screen parent, Set<String> expandedGroups, double scrollAmount) {
        super(parent, Minecraft.getInstance().options, Component.translatable("nearbycrafting.config.containerBlocks"));
        this.expandedGroups = expandedGroups;
        this.scrollAmount = scrollAmount;
    }

    @Override
    protected void addOptions() {
        Map<String, Map<String, Boolean>> toggles = new TreeMap<>(Services.CONFIG.containerBlockToggles());

        for (Map.Entry<String, Map<String, Boolean>> namespaceEntry : toggles.entrySet()) {
            String namespace = namespaceEntry.getKey();
            this.list.addSmall(List.of(new StringWidget(0, 0, 310, this.font.lineHeight, Component.literal(namespace), this.font)));
            this.addBlockSection(namespace, new ArrayList<>(new TreeMap<>(namespaceEntry.getValue()).keySet()));
        }

        this.applyScrollAmount(this.scrollAmount);
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
                        Component.literal((value ? "▼ " : "▶ ") + family + " (" + blockIds.size() + ")"))
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
                    this.minecraft.setScreen(new NearbyCraftingBlockTogglesScreen(this.lastScreen, new HashSet<>(this.expandedGroups), this.currentScrollAmount()));
                });

        this.list.addSmall(expandToggle, allToggle);

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
        this.minecraft.setScreen(new NearbyCraftingBlockTogglesScreen(this.lastScreen, next, this.currentScrollAmount()));
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

        this.list.addSmall(List.of(toggle));
    }

    @Override
    public void onClose() {
        Services.CONFIG.save();
        super.onClose();
    }
}
