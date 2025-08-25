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

//handles itemstack rendering/movement
@Mixin(BeltRenderer.class)
public class BeltRendererMixin {
    @ModifyVariable(method = "renderItems", at = @At(value = "STORE"), name = "verticality")
    private int modifyVerticality(int value, @Local(argsOnly = true) BeltBlockEntity be) {
        BlockState state = be.getBlockState();
        return switch(state.getValue(BeltBlock.SLOPE)) { //TODO: Find correct rotation for item
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
        //if(slope == BeltSlope.VERTICAL) return 0;
        return tiltForward ? -45 : 45;
    }

    @Inject(method = "renderItem", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V"))
    private void printItemLocation(
        BeltBlockEntity be, float partialTicks, PoseStack ms,
        MultiBufferSource buffer, int light, int overlay,
        Direction beltFacing, Vec3i directionVec, BeltSlope slope,
        int verticality, boolean slopeAlongX, boolean onContraption,
        TransportedItemStack transported, Vec3 beltStartOffset, CallbackInfo ci,
        @Local(name = "itemPos") Vec3 itemPos
    ) {
        Minecraft.getInstance().player.sendSystemMessage(Component.literal(String.format("%fx %fy %fz", itemPos.x, itemPos.y, itemPos.z)));
        //find where the items are moving over each line
    }

    @WrapOperation(method = "renderItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos$MutableBlockPos;move(III)Lnet/minecraft/core/BlockPos$MutableBlockPos;"))
    private BlockPos.MutableBlockPos wrapMove(BlockPos.MutableBlockPos instance, int x, int y, int z, Operation<BlockPos.MutableBlockPos> original) {
        return original.call(instance, 0, y, 0); //only apply vertical movement
    }

    @Inject(
        method = "renderItem",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V",
            shift = At.Shift.AFTER
        )
    )
    private void onMulPose(
        BeltBlockEntity be, float partialTicks, PoseStack ms,
        MultiBufferSource buffer, int light, int overlay,
        Direction beltFacing, Vec3i directionVec, BeltSlope slope,
        int verticality, boolean slopeAlongX, boolean onContraption,
        TransportedItemStack transported, Vec3 beltStartOffset, CallbackInfo ci
    ) {
        ms.mulPose(Axis.YP.rotationDegrees(180)); //try to ensure items are rendered upright on the belt, regardless of isItemUpright()
    }
}
