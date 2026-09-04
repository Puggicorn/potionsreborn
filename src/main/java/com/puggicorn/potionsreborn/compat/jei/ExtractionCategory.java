package com.puggicorn.potionsreborn.compat.jei;

import com.puggicorn.potionsreborn.brewing.EffectEntry;
import com.puggicorn.potionsreborn.block.ModBlocks;
import com.puggicorn.potionsreborn.recipe.EffectExtractionRecipe;
import com.puggicorn.potionsreborn.recipe.ExtractionLookup;
import java.util.stream.Collectors;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * JEI category for {@code potionsreborn:effect_extraction} recipes: ingredient on the left, the
 * resulting extracted potion on the right, and the effect names listed below.
 */
public class ExtractionCategory extends AbstractRecipeCategory<EffectExtractionRecipe> {
    public ExtractionCategory(IGuiHelper guiHelper) {
        super(
            PotionsRebornJeiPlugin.EXTRACTION,
            Component.translatable("gui.potionsreborn.extraction"),
            guiHelper.createDrawableItemStack(new net.minecraft.world.item.ItemStack(ModBlocks.CENTRIFUGE.get())),
            116, 54);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, EffectExtractionRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(1, 19).setStandardSlotBackground().addIngredients(recipe.ingredient());
        builder.addOutputSlot(95, 19).setOutputSlotBackground().addItemStack(ExtractionLookup.resultStack(recipe));
    }

    @Override
    public void draw(EffectExtractionRecipe recipe, IRecipeSlotsView slotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        String effects = recipe.effects().stream()
            .map(EffectEntry::effect)
            .map(holder -> holder.value().getDisplayName().getString())
            .collect(Collectors.joining(", "));
        guiGraphics.drawString(Minecraft.getInstance().font, effects, 1, 40, 0xFF808080, false);
    }
}
