package com.rld.unlimitedlogistics.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.rld.unlimitedlogistics.config.CULConfig;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FactoryPanelScreen.class)
public class FactoryScreenMixin {
    @WrapOperation(method = "mouseScrolled", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(III)I"))
    private int wrapClamp(int val, int min, int max, Operation<Integer> original) {
        return original.call(val, min, CULConfig.CONFIG.GAUGE_SLOT_MAX.get());
    }
}
