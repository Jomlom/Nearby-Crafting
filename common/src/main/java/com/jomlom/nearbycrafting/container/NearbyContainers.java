package com.jomlom.nearbycrafting.container;

import com.jomlom.nearbycrafting.NearbyCraftingCommon;
import com.jomlom.nearbycrafting.mixin.AbstractHorseAccessor;
import com.jomlom.nearbycrafting.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.AbstractChestedHorse;
import net.minecraft.world.entity.animal.equine.Donkey;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.animal.equine.Mule;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.entity.vehicle.boat.AbstractChestBoat;
import net.minecraft.world.entity.vehicle.boat.ChestRaft;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecartContainer;
import net.minecraft.world.entity.vehicle.minecart.MinecartHopper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class NearbyContainers {

    public static final String NAMESPACE = "minecraft";

    public static final String INVENTORY_GROUP = "inventory_containers";
    public static final String ENTITY_GROUP = "entities";

    public static final String SHULKER_BOXES = "shulker_boxes";
    public static final String BUNDLES = "bundles";
    public static final String BOAT_CHESTS = "boat_chests";
    public static final String RAFT_CHESTS = "raft_chests";
    public static final String CHEST_MINECARTS = "chest_minecarts";
    public static final String HOPPER_MINECARTS = "hopper_minecarts";
    public static final String DONKEY_CHESTS = "donkey_chests";
    public static final String MULE_CHESTS = "mule_chests";
    public static final String LLAMA_CHESTS = "llama_chests";
    public static final String OTHER_CONTAINERS = "other_containers";

    public static final List<String> INVENTORY_KEYS = List.of(SHULKER_BOXES, BUNDLES);
    public static final List<String> ENTITY_KEYS = List.of(BOAT_CHESTS, RAFT_CHESTS, CHEST_MINECARTS, HOPPER_MINECARTS, DONKEY_CHESTS, MULE_CHESTS, LLAMA_CHESTS, OTHER_CONTAINERS);

    public static String toggleId(String key) {
        return NAMESPACE + ":" + key;
    }

    public static List<String> toggleIds(List<String> keys) {
        return keys.stream().map(NearbyContainers::toggleId).toList();
    }

    public static boolean isToggleId(String blockId) {
        return toggleIds(INVENTORY_KEYS).contains(blockId) || toggleIds(ENTITY_KEYS).contains(blockId);
    }

    public static List<Container> assemble(Player player, Container inventory, List<Container> blocks, Level level, BlockPos center, int radius) {
        List<Container> nearby = new ArrayList<>(blocks);
        AABB area = new AABB(center).inflate(radius);
        for (Entity entity : level.getEntitiesOfClass(Entity.class, area, NearbyContainers::isEnabledContainerEntity)) {
            nearby.add(entity instanceof AbstractChestedHorse horse ? ((AbstractHorseAccessor) horse).nearbycrafting$getInventory() : (Container) entity);
        }

        List<Container> own = withInventoryContainers(inventory);
        List<Container> containers = new ArrayList<>();
        if (NearbyCraftingCommon.isInventoryFirst(player)) {
            containers.addAll(own);
            containers.addAll(nearby);
        } else {
            containers.addAll(nearby);
            containers.addAll(own);
        }
        return containers;
    }

    private static List<Container> withInventoryContainers(Container inventory) {
        boolean shulkers = isEnabled(SHULKER_BOXES);
        boolean bundles = isEnabled(BUNDLES);

        List<Container> containers = new ArrayList<>();
        containers.add(inventory);
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (shulkers && ShulkerBoxContainer.isShulkerBox(stack)) {
                containers.add(new ShulkerBoxContainer(stack));
            } else if (bundles && BundleContainer.isBundle(stack)) {
                containers.add(BundleContainer.of(stack));
            }
        }
        return containers;
    }

    private static boolean isEnabledContainerEntity(Entity entity) {
        String key = entityKey(entity);
        return key != null && isEnabled(key);
    }

    private static boolean isEnabled(String key) {
        return Services.CONFIG.isContainerBlockEnabled(NAMESPACE, toggleId(key));
    }

    private static @Nullable String entityKey(Entity entity) {
        if (entity instanceof ChestRaft) {
            return RAFT_CHESTS;
        }
        if (entity instanceof AbstractChestBoat) {
            return BOAT_CHESTS;
        }
        if (entity instanceof MinecartHopper) {
            return HOPPER_MINECARTS;
        }
        if (entity instanceof AbstractMinecartContainer) {
            return CHEST_MINECARTS;
        }
        if (entity instanceof AbstractChestedHorse horse) {
            return horse.hasChest() ? mountKey(horse) : null;
        }
        return entity instanceof ContainerEntity ? OTHER_CONTAINERS : null;
    }

    private static String mountKey(AbstractChestedHorse horse) {
        if (horse instanceof Donkey) {
            return DONKEY_CHESTS;
        }
        if (horse instanceof Mule) {
            return MULE_CHESTS;
        }
        return horse instanceof Llama ? LLAMA_CHESTS : OTHER_CONTAINERS;
    }
}
