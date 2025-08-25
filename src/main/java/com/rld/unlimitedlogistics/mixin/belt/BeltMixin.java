package com.rld.unlimitedlogistics.mixin.belt;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.rld.unlimitedlogistics.BeltState;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.content.kinetics.belt.BeltSlope;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;


@Mixin(BeltBlock.class)
public abstract class BeltMixin extends HorizontalKineticBlockMixin {
    @Shadow @Final public static Property<BeltSlope> SLOPE;

    @Shadow public static List<BlockPos> getBeltChain(LevelAccessor world, BlockPos controllerPos) {
        return null;
    }

    @Unique private static final Property<BeltState> LIFTER = EnumProperty.create("lifter", BeltState.class);

    @WrapOperation(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/kinetics/belt/BeltBlock;registerDefaultState(Lnet/minecraft/world/level/block/state/BlockState;)V"
        )
    )
    private void wrapRegisterDefaultState(BeltBlock instance, BlockState state, Operation<Void> original) {
        original.call(instance, state.setValue(LIFTER, BeltState.NONE));
    }

    @Inject(
        method = "createBlockStateDefinition",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/StateDefinition$Builder;add([Lnet/minecraft/world/level/block/state/properties/Property;)Lnet/minecraft/world/level/block/state/StateDefinition$Builder;",
            shift = At.Shift.AFTER
        )

    )
    private void onCreateBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(LIFTER);
    }

    @ModifyReturnValue(method = "canTransportObjects", at = @At("TAIL")) //makes it so vertical belts can transport items
    private static boolean modifyCanTransportObjects(boolean original, @Local(argsOnly = true) BlockState state) {
        return state.getValue(SLOPE) != BeltSlope.VERTICAL || state.getValue(LIFTER) != BeltState.NONE;
    }

    //allows sheets to be used to create lifter belts
    @Inject(method = "useItemOn", at = @At(value = "FIELD", target = "Lcom/simibubi/create/AllItems;WRENCH:Lcom/tterrag/registrate/util/entry/ItemEntry;", shift = At.Shift.BEFORE)) //code will be injected before BeltBlock line 249
    private void onSheetUseItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<ItemInteractionResult> cir) {
        int sheetType = stack.is(AllItems.IRON_SHEET.get()) ? 0 : stack.is(AllItems.BRASS_SHEET.get()) ? 1 : 2; //0 == iron sheet, 1 == brass sheet, 2 == other;
        if(sheetType < 2 && state.getValue(SLOPE) == BeltSlope.VERTICAL && state.getValue(LIFTER) == BeltState.NONE) { //if using a sheet on a vertical belt
            if(!player.isCreative()) stack.shrink(1);
            BeltState type = sheetType == 0 ? BeltState.IRON_LIFTER : BeltState.BRASS_LIFTER;
            for(BlockPos beltPos: getBeltChain(level, BeltHelper.getControllerBE(level, pos).getBlockPos()))
                level.setBlockAndUpdate(beltPos, level.getBlockState(beltPos).setValue(LIFTER, type));
        }
    }

    @Inject(method = "useItemOn", at = @At(value = "RETURN", ordinal = 3), cancellable = true)
    private void onWrenchUseItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<ItemInteractionResult> cir) {
        BeltState lifterState = state.getValue(LIFTER);
        if(lifterState != BeltState.NONE) { //If it's a lifter belt, wrenching it will remove the lifter property
            for(BlockPos beltPos: getBeltChain(level, BeltHelper.getControllerBE(level, pos).getBlockPos()))
                level.setBlockAndUpdate(beltPos, level.getBlockState(beltPos).setValue(LIFTER, BeltState.NONE));
            cir.setReturnValue(ItemInteractionResult.SUCCESS);
            if(lifterState == BeltState.IRON_LIFTER) player.addItem(AllItems.IRON_SHEET.asStack(1)); else player.addItem(AllItems.BRASS_SHEET.asStack(1));
            cir.cancel();
        }
        //let default interaction play out
    }
}
