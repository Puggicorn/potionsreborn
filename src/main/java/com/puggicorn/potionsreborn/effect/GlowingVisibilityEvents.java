package com.puggicorn.potionsreborn.effect;

import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

/**
 * Glowing entities are detected at full hostile detection range (not reduced by sneaking, armor, or
 * invisibility), complementing {@link com.puggicorn.potionsreborn.mixin.SensingGlowingMixin} which
 * removes the line-of-sight requirement.
 */
public final class GlowingVisibilityEvents {
    private GlowingVisibilityEvents() {
    }

    @SubscribeEvent
    public static void onVisibility(LivingEvent.LivingVisibilityEvent event) {
        if (event.getEntity().hasEffect(MobEffects.GLOWING)) {
            // Force full visibility so the hostile detection range isn't shrunk by sneaking/armor.
            double current = event.getVisibilityModifier();
            if (current < 1.0D && current > 0.0D) {
                event.modifyVisibility(1.0D / current);
            }
        }
    }
}
