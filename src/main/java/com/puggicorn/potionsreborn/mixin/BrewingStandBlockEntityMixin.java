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
import org.spongepowered.asm.mixin.injection.Redirect;
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
     * The vanilla helper is static and receives no level, so it cannot look up the datapack recipe
     * for an unknown ingredient. This redirect retains all vanilla/modded mixes, then adds the
     * extraction-recipe case using the owning brewing stand's level.
     */
    @Redirect(
        method = "serverTick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/entity/BrewingStandBlockEntity;isBrewable(Lnet/minecraft/world/item/alchemy/PotionBrewing;Lnet/minecraft/core/NonNullList;)Z"
        )
    )
    private static boolean potionsreborn$checkBrewable(PotionBrewing brewing, NonNullList<ItemStack> items,
            Level level, BlockPos pos, net.minecraft.world.level.block.state.BlockState state, BrewingStandBlockEntity brewingStand) {
        if (BrewingStandBlockEntityMixinInvoker.isBrewable(brewing, items)) {
            return true;
        }

        if (ExtractionLookup.find(level, items.get(3)) != null) {
            for (int i = 0; i < 3; i++) {
                if (RebornBrewingLogic.canExtractOnto(items.get(i), brewing)) {
                    return true;
                }
            }
        }
        for (int i = 0; i < 3; i++) {
            if (RebornBrewingLogic.canModifyExtracted(items.get(i), items.get(3))) {
                return true;
            }
        }
        return false;
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
        PotionBrewing brewing = level.potionBrewing();
        boolean anyCustomOperation = false;
        for (int i = 0; i < 3; i++) {
            ItemStack potion = items.get(i);
            if (recipe != null && RebornBrewingLogic.canExtractOnto(potion, brewing)
                || RebornBrewingLogic.canModifyExtracted(potion, ingredient)) {
                anyCustomOperation = true;
                break;
            }
        }
        // No applicable extraction/modifier operation: let vanilla or another mod's recipe run.
        if (!anyCustomOperation) {
            return;
        }

        for (int i = 0; i < 3; i++) {
            ItemStack stack = items.get(i);
            if (recipe != null && RebornBrewingLogic.canExtractOnto(stack, brewing)) {
                items.set(i, RebornBrewingLogic.brewOnto(stack, recipe));
            } else if (RebornBrewingLogic.canModifyExtracted(stack, ingredient)) {
                items.set(i, RebornBrewingLogic.modifyExtracted(stack, ingredient));
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
