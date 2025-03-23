package com.jomlom.nearbycrafting.clientUtil;

import com.jomlom.nearbycrafting.util.NearbyCraftingConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import net.minecraft.text.Text;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent ->
            {
//                MinecraftClient client = MinecraftClient.getInstance();
//                boolean isSinglePlayer = client.isInSingleplayer();

                return YetAnotherConfigLib.createBuilder()
                        .title(Text.of("Nearby Crafting Configuration"))
                        .category(
                                ConfigCategory.createBuilder()
                                        .name(Text.of("Nearby Crafting Configuration"))
                                        .group(
                                                OptionGroup.createBuilder()
                                                        .name(Text.of("Crafting Table"))
                                                        .option(Option.<Boolean>createBuilder()
                                                                .name(Text.of("Enabled"))
                                                                .description(OptionDescription.of(Text.of("Allows crafting tables to reach nearby item containers and use their contents for crafting.")))
                                                                .binding(
                                                                        true,
                                                                        () -> NearbyCraftingConfig.craftingTableCanReach,
                                                                        newVal -> { NearbyCraftingConfig.craftingTableCanReach = newVal;
                                                                            NearbyCraftingConfig.HANDLER.save();
                                                                        }
                                                                )
                                                                .controller(opt -> BooleanControllerBuilder.create(opt)
                                                                        .formatValue(val -> val ? Text.of("True") : Text.of("False")).coloured(true))
                                                                .build())
                                                        .option(Option.<Integer>createBuilder()
                                                                .name(Text.of("Reach Radius"))
                                                                .description(OptionDescription.of(Text.of("Radius (in blocks) which crafting tables can reach item containers.")))
                                                                .binding(
                                                                        NearbyCraftingConfig.defaultReach,
                                                                        () -> NearbyCraftingConfig.craftingTableReach,
                                                                        newVal -> { NearbyCraftingConfig.craftingTableReach = newVal;
                                                                            NearbyCraftingConfig.HANDLER.save();
                                                                        }
                                                                )
                                                                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                                        .range(0, 50)
                                                                        .step(1)
                                                                        .formatValue(val -> Text.of(val + " blocks")))
                                                                .build())
                                                        .build()
                                        )
                                        .group(
                                                OptionGroup.createBuilder()
                                                        .name(Text.of("Player Inventory Crafting"))
                                                        .option(Option.<Boolean>createBuilder()
                                                                .name(Text.of("Enabled"))
                                                                .description(OptionDescription.of(Text.of("Allows players to reach nearby item containers and use their contents for crafting.")))
                                                                .binding(
                                                                        true,
                                                                        () -> NearbyCraftingConfig.craftingPlayerCanReach,
                                                                        newVal -> { NearbyCraftingConfig.craftingPlayerCanReach = newVal;
                                                                            NearbyCraftingConfig.HANDLER.save();
                                                                        }
                                                                )
                                                                .controller(opt -> BooleanControllerBuilder.create(opt)
                                                                        .formatValue(val -> val ? Text.of("True") : Text.of("False")).coloured(true))
                                                                .build())
                                                        .option(Option.<Integer>createBuilder()
                                                                .name(Text.of("Reach Radius"))
                                                                .description(OptionDescription.of(Text.of("Radius (in blocks) which players can reach item containers.")))
                                                                .binding(
                                                                        NearbyCraftingConfig.defaultReach,
                                                                        () -> NearbyCraftingConfig.craftingPlayerReach,
                                                                        newVal -> {
                                                                            NearbyCraftingConfig.craftingPlayerReach = newVal;
                                                                            NearbyCraftingConfig.HANDLER.save();
                                                                        }
                                                                )
                                                                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                                        .range(0, 50)
                                                                        .step(1)
                                                                        .formatValue(val -> Text.of(val + " blocks")))
                                                                .build())
                                                        .build()
                                        )
                                        .build()
                        )
                        .build()
                        .generateScreen(parent);
            };
    }
}
