package com.puggicorn.potionsreborn.compat.rei;

import com.puggicorn.potionsreborn.block.ModBlocks;
import java.util.ArrayList;
import java.util.List;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;

/**
 * REI category rendering for {@code effect_extraction}: ingredient, arrow, extracted potion output,
 * and the effect names along the bottom.
 */
public class ExtractionCategory implements DisplayCategory<ExtractionDisplay> {
    @Override
    public CategoryIdentifier<ExtractionDisplay> getCategoryIdentifier() {
        return PotionsRebornReiPlugin.EXTRACTION;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.potionsreborn.extraction");
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(ModBlocks.CENTRIFUGE.get());
    }

    @Override
    public int getDisplayHeight() {
        return 44;
    }

    @Override
    public List<Widget> setupDisplay(ExtractionDisplay display, Rectangle bounds) {
        Point start = new Point(bounds.getCenterX() - 58, bounds.getCenterY() - 14);
        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));
        widgets.add(Widgets.createSlot(start).entries(display.getInputEntries().get(0)).markInput());
        widgets.add(Widgets.createArrow(new Point(start.x + 26, start.y)));
        widgets.add(Widgets.createSlot(new Point(start.x + 60, start.y)).entries(display.getOutputEntries().get(0)).markOutput());

        String effects = display.getRecipeEffects();
        widgets.add(Widgets.createLabel(new Point(bounds.getMaxX() - 5, bounds.getMaxY() - 12),
            Component.literal(effects)).noShadow().color(0xFF606060, 0xFFBBBBBB).rightAligned());
        return widgets;
    }
}
