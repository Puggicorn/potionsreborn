package com.puggicorn.potionsreborn.network;

import com.puggicorn.potionsreborn.PotionsRebornMod;
import com.puggicorn.potionsreborn.menu.CentrifugeMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Sent from the client when the player presses the Centrifuge start/stop button.
 */
public record CentrifugeStartPayload(boolean running) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CentrifugeStartPayload> TYPE = new CustomPacketPayload.Type<>(
        ResourceLocation.fromNamespaceAndPath(PotionsRebornMod.MODID, "centrifuge_start"));

    public static final StreamCodec<FriendlyByteBuf, CentrifugeStartPayload> STREAM_CODEC = StreamCodec.of(
        (buf, payload) -> buf.writeBoolean(payload.running),
        buf -> new CentrifugeStartPayload(buf.readBoolean()));

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CentrifugeStartPayload payload, net.neoforged.neoforge.network.handling.IPayloadContext context) {
        if (context.player().containerMenu instanceof CentrifugeMenu menu) {
            menu.setRunning(payload.running());
        }
    }
}
