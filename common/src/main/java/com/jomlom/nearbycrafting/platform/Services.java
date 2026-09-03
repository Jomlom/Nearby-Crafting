package com.jomlom.nearbycrafting.platform;

import com.jomlom.nearbycrafting.NearbyCraftingCommon;

import java.util.ServiceLoader;

public class Services {

    public static final NearbyCraftingConfigService CONFIG = load(NearbyCraftingConfigService.class);

    public static <T> T load(Class<T> clazz) {
        T loaded = ServiceLoader.load(clazz).findFirst()
                .orElseThrow(() -> new RuntimeException("Failed to load service for " + clazz.getName()));
        NearbyCraftingCommon.LOGGER.info("Loaded {} for service {}", loaded, clazz);
        return loaded;
    }
}
