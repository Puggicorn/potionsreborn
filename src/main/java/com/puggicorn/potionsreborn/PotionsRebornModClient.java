package com.puggicorn.potionsreborn;

import com.puggicorn.potionsreborn.client.CentrifugeScreen;
import com.puggicorn.potionsreborn.client.CentrifugeRenderer;
import com.puggicorn.potionsreborn.block.entity.ModBlockEntities;
import com.puggicorn.potionsreborn.effect.client.RagePlayerClientEvents;
import com.puggicorn.potionsreborn.menu.ModMenus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = PotionsRebornMod.MODID, dist = Dist.CLIENT)
public class PotionsRebornModClient {
    public PotionsRebornModClient(IEventBus modEventBus, ModContainer container) {
        modEventBus.addListener((RegisterMenuScreensEvent event) ->
            event.register(ModMenus.CENTRIFUGE.get(), CentrifugeScreen::new));
        modEventBus.addListener((ModelEvent.RegisterAdditional event) -> event.register(CentrifugeRenderer.ROTOR_MODEL));
        modEventBus.addListener((EntityRenderersEvent.RegisterRenderers event) ->
            event.registerBlockEntityRenderer(ModBlockEntities.CENTRIFUGE.get(), context -> new CentrifugeRenderer()));

        NeoForge.EVENT_BUS.register(RagePlayerClientEvents.class);
    }
}
