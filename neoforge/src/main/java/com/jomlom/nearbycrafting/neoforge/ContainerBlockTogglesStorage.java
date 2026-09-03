package com.jomlom.nearbycrafting.neoforge;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.jomlom.nearbycrafting.NearbyCraftingCommon;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ContainerBlockTogglesStorage {

    private static final Path PATH = FMLPaths.CONFIGDIR.get().resolve("nearby_crafting_container_toggles.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type TOGGLES_TYPE = new TypeToken<Map<String, Map<String, Boolean>>>() {}.getType();

    public static Map<String, Map<String, Boolean>> load() {
        if (Files.exists(PATH)) {
            try (Reader reader = Files.newBufferedReader(PATH)) {
                Map<String, Map<String, Boolean>> result = GSON.fromJson(reader, TOGGLES_TYPE);
                if (result != null) {
                    return new HashMap<>(result);
                }
            } catch (IOException e) {
                NearbyCraftingCommon.LOGGER.error("Failed to load container block toggles", e);
            }
        }
        return new HashMap<>();
    }

    public static void save(Map<String, Map<String, Boolean>> toggles) {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(toggles, TOGGLES_TYPE, writer);
            }
        } catch (IOException e) {
            NearbyCraftingCommon.LOGGER.error("Failed to save container block toggles", e);
        }
    }
}
