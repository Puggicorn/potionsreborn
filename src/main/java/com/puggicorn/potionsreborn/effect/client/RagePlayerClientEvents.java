package com.puggicorn.potionsreborn.effect.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Drives the enraged player's outbursts on the client each tick.
 */
@OnlyIn(Dist.CLIENT)
public final class RagePlayerClientEvents {
    private RagePlayerClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        RagePlayerClientBehavior.tick();
    }
}
