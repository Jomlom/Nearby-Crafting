package com.jomlom.nearbycrafting.mixin;

import com.jomlom.nearbycrafting.platform.Services;
import com.jomlom.recipebookaccess.api.RecipeBookInventoryProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;
import vectorwing.farmersdelight.common.block.entity.container.CookingPotMenu;

import java.util.ArrayList;
import java.util.List;

@Mixin(CookingPotMenu.class)
public abstract class CookingPotMenuMixin implements RecipeBookInventoryProvider {

    @Shadow @Final public CookingPotBlockEntity blockEntity;

    // ingredient grid is slots 0-5, meal display 6, bowl input 7, bowl output 8, player inventory 9+
    private static final int PLAYER_INVENTORY_START = 9;

    @Override
    public List<Container> getInventoriesForAutofill() {
        Container playerInventory = ((AbstractContainerMenu) (Object) this).getSlot(PLAYER_INVENTORY_START).container;

        if (!Services.CONFIG.craftingTableCanReach()) {
            return List.of(playerInventory);
        }

        List<Container> inventories = new ArrayList<>();
        Level world = blockEntity.getLevel();
        BlockPos pos = blockEntity.getBlockPos();

        if (world != null) {
            int radius = Services.CONFIG.craftingTableReach();

            BlockPos.betweenClosedStream(pos.offset(-radius, -radius, -radius), pos.offset(radius, radius, radius))
                    .forEach(currentPos -> {
                        if (currentPos.equals(pos)) return;

                        BlockEntity nearbyEntity = world.getBlockEntity(currentPos);
                        if (nearbyEntity instanceof Container inventory) {
                            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(nearbyEntity.getBlockState().getBlock());
                            if (isBlockEnabled(blockId)) {
                                inventories.add(inventory);
                            }
                        }
                    });
        }

        inventories.add(playerInventory);
        return inventories;
    }

    @Unique
    private boolean isBlockEnabled(ResourceLocation blockId) {
        return Services.CONFIG.isContainerBlockEnabled(blockId.getNamespace(), blockId.toString());
    }

    @Override
    public boolean persistentInventory() {
        return true;
    }
}
