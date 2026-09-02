package com.jomlom.nearbycrafting.mixin;

import com.jomlom.nearbycrafting.platform.Services;
import com.jomlom.recipebookaccess.api.RecipeBookInventoryProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

@Mixin(InventoryMenu.class)
public abstract class PlayerScreenHandlerMixin implements RecipeBookInventoryProvider {

    @Shadow @Final private Player owner;
    @Shadow @Final public static int CRAFT_SLOT_START;
    @Shadow @Final public static int CRAFT_SLOT_END;

    @Override
    public List<Container> getInventoriesForAutofill() {
        if (!Services.CONFIG.craftingPlayerCanReach()) { return List.of(owner.getInventory()); }
        Level world = owner.level();
        BlockPos playerPos = owner.blockPosition();
        List<Container> inventories = new ArrayList<>();
        int radius = Services.CONFIG.craftingPlayerReach();
        BlockPos.betweenClosedStream(playerPos.offset(-radius, -radius, -radius), playerPos.offset(radius, radius, radius))
                .forEach(currentPos -> {
                    BlockEntity blockEntity = world.getBlockEntity(currentPos);
                    if (blockEntity instanceof Container inventory) {
                        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(blockEntity.getBlockState().getBlock());
                        if (isBlockEnabled(blockId)) {
                            inventories.add(inventory);
                        }
                    }
                });
        inventories.add(owner.getInventory());
        return inventories;
    }

    @Unique
    private boolean isBlockEnabled(ResourceLocation blockId) {
        return Services.CONFIG.isContainerBlockEnabled(blockId.getNamespace(), blockId.toString());
    }

    @Override
    public int inputSlotsStartIndex() {
        return CRAFT_SLOT_START;
    }

    @Override
    public int inputSlotsEndIndex() {
        return CRAFT_SLOT_END;
    }
}
