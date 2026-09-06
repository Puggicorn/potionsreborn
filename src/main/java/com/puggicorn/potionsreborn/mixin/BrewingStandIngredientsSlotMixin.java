package com.puggicorn.potionsreborn.mixin;

import com.puggicorn.potionsreborn.recipe.ExtractionLookup;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Extends the brewing stand GUI ingredient slot to accept items backed by an extraction recipe.
 * Vanilla ingredients still go through the original slot logic unchanged.
 */
@Mixin(targets = "net.minecraft.world.inventory.BrewingStandMenu$IngredientsSlot")
public abstract class BrewingStandIngredientsSlotMixin {
    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void potionsreborn$allowExtractionIngredients(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        Container container = ((Slot)(Object)this).container;
        if (container instanceof BrewingStandBlockEntity brewingStand) {
            Level level = brewingStand.getLevel();
            if (level != null && ExtractionLookup.find(level, stack) != null) {
                cir.setReturnValue(true);
            }
        }
    }
}
