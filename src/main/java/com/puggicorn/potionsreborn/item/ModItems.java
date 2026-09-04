package com.puggicorn.potionsreborn.item;

import com.puggicorn.potionsreborn.PotionsRebornMod;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(PotionsRebornMod.MODID);

    /** Ground breeze rod. Used as fuel for the Centrifuge and as a brewing ingredient. */
    public static final DeferredItem<Item> BREEZE_POWDER = ITEMS.registerSimpleItem("breeze_powder");

    private ModItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
