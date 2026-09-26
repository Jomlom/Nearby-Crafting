package com.jomlom.nearbycrafting.neoforge;

import com.jomlom.nearbycrafting.NearbyCraftingCommon;
import com.jomlom.nearbycrafting.network.PriorityPayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(NearbyCraftingCommon.MOD_ID)
public class NearbyCraftingNeoForge {

    public NearbyCraftingNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, NearbyCraftingConfigNeoForge.SPEC);
        modEventBus.addListener(this::registerPayloads);
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
        NeoForge.EVENT_BUS.addListener(this::serverAboutToStart);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").optional().playToServer(PriorityPayload.ID, PriorityPayload.CODEC, (payload, context) ->
                context.enqueueWork(() -> NearbyCraftingCommon.setInventoryFirst(context.player(), payload.inventoryFirst())));
    }

    private void serverAboutToStart(ServerAboutToStartEvent event) {
        NearbyCraftingCommon.detectContainerBlocks();
    }

    private void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(NearbyCraftingCommon.buildCommand());
    }
}
