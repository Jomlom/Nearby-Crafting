package com.jomlom.nearbycrafting.mixin;

import com.jomlom.nearbycrafting.container.NearbyContainers;
import com.jomlom.nearbycrafting.platform.Services;
import com.jomlom.recipebookaccess.api.RecipeBookInventoryProvider;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;
import vectorwing.farmersdelight.common.block.entity.container.CookingPotMenu;

import java.util.List;

@Mixin(CookingPotMenu.class)
public abstract class CookingPotMenuMixin implements RecipeBookInventoryProvider {

    @Shadow @Final public CookingPotBlockEntity blockEntity;

    // ingredient grid is slots 0-5, meal display 6, bowl input 7, bowl output 8, player inventory 9+
    private static final int PLAYER_INVENTORY_START = 9;
    private static final int INDEX_MEAL = 6;

    @Override
    public List<Container> getInventoriesForAutofill() {
        Container playerInventory = ((AbstractContainerMenu) (Object) this).getSlot(PLAYER_INVENTORY_START).container;
        Level world = blockEntity.getLevel();

        if (!Services.CONFIG.craftingTableCanReach() || world == null) {
            return List.of(playerInventory);
        }

        return NearbyContainers.forCraftingTable(((Inventory) playerInventory).player, world, blockEntity.getBlockPos());
    }

    @Override
    public boolean isActive() {
        return Services.CONFIG.craftingTableCanReach();
    }

    @Override
    public boolean persistentInventory() {
        return true;
    }

    @Override
    public int inputSlotsStartIndex() {
        return 0;
    }

    @Override
    public int inputSlotsEndIndex() {
        return INDEX_MEAL;
    }
}
