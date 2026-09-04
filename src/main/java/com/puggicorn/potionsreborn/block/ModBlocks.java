package com.puggicorn.potionsreborn.block;

import com.puggicorn.potionsreborn.PotionsRebornMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(PotionsRebornMod.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(PotionsRebornMod.MODID);

    public static final DeferredBlock<CentrifugeBlock> CENTRIFUGE = BLOCKS.registerBlock("centrifuge",
        CentrifugeBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.BREWING_STAND));
    public static final DeferredItem<BlockItem> CENTRIFUGE_ITEM = ITEMS.registerSimpleBlockItem("centrifuge", CENTRIFUGE);

    private ModBlocks() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
    }
}
