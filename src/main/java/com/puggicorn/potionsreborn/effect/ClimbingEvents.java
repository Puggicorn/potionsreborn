package com.puggicorn.potionsreborn.effect;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Applies the spider-like wall climbing granted by {@link ModEffects#CLIMBING}.
 * Runs on both sides so movement stays responsive and the server stays authoritative.
 */
public final class ClimbingEvents {
    private ClimbingEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.hasEffect(ModEffects.CLIMBING)) {
            return;
        }

        // Climb when pushing against a wall; crouch to cling in place.
        if (player.horizontalCollision && player.zza > 0.0F) {
            Vec3 movement = player.getDeltaMovement();
            double y = player.isShiftKeyDown() ? 0.0D : Math.max(movement.y, 0.2D);
            player.setDeltaMovement(movement.x, y, movement.z);
            player.fallDistance = 0.0F;
        }
    }
}
