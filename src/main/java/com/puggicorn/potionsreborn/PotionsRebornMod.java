package com.puggicorn.potionsreborn;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.puggicorn.potionsreborn.block.ModBlocks;
import com.puggicorn.potionsreborn.block.entity.ModBlockEntities;
import com.puggicorn.potionsreborn.effect.ClimbingEvents;
import com.puggicorn.potionsreborn.effect.GlowingVisibilityEvents;
import com.puggicorn.potionsreborn.effect.ModEffects;
import com.puggicorn.potionsreborn.effect.RageEvents;
import com.puggicorn.potionsreborn.item.ModItems;
import com.puggicorn.potionsreborn.menu.ModMenus;
import com.puggicorn.potionsreborn.network.ModNetworking;
import com.puggicorn.potionsreborn.potion.ModPotions;
import com.puggicorn.potionsreborn.recipe.ModRecipeSerializers;
import com.puggicorn.potionsreborn.recipe.ModRecipeTypes;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(PotionsRebornMod.MODID)
public class PotionsRebornMod {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "potionsreborn";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public PotionsRebornMod(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenus.register(modEventBus);
        ModEffects.register(modEventBus);
        ModPotions.register(modEventBus);
        ModItems.register(modEventBus);
        ModRecipeTypes.register(modEventBus);
        ModRecipeSerializers.register(modEventBus);

        modEventBus.addListener(ModNetworking::registerPayloads);
        modEventBus.addListener(this::addCreative);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        NeoForge.EVENT_BUS.register(ClimbingEvents.class);
        NeoForge.EVENT_BUS.register(RageEvents.class);
        NeoForge.EVENT_BUS.register(GlowingVisibilityEvents.class);
    }

    // Add the centrifuge and breeze powder to their creative tabs
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModBlocks.CENTRIFUGE_ITEM);
        } else if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.BREEZE_POWDER);
        }
    }
}
