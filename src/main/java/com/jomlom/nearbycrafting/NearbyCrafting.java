package com.jomlom.nearbycrafting;

import com.jomlom.nearbycrafting.util.NearbyCraftingConfig;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;


public class NearbyCrafting implements ModInitializer {

	public static final String MOD_ID = "nearbycrafting";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		NearbyCraftingConfig.HANDLER.load();
		InitializeCommands();
	}

	private void InitializeCommands() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("nearbycrafting")

					// Operator permission
					.requires(source -> source.hasPermissionLevel(2))

					// Crafting Table subcommand
					.then(CommandManager.literal("craftingtable")
							.then(CommandManager.literal("enable")
									.executes(context -> setCraftingTableEnabled(context, true)))
							.then(CommandManager.literal("disable")
									.executes(context -> setCraftingTableEnabled(context, false)))
							.then(CommandManager.literal("setReach")
									.then(CommandManager.argument("radius", IntegerArgumentType.integer(0, 50))
											.executes(context -> setCraftingTableReach(context,
													IntegerArgumentType.getInteger(context, "radius")))))
							.then(CommandManager.literal("getReach")
									.executes(NearbyCrafting::getCraftingTableReach))
					)

					// Player Inventory Crafting subcommand
					.then(CommandManager.literal("playerinventorycrafting")
							.then(CommandManager.literal("enable")
									.executes(context -> setPlayerInventoryEnabled(context, true)))
							.then(CommandManager.literal("disable")
									.executes(context -> setPlayerInventoryEnabled(context, false)))
							.then(CommandManager.literal("setReach")
									.then(CommandManager.argument("radius", IntegerArgumentType.integer(0, 50))
											.executes(context -> setPlayerInventoryReach(context,
													IntegerArgumentType.getInteger(context, "radius")))))
							.then(CommandManager.literal("getReach")
									.executes(NearbyCrafting::getPlayerInventoryReach))
					)
			);
		});
	}

	// Helper methods for Crafting Table
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

	// Helper methods for Player Inventory Crafting
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