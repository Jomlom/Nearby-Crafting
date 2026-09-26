package com.jomlom.nearbycrafting.neoforge;

import com.jomlom.nearbycrafting.NearbyCraftingCommon;
import com.jomlom.nearbycrafting.client.PriorityState;
import com.jomlom.nearbycrafting.clientUtil.NearbyCraftingConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.lang.reflect.Method;

@Mod(value = NearbyCraftingCommon.MOD_ID, dist = Dist.CLIENT)
public class NearbyCraftingNeoForgeClient {

    public NearbyCraftingNeoForgeClient(ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (container, parent) -> new NearbyCraftingConfigScreen(parent));
        Method send = resolveSend();
        PriorityState.sender = payload -> {
            try {
                send.invoke(null, payload, new CustomPacketPayload[0]);
            } catch (ReflectiveOperationException | RuntimeException ignored) {
            }
        };
    }

    private static Method resolveSend() {
        for (String name : new String[]{"net.neoforged.neoforge.client.network.ClientPacketDistributor", "net.neoforged.neoforge.network.PacketDistributor"}) {
            try {
                for (Method method : Class.forName(name).getMethods()) {
                    if (method.getName().equals("sendToServer") && method.isVarArgs()) return method;
                }
            } catch (ClassNotFoundException ignored) {
            }
        }
        return null;
    }
}
