package com.jomlom.nearbycrafting.fabric;

import com.google.gson.GsonBuilder;
import com.jomlom.nearbycrafting.NearbyCraftingCommon;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class NearbyCraftingConfigFabric {

    public static final ConfigClassHandler<NearbyCraftingConfigFabric> HANDLER =
            ConfigClassHandler.createBuilder(NearbyCraftingConfigFabric.class)
                    .id(ResourceLocation.fromNamespaceAndPath(NearbyCraftingCommon.MOD_ID, "config"))
                    .serializer(config -> GsonConfigSerializerBuilder.create(config)
                            .setPath(FabricLoader.getInstance().getConfigDir().resolve("nearby_crafting_config.json5"))
                            .appendGsonBuilder(GsonBuilder::setPrettyPrinting)
                            .setJson5(true)
                            .build())
                    .build();

    public static int defaultReach = 8;

    @SerialEntry public static Map<String, Map<String, Boolean>> containerBlockToggles = new HashMap<>();

    @SerialEntry public static boolean craftingPlayerCanReach = true;
    @SerialEntry public static int craftingPlayerReach = defaultReach;

    @SerialEntry public static boolean craftingTableCanReach = true;
    @SerialEntry public static int craftingTableReach = defaultReach;

}
