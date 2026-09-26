package com.jomlom.nearbycrafting.platform;

import com.jomlom.nearbycrafting.CraftingMode;

import java.util.Map;

public interface NearbyCraftingConfigService {

    CraftingMode mode();
    void setMode(CraftingMode value);

    boolean craftingPlayerCanReach();
    void setCraftingPlayerCanReach(boolean value);

    int craftingPlayerReach();
    void setCraftingPlayerReach(int value);

    boolean craftingTableCanReach();
    void setCraftingTableCanReach(boolean value);

    int craftingTableReach();
    void setCraftingTableReach(int value);

    int craftingTableDepth();
    void setCraftingTableDepth(int value);

    boolean isContainerBlockEnabled(String namespace, String blockId);
    void setContainerBlockEnabled(String namespace, String blockId, boolean enabled);

    Map<String, Map<String, Boolean>> containerBlockToggles();

    void save();
}
