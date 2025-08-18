package com.rld.unlimitedlogistics.mixin;

import com.simibubi.create.content.logistics.filter.FilterItemStack;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FilteringBehaviour.class)
public abstract class FilteringBehaviourMixin {
    @Shadow public abstract int getAmount();
    @Shadow public int count;
    @Shadow public abstract ItemStack getFilter();
}
