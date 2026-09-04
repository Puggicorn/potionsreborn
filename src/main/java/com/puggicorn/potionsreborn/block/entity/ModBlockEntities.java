package com.puggicorn.potionsreborn.block.entity;

import com.puggicorn.potionsreborn.PotionsRebornMod;
import com.puggicorn.potionsreborn.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, PotionsRebornMod.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CentrifugeBlockEntity>> CENTRIFUGE = BLOCK_ENTITY_TYPES.register("centrifuge",
        () -> BlockEntityType.Builder.of(CentrifugeBlockEntity::new, ModBlocks.CENTRIFUGE.get()).build(null));

    private ModBlockEntities() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}
