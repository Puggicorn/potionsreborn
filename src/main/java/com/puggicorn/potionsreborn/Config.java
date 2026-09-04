package com.puggicorn.potionsreborn;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Common config for Potions Reborn. */
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue CENTRIFUGE_FUEL_USES = BUILDER
        .comment("How many Centrifuge operations one Breeze Powder fuels.")
        .defineInRange("centrifugeFuelUses", 20, 1, 1000);

    public static final ModConfigSpec.IntValue CENTRIFUGE_PROCESS_TIME = BUILDER
        .comment("How many ticks one Centrifuge separation takes (20 = 1 second).")
        .defineInRange("centrifugeProcessTime", 400, 20, 72000);

    static final ModConfigSpec SPEC = BUILDER.build();
}
