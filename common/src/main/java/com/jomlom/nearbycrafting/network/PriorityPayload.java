package com.jomlom.nearbycrafting.network;

import com.jomlom.nearbycrafting.NearbyCraftingCommon;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PriorityPayload(boolean inventoryFirst) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PriorityPayload> ID =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(NearbyCraftingCommon.MOD_ID, "priority"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PriorityPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, PriorityPayload::inventoryFirst,
                    PriorityPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
