package com.rld.unlimitedlogistics;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue FILL_FACTORY_CONTAINERS = BUILDER
            .comment("Whether factory gauges will ignore the 100 stack maximum and fully fill their assigned containers")
            .define("unlimitedFilling", true);


    static final ModConfigSpec SPEC = BUILDER.build();
}
