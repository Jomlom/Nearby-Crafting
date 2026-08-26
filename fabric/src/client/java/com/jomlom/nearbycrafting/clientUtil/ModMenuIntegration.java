package com.jomlom.nearbycrafting.clientUtil;

import com.jomlom.nearbycrafting.fabric.NearbyCraftingConfigFabric;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import net.minecraft.network.chat.Component;

import java.util.Map;
import java.util.TreeMap;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            YetAnotherConfigLib.Builder builder = YetAnotherConfigLib.createBuilder()
                    .title(Component.literal("Nearby Crafting Configuration"));

            // Base Category
            builder.category(
                    ConfigCategory.createBuilder()
                            .name(Component.literal("Nearby Crafting Configuration"))
                            .group(
                                    OptionGroup.createBuilder()
                                            .name(Component.literal("Crafting Table"))
                                            .option(Option.<Boolean>createBuilder()
                                                    .name(Component.literal("Enabled"))
                                                    .description(OptionDescription.of(Component.literal("Allows crafting tables to reach nearby item containers and use their contents for crafting.")))
                                                    .binding(
                                                            true,
                                                            () -> NearbyCraftingConfigFabric.craftingTableCanReach,
                                                            newVal -> {
                                                                NearbyCraftingConfigFabric.craftingTableCanReach = newVal;
                                                                NearbyCraftingConfigFabric.HANDLER.save();
                                                            }
                                                    )
                                                    .controller(opt -> BooleanControllerBuilder.create(opt)
                                                            .formatValue(val -> val ? Component.literal("True") : Component.literal("False"))
                                                            .coloured(true))
                                                    .build())
                                            .option(Option.<Integer>createBuilder()
                                                    .name(Component.literal("Reach Radius"))
                                                    .description(OptionDescription.of(Component.literal("Radius (in blocks) which crafting tables can reach item containers.")))
                                                    .binding(
                                                            NearbyCraftingConfigFabric.defaultReach,
                                                            () -> NearbyCraftingConfigFabric.craftingTableReach,
                                                            newVal -> {
                                                                NearbyCraftingConfigFabric.craftingTableReach = newVal;
                                                                NearbyCraftingConfigFabric.HANDLER.save();
                                                            }
                                                    )
                                                    .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                            .range(0, 50)
                                                            .step(1)
                                                            .formatValue(val -> Component.literal(val + " blocks")))
                                                    .build())
                                            .build()
                            )
                            .group(
                                    OptionGroup.createBuilder()
                                            .name(Component.literal("Player Inventory Crafting"))
                                            .option(Option.<Boolean>createBuilder()
                                                    .name(Component.literal("Enabled"))
                                                    .description(OptionDescription.of(Component.literal("Allows players to reach nearby item containers and use their contents for crafting.")))
                                                    .binding(
                                                            true,
                                                            () -> NearbyCraftingConfigFabric.craftingPlayerCanReach,
                                                            newVal -> {
                                                                NearbyCraftingConfigFabric.craftingPlayerCanReach = newVal;
                                                                NearbyCraftingConfigFabric.HANDLER.save();
                                                            }
                                                    )
                                                    .controller(opt -> BooleanControllerBuilder.create(opt)
                                                            .formatValue(val -> val ? Component.literal("True") : Component.literal("False"))
                                                            .coloured(true))
                                                    .build())
                                            .option(Option.<Integer>createBuilder()
                                                    .name(Component.literal("Reach Radius"))
                                                    .description(OptionDescription.of(Component.literal("Radius (in blocks) which players can reach item containers.")))
                                                    .binding(
                                                            NearbyCraftingConfigFabric.defaultReach,
                                                            () -> NearbyCraftingConfigFabric.craftingPlayerReach,
                                                            newVal -> {
                                                                NearbyCraftingConfigFabric.craftingPlayerReach = newVal;
                                                                NearbyCraftingConfigFabric.HANDLER.save();
                                                            }
                                                    )
                                                    .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                            .range(0, 50)
                                                            .step(1)
                                                            .formatValue(val -> Component.literal(val + " blocks")))
                                                    .build())
                                            .build()
                            )
                            .build()
            );

            // Dynamic Block Toggles by Namespace
            TreeMap<String, Map<String, Boolean>> sortedToggles = new TreeMap<>(NearbyCraftingConfigFabric.containerBlockToggles);

            for (Map.Entry<String, Map<String, Boolean>> namespaceEntry : sortedToggles.entrySet()) {
                String namespace = namespaceEntry.getKey();
                Map<String, Boolean> blocks = new TreeMap<>(namespaceEntry.getValue());

                OptionGroup.Builder groupBuilder = OptionGroup.createBuilder()
                        .name(Component.literal("Enabled Blocks"));

                groupBuilder.option(
                        Option.<Boolean>createBuilder()
                                .name(Component.literal("Info"))
                                .description(OptionDescription.of(Component.literal("These are all of the detected blocks with inventories in this namespace. Enable/disable them to control Nearby Crafting access.")))
                                .binding(false, () -> false, val -> {})
                                .controller(opt -> BooleanControllerBuilder.create(opt)
                                        .formatValue(val -> Component.literal(""))
                                        .coloured(false))
                                .build()
                );

                for (Map.Entry<String, Boolean> blockEntry : blocks.entrySet()) {
                    String blockId = blockEntry.getKey();
                    Boolean enabled = blockEntry.getValue();

                    groupBuilder.option(
                            Option.<Boolean>createBuilder()
                                    .name(Component.literal(blockId))
                                    .description(OptionDescription.of(Component.literal("Determines whether Nearby Crafting can access this block’s inventory during crafting.")))
                                    .binding(
                                            true,
                                            () -> NearbyCraftingConfigFabric.containerBlockToggles
                                                    .getOrDefault(namespace, Map.of())
                                                    .getOrDefault(blockId, true),
                                            newVal -> {
                                                NearbyCraftingConfigFabric.containerBlockToggles
                                                        .computeIfAbsent(namespace, x -> new TreeMap<>())
                                                        .put(blockId, newVal);
                                                NearbyCraftingConfigFabric.HANDLER.save();
                                            }
                                    )
                                    .controller(opt -> BooleanControllerBuilder.create(opt)
                                            .formatValue(val -> val ? Component.literal("True") : Component.literal("False"))
                                            .coloured(true))
                                    .build()
                    );
                }

                builder.category(
                        ConfigCategory.createBuilder()
                                .name(Component.literal("Blocks: " + namespace))
                                .group(groupBuilder.build())
                                .build()
                );
            }

            return builder.build().generateScreen(parent);
        };
    }
}
