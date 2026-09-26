package com.jomlom.nearbycrafting.mixin.client;

import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RecipeBookComponent.class)
public interface RecipeBookComponentAccessor {

    @Accessor("recipeBookPage")
    RecipeBookPage nearbycrafting$getRecipeBookPage();

    @Accessor("lastRecipe")
    RecipeDisplayId nearbycrafting$getLastRecipe();

    @Accessor("lastRecipeCollection")
    RecipeCollection nearbycrafting$getLastRecipeCollection();
}
