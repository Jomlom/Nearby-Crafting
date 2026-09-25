package com.jomlom.nearbycrafting.mixin.client;

import com.jomlom.nearbycrafting.client.NearbyItemsPanel;
import com.jomlom.nearbycrafting.client.PriorityState;
import com.jomlom.recipebookaccess.api.RecipeBookInventoryProvider;
import com.jomlom.recipebookaccess.network.ClientItemsReciever;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.RecipeBookMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractRecipeBookScreen.class)
public abstract class AbstractRecipeBookScreenMixin<T extends RecipeBookMenu> extends AbstractContainerScreen<T> {

    @Shadow @Final private RecipeBookComponent<?> recipeBookComponent;

    @Unique private Button nearbycrafting$priorityButton;

    protected AbstractRecipeBookScreenMixin(T menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void nearbycrafting$initPriorityButton(CallbackInfo ci) {
        if (!(this.menu instanceof RecipeBookInventoryProvider)) {
            this.nearbycrafting$priorityButton = null;
            return;
        }
        this.nearbycrafting$priorityButton = Button.builder(Component.empty(), button -> {
            PriorityState.toggle();
            this.nearbycrafting$refreshPriorityButton();
        }).size(NearbyItemsPanel.BUTTON_SIZE, NearbyItemsPanel.BUTTON_SIZE).build();
        this.nearbycrafting$refreshPriorityButton();
        this.addWidget(this.nearbycrafting$priorityButton);
        PriorityState.sync();
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void nearbycrafting$extractNearbyItems(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        if (this.nearbycrafting$priorityButton == null) {
            return;
        }
        boolean show = ClientItemsReciever.isActive() && this.recipeBookComponent.isVisible();
        this.nearbycrafting$priorityButton.visible = show;
        if (show) {
            NearbyItemsPanel.extract(graphics, this.font, this.minecraft.player.getInventory(), this.leftPos, this.topPos,
                    NearbyItemsPanel.requirements(this.recipeBookComponent, this.menu));
            this.nearbycrafting$priorityButton.setPosition(NearbyItemsPanel.buttonX(this.leftPos), NearbyItemsPanel.buttonY(this.topPos));
            this.nearbycrafting$priorityButton.extractRenderState(graphics, mouseX, mouseY, a);
        }
    }

    @Unique
    private void nearbycrafting$refreshPriorityButton() {
        boolean inventoryFirst = PriorityState.isInventoryFirst();
        this.nearbycrafting$priorityButton.setMessage(Component.translatable(inventoryFirst ? "nearbycrafting.priority.inventory.short" : "nearbycrafting.priority.nearby.short"));
        this.nearbycrafting$priorityButton.setTooltip(Tooltip.create(Component.empty()
                .append(Component.translatable(inventoryFirst ? "nearbycrafting.priority.inventory" : "nearbycrafting.priority.nearby"))
                .append("\n")
                .append(Component.translatable("nearbycrafting.priority.hint"))));
    }
}
