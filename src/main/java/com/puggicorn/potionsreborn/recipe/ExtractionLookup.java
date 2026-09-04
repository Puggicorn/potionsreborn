package com.puggicorn.potionsreborn.recipe;

import com.puggicorn.potionsreborn.potion.ModPotions;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

/**
 * Looks up {@link EffectExtractionRecipe}s against a stack, using the recipe manager so
 * datapacks control everything.
 */
public final class ExtractionLookup {
    private ExtractionLookup() {
    }

    @Nullable
    public static EffectExtractionRecipe find(Level level, ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        return level.getRecipeManager()
            .getRecipeFor(ModRecipeTypes.EFFECT_EXTRACTION.get(), new SingleRecipeInput(stack), level)
            .map(RecipeHolder::value)
            .orElse(null);
    }

    /**
     * Builds the potion this recipe produces, for display in recipe viewers (JEI/EMI/REI).
     */
    public static ItemStack resultStack(EffectExtractionRecipe recipe) {
        ItemStack stack = new ItemStack(Items.POTION);
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(
            Optional.<Holder<Potion>>of(ModPotions.EXTRACTED), Optional.empty(), recipe.createInstances()));
        return stack;
    }
}
