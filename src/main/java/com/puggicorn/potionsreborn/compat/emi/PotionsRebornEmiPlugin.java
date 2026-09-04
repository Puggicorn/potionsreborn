package com.puggicorn.potionsreborn.compat.emi;

import com.puggicorn.potionsreborn.PotionsRebornMod;
import com.puggicorn.potionsreborn.block.ModBlocks;
import com.puggicorn.potionsreborn.recipe.EffectExtractionRecipe;
import com.puggicorn.potionsreborn.recipe.ModRecipeTypes;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;

@EmiEntrypoint
public class PotionsRebornEmiPlugin implements EmiPlugin {
    public static final EmiRecipeCategory EXTRACTION = new EmiRecipeCategory(
        ResourceLocation.fromNamespaceAndPath(PotionsRebornMod.MODID, "effect_extraction"),
        EmiStack.of(ModBlocks.CENTRIFUGE.get()));

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(EXTRACTION);
        registry.addWorkstation(EXTRACTION, EmiStack.of(Items.BREWING_STAND));
        registry.addWorkstation(EXTRACTION, EmiStack.of(ModBlocks.CENTRIFUGE.get()));

        for (RecipeHolder<EffectExtractionRecipe> holder : registry.getRecipeManager().getAllRecipesFor(ModRecipeTypes.EFFECT_EXTRACTION.get())) {
            registry.addRecipe(new ExtractionEmiRecipe(holder));
        }
    }
}
