package com.jomlom.nearbycrafting.container;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ShulkerBoxContainer implements Container {

    private static final int SIZE = 27;
    private static final String ITEMS_KEY = "Items";

    private final ItemStack box;

    public ShulkerBoxContainer(ItemStack box) {
        this.box = box;
    }

    public static boolean isShulkerBox(ItemStack stack) {
        return stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock;
    }

    private NonNullList<ItemStack> read() {
        NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        CompoundTag tag = BlockItem.getBlockEntityData(box);
        if (!box.isEmpty() && tag != null) {
            ContainerHelper.loadAllItems(tag, items);
        }
        return items;
    }

    private void write(NonNullList<ItemStack> items) {
        if (box.isEmpty()) {
            return;
        }
        CompoundTag existing = BlockItem.getBlockEntityData(box);
        CompoundTag tag = existing != null ? existing.copy() : new CompoundTag();
        tag.remove(ITEMS_KEY);
        ContainerHelper.saveAllItems(tag, items, false);
        BlockItem.setBlockEntityData(box, BlockEntityType.SHULKER_BOX, tag);
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        return read().stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return read().get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        NonNullList<ItemStack> items = read();
        ItemStack removed = ContainerHelper.removeItem(items, slot, count);
        if (!removed.isEmpty()) {
            write(items);
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        NonNullList<ItemStack> items = read();
        ItemStack removed = ContainerHelper.takeItem(items, slot);
        write(items);
        return removed;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        NonNullList<ItemStack> items = read();
        items.set(slot, stack);
        write(items);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.getItem().canFitInsideContainerItems();
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return !box.isEmpty();
    }

    @Override
    public void clearContent() {
        write(NonNullList.withSize(SIZE, ItemStack.EMPTY));
    }
}
