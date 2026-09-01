package com.jomlom.nearbycrafting.neoforge;

import com.jomlom.nearbycrafting.NearbyCraftingCommon;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(NearbyCraftingCommon.MOD_ID)
public class NearbyCraftingNeoForge {

    public NearbyCraftingNeoForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, NearbyCraftingConfigNeoForge.SPEC);
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        NearbyCraftingCommon.detectContainerBlocks();
    }

    private void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(NearbyCraftingCommon.buildCommand());
    }
}
