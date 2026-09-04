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
}
