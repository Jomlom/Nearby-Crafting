package com.jomlom.nearbycrafting.mixin.client;

import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(InventoryScreen.class)
public interface InventoryScreenAccessor {

    @Accessor("recipeBookComponent")
    RecipeBookComponent nearbycrafting$getRecipeBookComponent();
}
