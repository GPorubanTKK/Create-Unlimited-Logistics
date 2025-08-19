package com.rld.unlimitedlogistics.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static com.rld.unlimitedlogistics.CreateUnlimitedLogistics.LOGGER;


//allows us to fill up to 100 vaults
@Mixin(FactoryPanelBehaviour.class)
public abstract class FactoryGaugeMixin extends FilteringBehaviourMixin {
    @Unique int mode = 0;


    @Unique private int getVaultMultiplier() {
        return AllConfigs.server().logistics.vaultCapacity.get();
    }

    @Inject(method = "createBoard", at = @At("HEAD"), cancellable = true)
    public void onCreateBoard(Player player, BlockHitResult hitResult, CallbackInfoReturnable<ValueSettingsBoard> cir) {
        int maxAmount = 100;
        cir.setReturnValue(
            new ValueSettingsBoard(CreateLang.translate("factory_panel.target_amount").component(),
                maxAmount, 10,
                List.of(
                    CreateLang.translate("schedule.condition.threshold.items")
                        .component(),
                    CreateLang.translate("schedule.condition.threshold.stacks")
                        .component(),
                    Component.translatable("schedule.condition.threshold.vaults")
                ),
                new ValueSettingsFormatter((settings) -> {
                    if(settings.value() == 0) return CreateLang.translateDirect("gui.factory_panel.inactive");
                    int valueToShow = Math.max(0, settings.value());
                    return switch(settings.row()) {
                        case 1 -> Component.literal(valueToShow + "▤");
                        case 2 -> Component.literal(valueToShow + "▨");
                        default -> Component.literal(valueToShow + "");
                    };
                })));
        cir.cancel();
    }

    @Inject(method = "setValueSettings", at = @At(value = "INVOKE", target = "Lorg/joml/Math;max(II)I"))
    public void onSetValueSettings(
        Player player, ValueSettingsBehaviour.ValueSettings settings, boolean ctrlDown, CallbackInfo ci
    ) {
        mode = settings.row();
        LOGGER.info("Row {} Value {} Mode {}", settings.row(), settings.value(), mode);
    }

    @Inject(method = "write", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/nbt/CompoundTag;putInt(Ljava/lang/String;I)V", ordinal = 0
    ))
    public void onWrite(
        CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci, @Local(name = "panelTag") CompoundTag panelTag
    ) { panelTag.putInt("Mode", mode); }

    @Inject(method = "writeSafe", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/nbt/CompoundTag;putInt(Ljava/lang/String;I)V", ordinal = 0
    ))

    public void onWriteSafe(
        CompoundTag nbt, HolderLookup.Provider registries, CallbackInfo ci, @Local(name = "panelTag") CompoundTag panelTag
    ) { panelTag.putInt("Mode", mode); }

    @Inject(method = "read", at = @At(
        value = "INVOKE",
        target = "Lcom/simibubi/create/content/logistics/filter/FilterItemStack;of(Lnet/minecraft/core/HolderLookup$Provider;Lnet/minecraft/nbt/CompoundTag;)Lcom/simibubi/create/content/logistics/filter/FilterItemStack;",
        ordinal = 0
    ))
    public void onRead(
        CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci, @Local(name = "panelTag") CompoundTag panelTag
    ) { mode = panelTag.getInt("Mode"); }

    /**
     * @author GPorubanTKK
     * @reason Adds support for Vaults Row
     */
    @Overwrite public ValueSettingsBehaviour.ValueSettings getValueSettings() {
        return new ValueSettingsBehaviour.ValueSettings(mode, count);
    }

    @ModifyVariable(method = "getCountLabelForValueBox", at = @At("STORE"), name = "inStorage")
    public int modifyInStorage(int is, @Local(name = "levelInStorage") int levelInStorage) {
        return levelInStorage / switch(mode) {
            case 0 -> 1;
            case 1 -> getFilter().getMaxStackSize();
            case 2 -> getFilter().getMaxStackSize() * getVaultMultiplier();
            default -> throw new IllegalStateException("Invalid Mode: " + mode);
        };
    }

    @ModifyVariable(method = "getCountLabelForValueBox", at = @At("STORE"), name = "stacks")
    public String modifyStacks(String stacks) {
        return switch(mode) {
            case 0 -> "";
            case 1 -> "▤";
            case 2 -> "▨";
            default -> throw new IllegalArgumentException("Mode is outside bounds [0,2]");
        };
    }

    @ModifyVariable(method = "tickStorageMonitor", at = @At("STORE"), name = "demand")
    public int modifyDemand(int original) {
        return getAmount() * switch(mode) {
            case 0 -> 1;
            case 1 -> getFilter().getMaxStackSize();
            case 2 -> getFilter().getMaxStackSize() * getVaultMultiplier();
            default -> throw new IllegalArgumentException("Invalid Mode");
        };
    }
}
