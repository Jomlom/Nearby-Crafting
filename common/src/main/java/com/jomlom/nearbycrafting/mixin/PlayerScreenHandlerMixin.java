package com.jomlom.nearbycrafting.mixin;

import com.jomlom.nearbycrafting.platform.Services;
import com.jomlom.recipebookaccess.api.RecipeBookInventoryProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;

@Mixin(InventoryMenu.class)
public abstract class PlayerScreenHandlerMixin implements RecipeBookInventoryProvider {

    @Shadow @Final private Player owner;

    @Override
    public List<Container> getInventoriesForAutofill() {
        if (!Services.CONFIG.craftingPlayerCanReach()) { return List.of(owner.getInventory()); }
        Level world = owner.level();
        BlockPos playerPos = owner.blockPosition();
        List<Container> inventories = new ArrayList<>();
        int radius = Services.CONFIG.craftingPlayerReach();
        BlockPos.betweenClosedStream(playerPos.offset(-radius, -radius, -radius), playerPos.offset(radius, radius, radius))
                .forEach(currentPos -> {
                    if (world.getBlockEntity(currentPos) instanceof Container inventory) {
                        inventories.add(inventory);
                    }
                });
        inventories.add(owner.getInventory());
        return inventories;
    }
}
