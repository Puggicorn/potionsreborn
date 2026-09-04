package com.puggicorn.potionsreborn;

import com.puggicorn.potionsreborn.client.CentrifugeScreen;
import com.puggicorn.potionsreborn.menu.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = PotionsRebornMod.MODID, dist = Dist.CLIENT)
public class PotionsRebornModClient {
    public PotionsRebornModClient(IEventBus modEventBus, ModContainer container) {
        modEventBus.addListener((RegisterMenuScreensEvent event) ->
            event.register(ModMenus.CENTRIFUGE.get(), CentrifugeScreen::new));
    }
}
