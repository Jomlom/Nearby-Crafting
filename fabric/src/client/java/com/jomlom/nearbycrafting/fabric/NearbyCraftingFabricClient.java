package com.jomlom.nearbycrafting.fabric;

import com.jomlom.nearbycrafting.client.PriorityState;
import com.jomlom.nearbycrafting.network.PriorityPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class NearbyCraftingFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		PriorityState.sender = payload -> {
			if (ClientPlayNetworking.canSend(PriorityPayload.ID)) {
				ClientPlayNetworking.send(payload);
			}
		};
	}
}
