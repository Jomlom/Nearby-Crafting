package com.jomlom.nearbycrafting.fabric;

import com.jomlom.nearbycrafting.NearbyCraftingCommon;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class NearbyCraftingFabric implements ModInitializer {

	@Override
	public void onInitialize() {
		NearbyCraftingConfigFabric.HANDLER.load();
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(NearbyCraftingCommon.buildCommand()));
		NearbyCraftingCommon.detectContainerBlocks();
	}
}
