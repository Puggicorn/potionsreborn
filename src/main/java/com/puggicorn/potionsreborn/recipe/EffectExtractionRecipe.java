package com.puggicorn.potionsreborn.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.puggicorn.potionsreborn.brewing.EffectEntry;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

/**
 * A recipe defining which effects a single brewing ingredient extracts. Loaded from
 * {@code data/<namespace>/recipe/<id>.json} with type {@code potionsreborn:effect_extraction},
 * so each ingredient gets its own file and its own recipe ID.
 */
public record EffectExtractionRecipe(Ingredient ingredient, List<EffectEntry> effects) implements Recipe<SingleRecipeInput> {
    public static final int MAX_EFFECTS = 3;

    public List<net.minecraft.world.effect.MobEffectInstance> createInstances() {
        return this.effects.stream().map(EffectEntry::createInstance).toList();
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return this.ingredient.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, this.ingredient);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.EFFECT_EXTRACTION.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.EFFECT_EXTRACTION.get();
    }

    public static class Serializer implements RecipeSerializer<EffectExtractionRecipe> {
        private static final MapCodec<EffectExtractionRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(EffectExtractionRecipe::ingredient),
            EffectEntry.CODEC.listOf(1, MAX_EFFECTS).fieldOf("effects").forGetter(EffectExtractionRecipe::effects))
            .apply(instance, EffectExtractionRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, EffectExtractionRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            EffectExtractionRecipe::ingredient,
            EffectEntry.STREAM_CODEC.apply(ByteBufCodecs.list()),
            EffectExtractionRecipe::effects,
            EffectExtractionRecipe::new);

        @Override
        public MapCodec<EffectExtractionRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, EffectExtractionRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
