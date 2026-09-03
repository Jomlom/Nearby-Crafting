package com.jomlom.nearbycrafting.mixin;

import com.jomlom.nearbycrafting.platform.Services;
import com.jomlom.recipebookaccess.api.RecipeBookInventoryProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.Collections;
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

        access.execute((world, pos) -> {
            int radius = Services.CONFIG.craftingTableReach();

            BlockPos.betweenClosedStream(pos.offset(-radius, -radius, -radius), pos.offset(radius, radius, radius))
                    .forEach(currentPos -> {
                        if (currentPos.equals(pos)) return;

                        BlockEntity blockEntity = world.getBlockEntity(currentPos);
                        if (blockEntity instanceof Container inventory) {
                            Identifier blockId = BuiltInRegistries.BLOCK.getKey(blockEntity.getBlockState().getBlock());
                            if (isBlockEnabled(blockId)) {
                                inventories.add(inventory);
                            }
                        }
                    });

            // Always add player's own inventory
            inventories.add(player.getInventory());
        });

        return inventories;
    }

    @Unique
    private boolean isBlockEnabled(Identifier blockId) {
        return Services.CONFIG.isContainerBlockEnabled(blockId.getNamespace(), blockId.toString());
    }

}
