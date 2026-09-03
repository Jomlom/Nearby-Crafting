package com.jomlom.nearbycrafting.neoforge;

import net.neoforged.neoforge.common.ModConfigSpec;

public class NearbyCraftingConfigNeoForge {

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue CRAFTING_PLAYER_CAN_REACH;
    public static final ModConfigSpec.IntValue CRAFTING_PLAYER_REACH;
    public static final ModConfigSpec.BooleanValue CRAFTING_TABLE_CAN_REACH;
    public static final ModConfigSpec.IntValue CRAFTING_TABLE_REACH;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        CRAFTING_PLAYER_CAN_REACH = builder
                .comment("Allows players to reach nearby item containers and use their contents for crafting.")
                .define("craftingPlayerCanReach", true);
        CRAFTING_PLAYER_REACH = builder
                .comment("Radius (in blocks) which players can reach item containers.")
                .defineInRange("craftingPlayerReach", 8, 0, 50);

        CRAFTING_TABLE_CAN_REACH = builder
                .comment("Allows crafting tables to reach nearby item containers and use their contents for crafting.")
                .define("craftingTableCanReach", true);
        CRAFTING_TABLE_REACH = builder
                .comment("Radius (in blocks) which crafting tables can reach item containers.")
                .defineInRange("craftingTableReach", 8, 0, 50);

        SPEC = builder.build();
    }
}
