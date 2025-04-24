package com.jomlom.nearbycrafting.mixin;

import com.jomlom.nearbycrafting.util.NearbyCraftingConfig;
import com.jomlom.recipebookaccess.api.RecipeBookInventoryProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;

@Mixin(PlayerScreenHandler.class)
public abstract class PlayerScreenHandlerMixin implements RecipeBookInventoryProvider {

    @Shadow @Final private PlayerEntity owner;
    @Shadow @Final public static int CRAFTING_INPUT_START;
    @Shadow @Final public static int CRAFTING_INPUT_END;

    @Override
    public List<Inventory> getInventoriesForAutofill() {
        if (!NearbyCraftingConfig.craftingPlayerCanReach) { return List.of(owner.getInventory()); }
        World world = owner.getWorld();
        BlockPos playerPos = owner.getBlockPos();
        List<Inventory> inventories = new ArrayList<>();
        int radius = NearbyCraftingConfig.craftingPlayerReach;
        BlockPos.stream(playerPos.add(-radius, -radius, -radius), playerPos.add(radius, radius, radius))
                .forEach(currentPos -> {
                    if (world.getBlockEntity(currentPos) instanceof Inventory inventory) {
                        inventories.add(inventory);
                    }
                });
        inventories.add(owner.getInventory());
        return inventories;
    }

    @Override
    public int inputSlotsStartIndex() {
        return CRAFTING_INPUT_START;
    }

    @Override
    public int inputSlotsEndIndex() {
        return CRAFTING_INPUT_END;
    }

}
