package com.jomlom.nearbycrafting.client;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.player.Player;

public class OperatorStatus {

    public static boolean isOperator() {
        Player player = Minecraft.getInstance().player;
        return player != null && Commands.LEVEL_GAMEMASTERS.check(player.permissions());
    }
}
