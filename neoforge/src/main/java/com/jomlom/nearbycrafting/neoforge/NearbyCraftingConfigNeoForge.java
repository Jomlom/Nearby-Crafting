package com.jomlom.nearbycrafting.neoforge;

import com.jomlom.nearbycrafting.CraftingMode;
import net.minecraftforge.common.ForgeConfigSpec;

public class NearbyCraftingConfigNeoForge {

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.EnumValue<CraftingMode> MODE;
    public static final ForgeConfigSpec.BooleanValue CRAFTING_PLAYER_CAN_REACH;
    public static final ForgeConfigSpec.IntValue CRAFTING_PLAYER_REACH;
    public static final ForgeConfigSpec.BooleanValue CRAFTING_TABLE_CAN_REACH;
    public static final ForgeConfigSpec.IntValue CRAFTING_TABLE_REACH;
    public static final ForgeConfigSpec.IntValue CRAFTING_TABLE_DEPTH;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        MODE = builder
                .comment("DEFAULT scans a cube around the crafter. CONNECTED only uses containers connected to the crafting table.")
                .defineEnum("mode", CraftingMode.DEFAULT);

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

        CRAFTING_TABLE_DEPTH = builder
                .comment("How many containers away from the crafting table are used in connected mode.")
                .defineInRange("craftingTableDepth", 10, 1, 50);

        SPEC = builder.build();
    }
}
