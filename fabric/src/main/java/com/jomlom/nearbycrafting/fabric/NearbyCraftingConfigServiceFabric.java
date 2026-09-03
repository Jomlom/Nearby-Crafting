package com.jomlom.nearbycrafting.fabric;

import com.jomlom.nearbycrafting.platform.NearbyCraftingConfigService;

import java.util.HashMap;
import java.util.Map;

public class NearbyCraftingConfigServiceFabric implements NearbyCraftingConfigService {

    @Override
    public boolean craftingPlayerCanReach() {
        return NearbyCraftingConfigFabric.craftingPlayerCanReach;
    }

    @Override
    public void setCraftingPlayerCanReach(boolean value) {
        NearbyCraftingConfigFabric.craftingPlayerCanReach = value;
    }

    @Override
    public int craftingPlayerReach() {
        return NearbyCraftingConfigFabric.craftingPlayerReach;
    }

    @Override
    public void setCraftingPlayerReach(int value) {
        NearbyCraftingConfigFabric.craftingPlayerReach = value;
    }

    @Override
    public boolean craftingTableCanReach() {
        return NearbyCraftingConfigFabric.craftingTableCanReach;
    }

    @Override
    public void setCraftingTableCanReach(boolean value) {
        NearbyCraftingConfigFabric.craftingTableCanReach = value;
    }

    @Override
    public int craftingTableReach() {
        return NearbyCraftingConfigFabric.craftingTableReach;
    }

    @Override
    public void setCraftingTableReach(int value) {
        NearbyCraftingConfigFabric.craftingTableReach = value;
    }

    @Override
    public boolean isContainerBlockEnabled(String namespace, String blockId) {
        return NearbyCraftingConfigFabric.containerBlockToggles
                .getOrDefault(namespace, Map.of())
                .getOrDefault(blockId, true);
    }

    @Override
    public void setContainerBlockEnabled(String namespace, String blockId, boolean enabled) {
        NearbyCraftingConfigFabric.containerBlockToggles
                .computeIfAbsent(namespace, ns -> new HashMap<>())
                .put(blockId, enabled);
    }

    @Override
    public Map<String, Map<String, Boolean>> containerBlockToggles() {
        return NearbyCraftingConfigFabric.containerBlockToggles;
    }

    @Override
    public void save() {
        NearbyCraftingConfigFabric.HANDLER.save();
    }
}
