package com.jomlom.nearbycrafting.client;

import com.jomlom.nearbycrafting.network.PriorityPayload;

import java.util.function.Consumer;

public class PriorityState {

    public static Consumer<PriorityPayload> sender = payload -> {};

    private static boolean inventoryFirst = true;

    public static boolean isInventoryFirst() {
        return inventoryFirst;
    }

    public static void toggle() {
        inventoryFirst = !inventoryFirst;
        sync();
    }

    public static void sync() {
        sender.accept(new PriorityPayload(inventoryFirst));
    }
}
