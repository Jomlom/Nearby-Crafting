package com.jomlom.nearbycrafting.platform;

import java.util.Map;

public interface NearbyCraftingConfigService {

    boolean craftingPlayerCanReach();
    void setCraftingPlayerCanReach(boolean value);

    int craftingPlayerReach();
    void setCraftingPlayerReach(int value);

    boolean craftingTableCanReach();
    void setCraftingTableCanReach(boolean value);

    int craftingTableReach();
    void setCraftingTableReach(int value);

    boolean isContainerBlockEnabled(String namespace, String blockId);
    void setContainerBlockEnabled(String namespace, String blockId, boolean enabled);

    Map<String, Map<String, Boolean>> containerBlockToggles();

    void save();
}
