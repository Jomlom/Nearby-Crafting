package com.jomlom.nearbycrafting.neoforge;

import com.jomlom.nearbycrafting.client.PriorityState;
import com.jomlom.nearbycrafting.clientUtil.NearbyCraftingConfigScreen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;

public class NearbyCraftingNeoForgeClient {

    public static void init() {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> new NearbyCraftingConfigScreen(parent)));
        PriorityState.sender = payload -> {
            try {
                NearbyCraftingNeoForge.CHANNEL.sendToServer(payload);
            } catch (RuntimeException ignored) {
            }
        };
    }
}
