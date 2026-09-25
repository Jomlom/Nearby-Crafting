package com.jomlom.nearbycrafting.fabric;

import com.jomlom.nearbycrafting.NearbyCraftingCommon;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class NearbyCraftingFabric implements ModInitializer {

	@Override
	public void onInitialize() {
		NearbyCraftingConfigFabric.load();
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(NearbyCraftingCommon.buildCommand()));
		ServerLifecycleEvents.SERVER_STARTING.register(server -> NearbyCraftingCommon.detectContainerBlocks());
	}
}
