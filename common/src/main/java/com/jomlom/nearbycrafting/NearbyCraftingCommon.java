package com.jomlom.nearbycrafting;

import com.jomlom.nearbycrafting.platform.Services;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class NearbyCraftingCommon {

    public static final String MOD_ID = "nearbycrafting";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void detectContainerBlocks() {
        for (BlockEntityType<?> beType : BuiltInRegistries.BLOCK_ENTITY_TYPE) {
            Identifier beId = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(beType);
            if (beId == null) continue;

            BlockState validState = null;

            for (Block block : BuiltInRegistries.BLOCK) {
                if (block instanceof BaseEntityBlock blockWE) {
                    BlockEntity be = null;
                    try {
                        be = blockWE.newBlockEntity(net.minecraft.core.BlockPos.ZERO, block.defaultBlockState());
                    } catch (Exception ignored) {}

                    if (be != null && be.getType() == beType) {
                        validState = block.defaultBlockState();
                        break;
                    }
                }
            }

            if (validState == null) {
                continue;
            }

            BlockEntity blockEntity;
            try {
                blockEntity = beType.create(net.minecraft.core.BlockPos.ZERO, validState);
            } catch (Exception e) {
                continue;
            }

            if (!(blockEntity instanceof Container)) continue;

            for (Block block : BuiltInRegistries.BLOCK) {
                if (block instanceof BaseEntityBlock blockWE) {
                    BlockEntity be = null;
                    try {
                        be = blockWE.newBlockEntity(net.minecraft.core.BlockPos.ZERO, block.defaultBlockState());
                    } catch (Exception ignored) {}

                    if (be != null && be.getType() == beType) {
                        Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
                        String namespace = blockId.getNamespace();
                        String blockIdStr = blockId.toString();

                        Map<String, Map<String, Boolean>> toggles = Services.CONFIG.containerBlockToggles();
                        toggles.putIfAbsent(namespace, new HashMap<>());
                        toggles.get(namespace).putIfAbsent(blockIdStr, true);
                    }
                }
            }
        }
    }

    public static final SuggestionProvider<CommandSourceStack> SUGGEST_CONTAINER_BLOCKS = (context, builder) -> {
        for (Identifier blockId : BuiltInRegistries.BLOCK.keySet()) {
            if (Services.CONFIG.containerBlockToggles()
                    .getOrDefault(blockId.getNamespace(), Collections.emptyMap())
                    .containsKey(blockId.toString())) {
                builder.suggest(blockId.toString());
            }
        }
        return builder.buildFuture();
    };

    public static LiteralArgumentBuilder<CommandSourceStack> buildCommand() {
        return Commands.literal("nearbycrafting")

                // Operator permission
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))

                // Crafting Table subcommand
                .then(Commands.literal("craftingTable")
                        .then(Commands.literal("enable")
                                .executes(context -> setCraftingTableEnabled(context, true)))
                        .then(Commands.literal("disable")
                                .executes(context -> setCraftingTableEnabled(context, false)))
                        .then(Commands.literal("setReach")
                                .then(Commands.argument("radius", IntegerArgumentType.integer(0, 50))
                                        .executes(context -> setCraftingTableReach(context, IntegerArgumentType.getInteger(context, "radius")))))
                        .then(Commands.literal("getReach")
                                .executes(NearbyCraftingCommon::getCraftingTableReach))
                )

                // Player Inventory Crafting subcommand
                .then(Commands.literal("playerInventoryCrafting")
                        .then(Commands.literal("enable")
                                .executes(context -> setPlayerInventoryEnabled(context, true)))
                        .then(Commands.literal("disable")
                                .executes(context -> setPlayerInventoryEnabled(context, false)))
                        .then(Commands.literal("setReach")
                                .then(Commands.argument("radius", IntegerArgumentType.integer(0, 50))
                                        .executes(context -> setPlayerInventoryReach(context, IntegerArgumentType.getInteger(context, "radius")))))
                        .then(Commands.literal("getReach")
                                .executes(NearbyCraftingCommon::getPlayerInventoryReach))
                )

                // CONTAINERS subcommand for toggling blocks accessibility individually
                .then(Commands.literal("CONTAINERS")

                        .then(Commands.literal("enable")
                                .then(Commands.argument("block", IdentifierArgument.id())
                                        .suggests(SUGGEST_CONTAINER_BLOCKS)
                                        .executes(context -> setContainerBlockEnabled(context, true))))

                        .then(Commands.literal("disable")
                                .then(Commands.argument("block", IdentifierArgument.id())
                                        .suggests(SUGGEST_CONTAINER_BLOCKS)
                                        .executes(context -> setContainerBlockEnabled(context, false))))

                        .then(Commands.literal("get")
                                .then(Commands.argument("block", IdentifierArgument.id())
                                        .suggests(SUGGEST_CONTAINER_BLOCKS)
                                        .executes(NearbyCraftingCommon::getContainerBlockStatus)))

                        .then(Commands.literal("list")
                                .executes(NearbyCraftingCommon::listContainerBlocks))
                );
    }

    private static int setContainerBlockEnabled(CommandContext<CommandSourceStack> context, boolean enabled) throws CommandSyntaxException {
        Identifier blockId = IdentifierArgument.getId(context, "block");

        Services.CONFIG.setContainerBlockEnabled(blockId.getNamespace(), blockId.toString(), enabled);

        context.getSource().sendSuccess(() ->
                Component.literal("Container block " + blockId + " set to " + (enabled ? "enabled" : "disabled")), true);
        return 1;
    }

    private static int listContainerBlocks(CommandContext<CommandSourceStack> context) {
        StringBuilder sb = new StringBuilder("Container blocks and their enabled states:\n");

        Services.CONFIG.containerBlockToggles().forEach((namespace, map) -> {
            map.forEach((blockId, enabled) -> {
                sb.append(blockId).append(" : ").append(enabled ? "Enabled" : "Disabled").append("\n");
            });
        });

        context.getSource().sendSuccess(() ->
                Component.literal(sb.toString()), false);
        return 1;
    }

    private static int getContainerBlockStatus(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Identifier blockId = IdentifierArgument.getId(context, "block");
        boolean enabled = Services.CONFIG.isContainerBlockEnabled(blockId.getNamespace(), blockId.toString());

        context.getSource().sendSuccess(() ->
                Component.literal("Container block " + blockId + " is " + (enabled ? "enabled" : "disabled")), false);

        return 1;
    }

    private static int setCraftingTableEnabled(CommandContext<CommandSourceStack> context, boolean enabled) {
        Services.CONFIG.setCraftingTableCanReach(enabled);
        Services.CONFIG.save();
        context.getSource().sendSuccess(() ->
                Component.literal("Crafting Table reach enabled: " + enabled), true);
        return 1;
    }

    private static int setCraftingTableReach(CommandContext<CommandSourceStack> context, int radius) {
        Services.CONFIG.setCraftingTableReach(radius);
        Services.CONFIG.save();
        context.getSource().sendSuccess(() ->
                Component.literal("Crafting Table reach radius set to: " + radius), true);
        return 1;
    }

    private static int getCraftingTableReach(CommandContext<CommandSourceStack> context) {
        int radius = Services.CONFIG.craftingTableReach();
        context.getSource().sendSuccess(() ->
                Component.literal("Crafting Table reach radius: " + radius), false);
        return 1;
    }

    private static int setPlayerInventoryEnabled(CommandContext<CommandSourceStack> context, boolean enabled) {
        Services.CONFIG.setCraftingPlayerCanReach(enabled);
        Services.CONFIG.save();
        context.getSource().sendSuccess(() ->
                Component.literal("Player Inventory Crafting reach enabled: " + enabled), true);
        return 1;
    }

    private static int setPlayerInventoryReach(CommandContext<CommandSourceStack> context, int radius) {
        Services.CONFIG.setCraftingPlayerReach(radius);
        Services.CONFIG.save();
        context.getSource().sendSuccess(() ->
                Component.literal("Player Inventory Crafting reach radius set to: " + radius), true);
        return 1;
    }

    private static int getPlayerInventoryReach(CommandContext<CommandSourceStack> context) {
        int radius = Services.CONFIG.craftingPlayerReach();
        context.getSource().sendSuccess(() ->
                Component.literal("Player Inventory Crafting reach radius: " + radius), false);
        return 1;
    }
}
