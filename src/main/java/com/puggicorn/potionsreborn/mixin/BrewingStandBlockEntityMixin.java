package com.puggicorn.potionsreborn.mixin;

import com.puggicorn.potionsreborn.brewing.RebornBrewingLogic;
import com.puggicorn.potionsreborn.recipe.EffectExtractionRecipe;
import com.puggicorn.potionsreborn.recipe.ExtractionLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hooks the vanilla brewing stand so that any ingredient with an {@code effect_extraction} recipe
 * extracts those effects into the potions below instead of using vanilla brewing mixes.
 * Anything without a recipe keeps the vanilla behavior untouched.
 */
@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandBlockEntityMixin {
    @Unique
    private BrewingStandBlockEntity potionsreborn$self() {
        return (BrewingStandBlockEntity)(Object)this;
    }

    @Unique
    private static Level potionsreborn$levelOf(BrewingStandBlockEntity be) {
        return be.getLevel();
    }

    /**
     * Replaces the vanilla mix with effect extraction for ingredients that have an extraction recipe.
     * Vanilla ingredient consumption (including crafting remainders) is replicated so automation
     * keeps working.
     */
    @Inject(method = "doBrew", at = @At("HEAD"), cancellable = true)
    private static void potionsreborn$doBrew(Level level, BlockPos pos, NonNullList<ItemStack> items, CallbackInfo ci) {
        ItemStack ingredient = items.get(3);
        EffectExtractionRecipe recipe = ExtractionLookup.find(level, ingredient);
        if (recipe == null) {
            return;
        }

        // Only take over when extraction actually applies to at least one bottle; otherwise defer
        // to the vanilla/modded brew for this ingredient (e.g. a normal Spider Eye mix).
        PotionBrewing brewing = level.potionBrewing();
        boolean anyExtractable = false;
        for (int i = 0; i < 3; i++) {
            if (RebornBrewingLogic.canExtractOnto(items.get(i), brewing)) {
                anyExtractable = true;
                break;
            }
        }
        if (!anyExtractable) {
            return;
        }

        for (int i = 0; i < 3; i++) {
            ItemStack stack = items.get(i);
            if (RebornBrewingLogic.canExtractOnto(stack, brewing)) {
                items.set(i, RebornBrewingLogic.brewOnto(stack, recipe));
            }
        }

        if (ingredient.hasCraftingRemainingItem()) {
            ItemStack remainder = ingredient.getCraftingRemainingItem();
            ingredient.shrink(1);
            if (ingredient.isEmpty()) {
                ingredient = remainder;
            } else {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), remainder);
            }
        } else {
            ingredient.shrink(1);
        }
        items.set(3, ingredient);

        level.levelEvent(1035, pos, 0);
        EventHooks.onPotionBrewed(items);
        ci.cancel();
    }

    /**
     * Lets recipe'd items enter the ingredient slot even when vanilla would reject them
     * (covers hopper/dropper insertion).
     */
    @Inject(method = "canPlaceItem", at = @At("HEAD"), cancellable = true)
    private void potionsreborn$canPlaceItem(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (slot == 3) {
            Level level = potionsreborn$levelOf(potionsreborn$self());
            if (level != null && ExtractionLookup.find(level, stack) != null) {
                cir.setReturnValue(true);
            }
        }
    }
}
