package com.jomlom.nearbycrafting.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jomlom.nearbycrafting.NearbyCraftingCommon;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class NearbyItemsPanelConfig {

    private static final Path PATH = Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("nearbycrafting-panel.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static boolean enabled = true;
    private static int offsetX = 0;
    private static int offsetY = 0;

    static {
        load();
    }

    public static boolean enabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static int offsetX() {
        return offsetX;
    }

    public static void setOffsetX(int value) {
        offsetX = value;
    }

    public static int offsetY() {
        return offsetY;
    }

    public static void setOffsetY(int value) {
        offsetY = value;
    }

    private static void load() {
        if (!Files.exists(PATH)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(PATH)) {
            Data data = GSON.fromJson(reader, Data.class);
            if (data == null) {
                return;
            }
            enabled = data.enabled;
            offsetX = data.offsetX;
            offsetY = data.offsetY;
        } catch (IOException e) {
            NearbyCraftingCommon.LOGGER.error("Failed to load panel config", e);
        }
    }

    public static void save() {
        Data data = new Data();
        data.enabled = enabled;
        data.offsetX = offsetX;
        data.offsetY = offsetY;

        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            NearbyCraftingCommon.LOGGER.error("Failed to save panel config", e);
        }
    }

    private static class Data {
        boolean enabled = true;
        int offsetX = 0;
        int offsetY = 0;
    }
}
