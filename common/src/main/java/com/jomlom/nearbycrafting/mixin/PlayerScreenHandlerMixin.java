package com.jomlom.nearbycrafting.mixin;

import com.jomlom.nearbycrafting.container.NearbyContainers;
import com.jomlom.nearbycrafting.platform.Services;
import com.jomlom.recipebookaccess.api.RecipeBookInventoryProvider;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(InventoryMenu.class)
public abstract class PlayerScreenHandlerMixin implements RecipeBookInventoryProvider {

    @Shadow @Final private Player owner;

    @Override
    public List<Container> getInventoriesForAutofill() {
        if (!Services.CONFIG.craftingPlayerCanReach()) {
            return List.of(owner.getInventory());
        }
        return NearbyContainers.forPlayer(owner);
    }

    @Override
    public boolean isActive() {
        return Services.CONFIG.craftingPlayerCanReach();
    }
}
