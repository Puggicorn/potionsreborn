package com.puggicorn.potionsreborn.compat.emi;

import com.puggicorn.potionsreborn.brewing.EffectEntry;
import com.puggicorn.potionsreborn.recipe.EffectExtractionRecipe;
import com.puggicorn.potionsreborn.recipe.ExtractionLookup;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import java.util.stream.Collectors;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * EMI representation of an {@code effect_extraction} recipe.
 */
public class ExtractionEmiRecipe extends BasicEmiRecipe {
    private final EffectExtractionRecipe recipe;

    public ExtractionEmiRecipe(RecipeHolder<EffectExtractionRecipe> holder) {
        super(PotionsRebornEmiPlugin.EXTRACTION, holder.id(), 120, 44);
        this.recipe = holder.value();
        this.inputs.add(EmiIngredient.of(holder.value().ingredient()));
        this.outputs.add(EmiStack.of(ExtractionLookup.resultStack(holder.value())));
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 26, 5);
        widgets.addSlot(this.inputs.get(0), 0, 4);
        widgets.addSlot(this.outputs.get(0), 62, 4).recipeContext(this);
        String effects = this.recipe.effects().stream()
            .map(EffectEntry::effect)
            .map(holder -> holder.value().getDisplayName().getString())
            .collect(Collectors.joining(", "));
        widgets.addText(Component.literal(effects), 0, 26, 0xFFAAAAAA, true);
    }
}
