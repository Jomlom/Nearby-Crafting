package com.jomlom.nearbycrafting.neoforge;

import com.jomlom.nearbycrafting.NearbyCraftingCommon;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

@Mod(NearbyCraftingCommon.MOD_ID)
public class NearbyCraftingNeoForge {

    public NearbyCraftingNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, NearbyCraftingConfigNeoForge.SPEC);
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
        NeoForge.EVENT_BUS.addListener(this::serverAboutToStart);
    }

    private void serverAboutToStart(ServerAboutToStartEvent event) {
        NearbyCraftingCommon.detectContainerBlocks();
    }

    private void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(NearbyCraftingCommon.buildCommand());
    }
}
