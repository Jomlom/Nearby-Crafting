package com.jomlom.nearbycrafting.mixin;

import com.jomlom.nearbycrafting.container.NearbyContainers;
import com.jomlom.nearbycrafting.platform.Services;
import com.jomlom.recipebookaccess.api.RecipeBookInventoryProvider;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;

@Mixin(CraftingMenu.class)
public abstract class CraftingScreenHandlerMixin implements RecipeBookInventoryProvider {

    @Shadow @Final private ContainerLevelAccess access;

    @Shadow @Final private Player player;

    @Override
    public List<Container> getInventoriesForAutofill() {
        if (!Services.CONFIG.craftingTableCanReach()) {
            return List.of(player.getInventory());
        }

        List<Container> inventories = new ArrayList<>();
        access.execute((world, pos) -> inventories.addAll(NearbyContainers.forCraftingTable(player, world, pos)));
        return inventories;
    }

    @Override
    public boolean isActive() {
        return Services.CONFIG.craftingTableCanReach();
    }
}
