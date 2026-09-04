package com.puggicorn.potionsreborn.recipe;

import com.puggicorn.potionsreborn.PotionsRebornMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, PotionsRebornMod.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<EffectExtractionRecipe>> EFFECT_EXTRACTION = RECIPE_TYPES.register("effect_extraction",
        () -> RecipeType.simple(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(PotionsRebornMod.MODID, "effect_extraction")));

    private ModRecipeTypes() {
    }

    public static void register(IEventBus modEventBus) {
        RECIPE_TYPES.register(modEventBus);
    }
}
