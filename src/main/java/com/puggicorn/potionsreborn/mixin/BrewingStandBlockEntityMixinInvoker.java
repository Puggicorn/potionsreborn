package com.puggicorn.potionsreborn.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Accesses vanilla's private isBrewable helper so the redirect can preserve its behavior. */
@Mixin(BrewingStandBlockEntity.class)
public interface BrewingStandBlockEntityMixinInvoker {
    @Invoker("isBrewable")
    static boolean isBrewable(PotionBrewing brewing, NonNullList<ItemStack> items) {
        throw new AssertionError("Mixin invoker was not applied");
    }
}