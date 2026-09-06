package com.puggicorn.potionsreborn.brewing;

import com.puggicorn.potionsreborn.potion.ModPotions;
import com.puggicorn.potionsreborn.recipe.EffectExtractionRecipe;
import com.puggicorn.potionsreborn.recipe.ExtractionLookup;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;

/**
 * Shared logic for the extraction brewing system.
 */
public final class RebornBrewingLogic {
    private RebornBrewingLogic() {
    }

    /**
     * @return the datapack-defined extraction recipe for this ingredient, or null if none matches
     */
    @Nullable
    public static EffectExtractionRecipe getRecipe(Level level, ItemStack stack) {
        return ExtractionLookup.find(level, stack);
    }

    /**
     * @return true if the stack can receive extracted effects. Requires a potion that already has a
     * base potion (an awkward potion, or any potion that already carries a base), so the vanilla
     * Nether Wart step is still required. Plain water bottles and empty glass bottles do not work.
     */
    public static boolean canExtractOnto(ItemStack stack, PotionBrewing brewing) {
        if (stack.isEmpty() || !brewing.isInput(stack)) {
            return false;
        }
        PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        // Exclude plain water bottles and empty bottles; require a non-water base potion.
        return contents.potion().isPresent()
            && !contents.potion().get().is(net.minecraft.world.item.alchemy.Potions.WATER.unwrapKey().get());
    }

    /**
     * Brews the recipe's effects onto the given stack. Existing effects are preserved;
     * effects of the same type are replaced by the newly extracted ones.
     */
    public static ItemStack brewOnto(ItemStack stack, EffectExtractionRecipe recipe) {
        ItemStack result = stack.copy();

        PotionContents oldContents = result.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        List<MobEffectInstance> merged = new ArrayList<>();
        oldContents.forEachEffect(merged::add);

        for (EffectEntry entry : recipe.effects()) {
            merged.removeIf(instance -> instance.getEffect().is(entry.effect().getKey()));
            merged.add(entry.createInstance());
        }

        result.set(DataComponents.POTION_CONTENTS, new PotionContents(
            Optional.<Holder<Potion>>of(ModPotions.EXTRACTED), Optional.empty(), merged));
        return result;
    }

    /**
     * @return whether this is one of this mod's multi-effect extracted potions with custom effects.
     */
    public static boolean hasExtractedEffects(ItemStack stack) {
        PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        return contents.potion().isPresent()
            && contents.potion().get().is(ModPotions.EXTRACTED.getKey())
            && !contents.customEffects().isEmpty();
    }

    /**
     * Checks whether a vanilla modifier can be applied to an Extracted Potion without losing its
     * custom effects. Other ingredients remain the responsibility of vanilla/modded brewing.
     */
    public static boolean canModifyExtracted(ItemStack potion, ItemStack ingredient) {
        if (!hasExtractedEffects(potion)) {
            return false;
        }
        return ingredient.is(Items.REDSTONE)
            || ingredient.is(Items.GLOWSTONE_DUST)
            || ingredient.is(Items.GUNPOWDER)
            || ingredient.is(Items.DRAGON_BREATH) && potion.is(Items.SPLASH_POTION);
    }

    /**
     * Applies a vanilla-style modifier to an Extracted Potion. The original ItemStack is copied,
     * so custom potion contents and all unrelated data survive splash/lingering conversion.
     */
    public static ItemStack modifyExtracted(ItemStack potion, ItemStack ingredient) {
        if (ingredient.is(Items.GUNPOWDER)) {
            return potion.transmuteCopy(Items.SPLASH_POTION);
        }
        if (ingredient.is(Items.DRAGON_BREATH)) {
            return potion.transmuteCopy(Items.LINGERING_POTION);
        }

        PotionContents contents = potion.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        List<MobEffectInstance> modified = new ArrayList<>();
        for (MobEffectInstance effect : contents.customEffects()) {
            if (effect.getEffect().value().isInstantenous()) {
                modified.add(effect);
            } else if (ingredient.is(Items.REDSTONE)) {
                // Match the usual long-potion increase (3 minutes to 8 minutes).
                modified.add(new MobEffectInstance(effect.getEffect(), effect.getDuration() * 8 / 3, effect.getAmplifier()));
            } else {
                // Glowstone makes the effect stronger but shorter, as normal strong potions do.
                modified.add(new MobEffectInstance(effect.getEffect(), Math.max(1, effect.getDuration() / 2), effect.getAmplifier() + 1));
            }
        }

        ItemStack result = potion.copy();
        result.set(DataComponents.POTION_CONTENTS, new PotionContents(
            contents.potion(), contents.customColor(), modified));
        return result;
    }
}
