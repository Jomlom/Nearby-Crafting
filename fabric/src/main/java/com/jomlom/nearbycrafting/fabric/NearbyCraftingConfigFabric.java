package com.jomlom.nearbycrafting.fabric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jomlom.nearbycrafting.NearbyCraftingCommon;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class NearbyCraftingConfigFabric {

    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("nearby_crafting_config.json5");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static int defaultReach = 8;

    public static Map<String, Map<String, Boolean>> containerBlockToggles = new HashMap<>();

    public static boolean craftingPlayerCanReach = true;
    public static int craftingPlayerReach = defaultReach;

    public static boolean craftingTableCanReach = true;
    public static int craftingTableReach = defaultReach;

    public static void load() {
        if (!Files.exists(PATH)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(PATH)) {
            Data data = GSON.fromJson(reader, Data.class);
            if (data == null) {
                return;
            }
            containerBlockToggles = data.containerBlockToggles != null ? data.containerBlockToggles : new HashMap<>();
            craftingPlayerCanReach = data.craftingPlayerCanReach;
            craftingPlayerReach = data.craftingPlayerReach;
            craftingTableCanReach = data.craftingTableCanReach;
            craftingTableReach = data.craftingTableReach;
        } catch (IOException e) {
            NearbyCraftingCommon.LOGGER.error("Failed to load config", e);
        }
    }

    public static void save() {
        Data data = new Data();
        data.containerBlockToggles = containerBlockToggles;
        data.craftingPlayerCanReach = craftingPlayerCanReach;
        data.craftingPlayerReach = craftingPlayerReach;
        data.craftingTableCanReach = craftingTableCanReach;
        data.craftingTableReach = craftingTableReach;

        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            NearbyCraftingCommon.LOGGER.error("Failed to save config", e);
        }
    }

    private static class Data {
        Map<String, Map<String, Boolean>> containerBlockToggles = new HashMap<>();
        boolean craftingPlayerCanReach = true;
        int craftingPlayerReach = 8;
        boolean craftingTableCanReach = true;
        int craftingTableReach = 8;
    }
}
