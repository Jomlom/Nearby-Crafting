package com.jomlom.nearbycrafting.neoforge;

import com.jomlom.nearbycrafting.NearbyCraftingCommon;
import com.jomlom.nearbycrafting.network.PriorityPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

@Mod(NearbyCraftingCommon.MOD_ID)
public class NearbyCraftingNeoForge {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(NearbyCraftingCommon.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            version -> true,
            version -> true
    );

    public NearbyCraftingNeoForge() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, NearbyCraftingConfigNeoForge.SPEC);
        CHANNEL.registerMessage(0, PriorityPayload.class, PriorityPayload::encode, PriorityPayload::decode, this::handlePriority);
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
        MinecraftForge.EVENT_BUS.addListener(this::serverAboutToStart);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> NearbyCraftingNeoForgeClient::init);
    }

    private void handlePriority(PriorityPayload payload, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                NearbyCraftingCommon.setInventoryFirst(player, payload.inventoryFirst());
            }
        });
        ctx.setPacketHandled(true);
    }

    private void serverAboutToStart(ServerAboutToStartEvent event) {
        NearbyCraftingCommon.detectContainerBlocks();
    }

    private void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(NearbyCraftingCommon.buildCommand());
    }
}
