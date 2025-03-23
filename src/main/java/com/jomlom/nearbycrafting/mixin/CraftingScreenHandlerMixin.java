package com.jomlom.nearbycrafting.mixin;

import com.jomlom.nearbycrafting.util.NearbyCraftingConfig;
import com.jomlom.recipebookaccess.api.RecipeBookInventoryProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;

@Mixin(CraftingScreenHandler.class)
public abstract class CraftingScreenHandlerMixin implements RecipeBookInventoryProvider {

    @Shadow @Final private ScreenHandlerContext context;

    @Shadow @Final private PlayerEntity player;

    @Override
    public List<Inventory> getInventoriesForAutofill() {
        if (!NearbyCraftingConfig.craftingTableCanReach){ return List.of(player.getInventory()); }
        List<Inventory> inventories = new ArrayList<>();
        context.run((world, pos) -> {
            int radius = NearbyCraftingConfig.craftingTableReach;
            BlockPos.stream(pos.add(-radius, -radius, -radius), pos.add(radius, radius, radius))
                    .forEach(currentPos -> {
                        if ((currentPos != pos) && (world.getBlockEntity(currentPos) instanceof Inventory inventory)) {
                            inventories.add(inventory);
                        }
                    });
            inventories.add(player.getInventory());
        });
        return inventories;
    }
}
