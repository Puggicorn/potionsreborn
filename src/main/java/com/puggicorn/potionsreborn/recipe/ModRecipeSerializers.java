package com.puggicorn.potionsreborn.recipe;

import com.puggicorn.potionsreborn.PotionsRebornMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, PotionsRebornMod.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<EffectExtractionRecipe>> EFFECT_EXTRACTION = RECIPE_SERIALIZERS.register("effect_extraction",
        EffectExtractionRecipe.Serializer::new);

    private ModRecipeSerializers() {
    }

    public static void register(IEventBus modEventBus) {
        RECIPE_SERIALIZERS.register(modEventBus);
    }
}
