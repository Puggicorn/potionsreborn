package com.puggicorn.potionsreborn.compat.rei;

import com.puggicorn.potionsreborn.recipe.EffectExtractionRecipe;
import com.puggicorn.potionsreborn.recipe.ExtractionLookup;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * REI display for an {@code effect_extraction} recipe.
 */
public class ExtractionDisplay extends BasicDisplay {
    private final EffectExtractionRecipe recipe;

    public ExtractionDisplay(RecipeHolder<EffectExtractionRecipe> recipe) {
        super(
            List.of(EntryIngredients.ofItemStacks(java.util.Arrays.asList(recipe.value().ingredient().getItems()))),
            List.of(EntryIngredient.of(EntryStacks.of(ExtractionLookup.resultStack(recipe.value())))),
            Optional.of(recipe.id()));
        this.recipe = recipe.value();
    }

    /** Comma-separated effect names, for the category label. */
    public String getRecipeEffects() {
        return this.recipe.effects().stream()
            .map(com.puggicorn.potionsreborn.brewing.EffectEntry::effect)
            .map(holder -> holder.value().getDisplayName().getString())
            .collect(Collectors.joining(", "));
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return PotionsRebornReiPlugin.EXTRACTION;
    }
}
