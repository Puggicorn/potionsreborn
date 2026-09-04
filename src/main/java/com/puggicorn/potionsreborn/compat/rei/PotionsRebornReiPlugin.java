package com.puggicorn.potionsreborn.compat.rei;

import com.puggicorn.potionsreborn.PotionsRebornMod;
import com.puggicorn.potionsreborn.block.ModBlocks;
import com.puggicorn.potionsreborn.recipe.EffectExtractionRecipe;
import com.puggicorn.potionsreborn.recipe.ModRecipeTypes;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.forge.REIPluginClient;
import net.minecraft.world.item.Items;

@REIPluginClient
public class PotionsRebornReiPlugin implements REIClientPlugin {
    public static final CategoryIdentifier<ExtractionDisplay> EXTRACTION =
        CategoryIdentifier.of(PotionsRebornMod.MODID, "effect_extraction");

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new ExtractionCategory());
        registry.addWorkstations(EXTRACTION, EntryStacks.of(Items.BREWING_STAND), EntryStacks.of(ModBlocks.CENTRIFUGE.get()));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerRecipeFiller(EffectExtractionRecipe.class, ModRecipeTypes.EFFECT_EXTRACTION.get(), ExtractionDisplay::new);
    }
}
