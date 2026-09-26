package com.jomlom.nearbycrafting.fabric;

import com.jomlom.nearbycrafting.client.PriorityState;
import com.jomlom.nearbycrafting.network.PriorityPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;

public class NearbyCraftingFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		PriorityState.sender = payload -> {
			if (ClientPlayNetworking.canSend(PriorityPayload.ID)) {
				FriendlyByteBuf buf = PacketByteBufs.create();
				PriorityPayload.encode(payload, buf);
				ClientPlayNetworking.send(PriorityPayload.ID, buf);
			}
		};
	}
}
