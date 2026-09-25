package com.jomlom.nearbycrafting.container;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.BundleContents;
import org.apache.commons.lang3.math.Fraction;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class BundleContainer implements Container {

    private static final int SIZE = 64;
    private static final Map<ItemStack, BundleContainer> VIEWS = Collections.synchronizedMap(new WeakHashMap<>());

    private final WeakReference<ItemStack> bundle;
    private final NonNullList<ItemStack> slots = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private BundleContents synced;

    private BundleContainer(ItemStack bundle) {
        this.bundle = new WeakReference<>(bundle);
    }

    public static boolean isBundle(ItemStack stack) {
        return stack.has(DataComponents.BUNDLE_CONTENTS);
    }

    public static BundleContainer of(ItemStack bundle) {
        return VIEWS.computeIfAbsent(bundle, BundleContainer::new);
    }

    private BundleContents contents() {
        ItemStack stack = bundle.get();
        return stack == null || stack.isEmpty() ? BundleContents.EMPTY : stack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
    }

    private void refresh() {
        BundleContents current = contents();
        if (!current.equals(synced)) {
            load(current);
        }
    }

    private void load(BundleContents current) {
        slots.clear();
        int index = 0;
        for (ItemStackTemplate template : current.items()) {
            ItemStack stack = template.create();
            if (!stack.isEmpty()) {
                slots.set(index++, stack);
            }
        }
        synced = current;
    }

    private static BundleContents toContents(List<ItemStack> stacks) {
        List<ItemStackTemplate> templates = new ArrayList<>();
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                templates.add(ItemStackTemplate.fromNonEmptyStack(stack));
            }
        }
        return new BundleContents(templates);
    }

    private static boolean fits(List<ItemStack> stacks) {
        return toContents(stacks).weight().result().map(weight -> weight.compareTo(Fraction.ONE) <= 0).orElse(false);
    }

    private void write() {
        ItemStack stack = bundle.get();
        if (stack == null || stack.isEmpty()) {
            return;
        }
        BundleContents contents = toContents(slots);
        stack.set(DataComponents.BUNDLE_CONTENTS, contents);
        synced = contents;
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        refresh();
        return slots.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        refresh();
        return slots.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        refresh();
        ItemStack removed = ContainerHelper.removeItem(slots, slot, count);
        if (!removed.isEmpty()) {
            write();
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        refresh();
        ItemStack removed = ContainerHelper.takeItem(slots, slot);
        write();
        return removed;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        refresh();
        slots.set(slot, stack);
        if (fits(slots)) {
            write();
        } else {
            load(contents());
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        refresh();
        if (!BundleContents.canItemBeInBundle(stack)) {
            return false;
        }
        List<ItemStack> trial = new ArrayList<>(slots);
        ItemStack existing = trial.get(slot);
        trial.set(slot, existing.isEmpty() ? stack : existing.copyWithCount(existing.getCount() + stack.getCount()));
        return fits(trial);
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return bundle.get() != null;
    }

    @Override
    public void clearContent() {
        refresh();
        slots.clear();
        write();
    }
}
