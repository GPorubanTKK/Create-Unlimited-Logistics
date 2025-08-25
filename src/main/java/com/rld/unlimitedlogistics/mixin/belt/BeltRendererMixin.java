package com.rld.unlimitedlogistics.mixin.belt;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.belt.*;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.logging.Logger;

import static com.rld.unlimitedlogistics.CreateUnlimitedLogistics.LOGGER;

//handles itemstack rendering/movement
@Mixin(BeltRenderer.class)
public class BeltRendererMixin {
    @ModifyVariable(method = "renderItems", at = @At(value = "STORE"), name = "verticality")
    private int modifyVerticality(int value, @Local(argsOnly = true) BeltBlockEntity be) {
        BlockState state = be.getBlockState();
        return switch(state.getValue(BeltBlock.SLOPE)) {
            case UPWARD -> 1;
            case DOWNWARD -> -1;
            default -> 0;
        };
    }

    @ModifyVariable(method = "renderItem", at = @At("STORE"), name = "slopeAngle")
    private float modifySlopeAngle(
        float value, @Local(name = "onSlope") boolean onSlope,
        @Local(name = "tiltForward") boolean tiltForward,
        @Local(argsOnly = true) BeltSlope slope
    ) {
        if((!onSlope) || slope == BeltSlope.VERTICAL) return 0;
        return tiltForward ? -45 : 45;
    }

    @WrapOperation(method = "renderItem", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V", ordinal = 0))
    private void wrapTranslate(
        PoseStack instance,
        double x, double y, double z,
        Operation<Void> original, @Local(argsOnly = true) Direction beltFacing, @Local(argsOnly = true) BeltSlope slope
    ) {
        if(slope == BeltSlope.VERTICAL) {
            original.call(instance, 0d, switch(beltFacing.getAxis()) {
                case X -> x;
                case Y -> y;
                case Z -> z;
            }, 0d);
        } else original.call(instance, x, y, z);
    }

    @Inject(method = "renderItem", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", ordinal = 0))
    private void printItemLocation(
        BeltBlockEntity be, float partialTicks, PoseStack ms,
        MultiBufferSource buffer, int light, int overlay,
        Direction beltFacing, Vec3i directionVec, BeltSlope slope,
        int verticality, boolean slopeAlongX, boolean onContraption,
        TransportedItemStack transported, Vec3 beltStartOffset, CallbackInfo ci,
        @Local(name = "itemPos") Vec3 itemPos
    ) {
    }
}
