package com.jomlom.nearbycrafting.network;

import com.jomlom.nearbycrafting.NearbyCraftingCommon;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class PriorityPayload {

    public static final ResourceLocation ID = new ResourceLocation(NearbyCraftingCommon.MOD_ID, "priority");

    private final boolean inventoryFirst;

    public PriorityPayload(boolean inventoryFirst) {
        this.inventoryFirst = inventoryFirst;
    }

    public boolean inventoryFirst() {
        return inventoryFirst;
    }

    public static void encode(PriorityPayload payload, FriendlyByteBuf buf) {
        buf.writeBoolean(payload.inventoryFirst);
    }

    public static PriorityPayload decode(FriendlyByteBuf buf) {
        return new PriorityPayload(buf.readBoolean());
    }
}
