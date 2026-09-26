package com.jomlom.nearbycrafting.mixin.client;

import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RecipeBookPage.class)
public interface RecipeBookPageAccessor {

    @Accessor("hoveredButton")
    RecipeButton nearbycrafting$getHoveredButton();
}
