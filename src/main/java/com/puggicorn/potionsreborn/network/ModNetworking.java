package com.puggicorn.potionsreborn.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registers the mod's network payloads.
 */
public final class ModNetworking {
    private ModNetworking() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(CentrifugeStartPayload.TYPE, CentrifugeStartPayload.STREAM_CODEC, CentrifugeStartPayload::handle);
    }
}
