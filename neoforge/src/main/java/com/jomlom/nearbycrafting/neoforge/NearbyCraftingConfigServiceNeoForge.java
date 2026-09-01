package com.jomlom.nearbycrafting.neoforge;

import com.jomlom.nearbycrafting.platform.NearbyCraftingConfigService;

import java.util.HashMap;
import java.util.Map;

public class NearbyCraftingConfigServiceNeoForge implements NearbyCraftingConfigService {

    private final Map<String, Map<String, Boolean>> containerBlockToggles = ContainerBlockTogglesStorage.load();

    @Override
    public boolean craftingPlayerCanReach() {
        return NearbyCraftingConfigNeoForge.CRAFTING_PLAYER_CAN_REACH.get();
    }

    @Override
    public void setCraftingPlayerCanReach(boolean value) {
        NearbyCraftingConfigNeoForge.CRAFTING_PLAYER_CAN_REACH.set(value);
    }

    @Override
    public int craftingPlayerReach() {
        return NearbyCraftingConfigNeoForge.CRAFTING_PLAYER_REACH.get();
    }

    @Override
    public void setCraftingPlayerReach(int value) {
        NearbyCraftingConfigNeoForge.CRAFTING_PLAYER_REACH.set(value);
    }

    @Override
    public boolean craftingTableCanReach() {
        return NearbyCraftingConfigNeoForge.CRAFTING_TABLE_CAN_REACH.get();
    }

    @Override
    public void setCraftingTableCanReach(boolean value) {
        NearbyCraftingConfigNeoForge.CRAFTING_TABLE_CAN_REACH.set(value);
    }

    @Override
    public int craftingTableReach() {
        return NearbyCraftingConfigNeoForge.CRAFTING_TABLE_REACH.get();
    }

    @Override
    public void setCraftingTableReach(int value) {
        NearbyCraftingConfigNeoForge.CRAFTING_TABLE_REACH.set(value);
    }

    @Override
    public boolean isContainerBlockEnabled(String namespace, String blockId) {
        return containerBlockToggles
                .getOrDefault(namespace, Map.of())
                .getOrDefault(blockId, true);
    }

    @Override
    public void setContainerBlockEnabled(String namespace, String blockId, boolean enabled) {
        containerBlockToggles
                .computeIfAbsent(namespace, ns -> new HashMap<>())
                .put(blockId, enabled);
    }

    @Override
    public Map<String, Map<String, Boolean>> containerBlockToggles() {
        return containerBlockToggles;
    }

    @Override
    public void save() {
        ContainerBlockTogglesStorage.save(containerBlockToggles);
    }
}
