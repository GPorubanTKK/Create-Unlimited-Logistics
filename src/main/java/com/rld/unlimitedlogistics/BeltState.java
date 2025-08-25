package com.rld.unlimitedlogistics;

import net.createmod.catnip.lang.Lang;
import net.minecraft.util.StringRepresentable;

public enum BeltState implements StringRepresentable {
    NONE, IRON_LIFTER, BRASS_LIFTER;

    @Override public String getSerializedName() { return Lang.asId(name()); }
}
