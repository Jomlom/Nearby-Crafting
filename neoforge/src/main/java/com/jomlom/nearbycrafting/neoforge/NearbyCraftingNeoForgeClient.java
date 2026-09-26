package com.jomlom.nearbycrafting.neoforge;

import com.jomlom.nearbycrafting.NearbyCraftingCommon;
import com.jomlom.nearbycrafting.client.PriorityState;
import com.jomlom.nearbycrafting.clientUtil.NearbyCraftingConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@Mod(value = NearbyCraftingCommon.MOD_ID, dist = Dist.CLIENT)
public class NearbyCraftingNeoForgeClient {

    public NearbyCraftingNeoForgeClient(ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (container, parent) -> new NearbyCraftingConfigScreen(parent));
        PriorityState.sender = payload -> {
            try {
                ClientPacketDistributor.sendToServer(payload);
            } catch (RuntimeException ignored) {
            }
        };
    }
}
