package com.jomlom.nearbycrafting;

import com.jomlom.nearbycrafting.util.NearbyCraftingConfig;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.inventory.Inventory;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class NearbyCrafting implements ModInitializer {

	public static final String MOD_ID = "nearbycrafting";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);


	@Override
	public void onInitialize() {
		NearbyCraftingConfig.HANDLER.load();
		InitializeCommands();
		DetectContainerBlocks();
	}

	private void InitializeCommands() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("nearbycrafting")

					// Operator permission
					.requires(source -> source.hasPermissionLevel(2))

					// Crafting Table subcommand
					.then(CommandManager.literal("craftingTable")
							.then(CommandManager.literal("enable")
									.executes(context -> setCraftingTableEnabled(context, true)))
							.then(CommandManager.literal("disable")
									.executes(context -> setCraftingTableEnabled(context, false)))
							.then(CommandManager.literal("setReach")
									.then(CommandManager.argument("radius", IntegerArgumentType.integer(0, 50))
											.executes(context -> setCraftingTableReach(context, IntegerArgumentType.getInteger(context, "radius")))))
							.then(CommandManager.literal("getReach")
									.executes(NearbyCrafting::getCraftingTableReach))
					)

					// Player Inventory Crafting subcommand
					.then(CommandManager.literal("playerInventoryCrafting")
							.then(CommandManager.literal("enable")
									.executes(context -> setPlayerInventoryEnabled(context, true)))
							.then(CommandManager.literal("disable")
									.executes(context -> setPlayerInventoryEnabled(context, false)))
							.then(CommandManager.literal("setReach")
									.then(CommandManager.argument("radius", IntegerArgumentType.integer(0, 50))
											.executes(context -> setPlayerInventoryReach(context, IntegerArgumentType.getInteger(context, "radius")))))
							.then(CommandManager.literal("getReach")
									.executes(NearbyCrafting::getPlayerInventoryReach))
					)

					// CONTAINERS subcommand for toggling blocks accessibility individually
					.then(CommandManager.literal("CONTAINERS")

							.then(CommandManager.literal("enable")
									.then(CommandManager.argument("block", IdentifierArgumentType.identifier())
											.suggests(ContainerBlockSuggestionProvider.SUGGEST_CONTAINER_BLOCKS)
											.executes(context -> setContainerBlockEnabled(context, true))))

							.then(CommandManager.literal("disable")
									.then(CommandManager.argument("block", IdentifierArgumentType.identifier())
											.suggests(ContainerBlockSuggestionProvider.SUGGEST_CONTAINER_BLOCKS)
											.executes(context -> setContainerBlockEnabled(context, false))))

							.then(CommandManager.literal("get")
									.then(CommandManager.argument("block", IdentifierArgumentType.identifier())
											.suggests(ContainerBlockSuggestionProvider.SUGGEST_CONTAINER_BLOCKS)
											.executes(this::getContainerBlockStatus)))

							.then(CommandManager.literal("list")
									.executes(this::listContainerBlocks))
					)
			);
		});
	}

	public static class ContainerBlockSuggestionProvider {
		public static final SuggestionProvider<ServerCommandSource> SUGGEST_CONTAINER_BLOCKS = (context, builder) -> {
			for (Identifier blockId : Registries.BLOCK.getIds()) {
				if (NearbyCraftingConfig.containerBlockToggles
						.getOrDefault(blockId.getNamespace(), Collections.emptyMap())
						.containsKey(blockId.toString())) {
					builder.suggest(blockId.toString());
				}
			}
			return builder.buildFuture();
		};
	}

	private int setContainerBlockEnabled(CommandContext<ServerCommandSource> context, boolean enabled) throws CommandSyntaxException {
		Identifier blockId = IdentifierArgumentType.getIdentifier(context, "block");

		setBlockEnabled(blockId, enabled);

		context.getSource().sendFeedback(() ->
				Text.literal("Container block " + blockId + " set to " + (enabled ? "enabled" : "disabled")), true);
		return 1;
	}

	private int listContainerBlocks(CommandContext<ServerCommandSource> context) {
		StringBuilder sb = new StringBuilder("Container blocks and their enabled states:\n");

		NearbyCraftingConfig.containerBlockToggles.forEach((namespace, map) -> {
			map.forEach((blockId, enabled) -> {
				sb.append(blockId).append(" : ").append(enabled ? "Enabled" : "Disabled").append("\n");
			});
		});

		context.getSource().sendFeedback(() ->
				Text.literal(sb.toString()), false);
		return 1;
	}

	private void DetectContainerBlocks() {
		Map<String, Map<Identifier, Boolean>> containerBlocks = new HashMap<>();

		for (BlockEntityType<?> beType : Registries.BLOCK_ENTITY_TYPE) {
			Identifier beId = Registries.BLOCK_ENTITY_TYPE.getId(beType);
			if (beId == null) continue;

			BlockState validState = null;

			for (Block block : Registries.BLOCK) {
				if (block instanceof BlockWithEntity blockWE) {
					BlockEntity be = null;
					try {
						be = blockWE.createBlockEntity(BlockPos.ORIGIN, block.getDefaultState());
					} catch (Exception ignored) {}

					if (be != null && be.getType() == beType) {
						validState = block.getDefaultState();
						break;
					}
				}
			}

			if (validState == null) {
				continue;
			}

			BlockEntity blockEntity;
			try {
				blockEntity = beType.instantiate(BlockPos.ORIGIN, validState);
			} catch (Exception e) {
				continue;
			}

			if (!(blockEntity instanceof Inventory)) continue;

			for (Block block : Registries.BLOCK) {
				if (block instanceof BlockWithEntity blockWE) {
					BlockEntity be = null;
					try {
						be = blockWE.createBlockEntity(BlockPos.ORIGIN, block.getDefaultState());
					} catch (Exception ignored) {}

					if (be != null && be.getType() == beType) {
						Identifier blockId = Registries.BLOCK.getId(block);

                        String namespace = blockId.getNamespace();
						containerBlocks.putIfAbsent(namespace, new HashMap<>());
						Map<Identifier, Boolean> blocks = containerBlocks.get(namespace);

						String blockIdStr = blockId.toString();
						NearbyCraftingConfig.containerBlockToggles.putIfAbsent(namespace, new HashMap<>());
						NearbyCraftingConfig.containerBlockToggles.get(namespace).putIfAbsent(blockIdStr, true);

						blocks.putIfAbsent(blockId, NearbyCraftingConfig.containerBlockToggles.get(namespace).get(blockIdStr));
					}
				}
			}
		}
	}

	private int getContainerBlockStatus(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		Identifier blockId = IdentifierArgumentType.getIdentifier(context, "block");
		String ns = blockId.getNamespace();
		String id = blockId.toString();

		boolean enabled = NearbyCraftingConfig.containerBlockToggles
				.getOrDefault(ns, Collections.emptyMap())
				.getOrDefault(id, true);

		context.getSource().sendFeedback(() ->
				Text.literal("Container block " + blockId + " is " + (enabled ? "enabled" : "disabled")), false);

		return 1;
	}

	public void setBlockEnabled(Identifier blockId, boolean enabled) {
		String ns = blockId.getNamespace();
		String id = blockId.toString();
		NearbyCraftingConfig.containerBlockToggles
				.computeIfAbsent(ns, x -> new HashMap<>())
				.put(id, enabled);
		NearbyCraftingConfig.HANDLER.save();
	}

	private static int setCraftingTableEnabled(CommandContext<ServerCommandSource> context, boolean enabled) {
		NearbyCraftingConfig.craftingTableCanReach = enabled;
		NearbyCraftingConfig.HANDLER.save();
		context.getSource().sendFeedback(() ->
				Text.of("Crafting Table reach enabled: " + enabled), true);
		return 1;
	}

	private static int setCraftingTableReach(CommandContext<ServerCommandSource> context, int radius) {
		NearbyCraftingConfig.craftingTableReach = radius;
		NearbyCraftingConfig.HANDLER.save();
		context.getSource().sendFeedback(() ->
				Text.of("Crafting Table reach radius set to: " + radius), true);
		return 1;
	}

	private static int getCraftingTableReach(CommandContext<ServerCommandSource> context) {
		int radius = NearbyCraftingConfig.craftingTableReach;
		context.getSource().sendFeedback(() ->
				Text.of("Crafting Table reach radius: " + radius), false);
		return 1;
	}

	private static int setPlayerInventoryEnabled(CommandContext<ServerCommandSource> context, boolean enabled) {
		NearbyCraftingConfig.craftingPlayerCanReach = enabled;
		NearbyCraftingConfig.HANDLER.save();
		context.getSource().sendFeedback(() ->
				Text.of("Player Inventory Crafting reach enabled: " + enabled), true);
		return 1;
	}

	private static int setPlayerInventoryReach(CommandContext<ServerCommandSource> context, int radius) {
		NearbyCraftingConfig.craftingPlayerReach = radius;
		NearbyCraftingConfig.HANDLER.save();
		context.getSource().sendFeedback(() ->
				Text.of("Player Inventory Crafting reach radius set to: " + radius), true);
		return 1;
	}

	private static int getPlayerInventoryReach(CommandContext<ServerCommandSource> context) {
		int radius = NearbyCraftingConfig.craftingPlayerReach;
		context.getSource().sendFeedback(() ->
				Text.of("Player Inventory Crafting reach radius: " + radius), false);
		return 1;
	}
}