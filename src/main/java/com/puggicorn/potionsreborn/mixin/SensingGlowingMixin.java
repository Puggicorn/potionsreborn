package com.puggicorn.potionsreborn.mixin;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.sensing.Sensing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Glowing entities are visible to hostiles even through walls: the line-of-sight check used by
 * target selection reports true for a glowing target. This makes mobs that are already hostile
 * toward a target type (e.g. zombies toward players, endermen toward endermites) lock on to a
 * glowing one regardless of obstruction.
 */
@Mixin(Sensing.class)
public abstract class SensingGlowingMixin {
    @Inject(method = "hasLineOfSight", at = @At("HEAD"), cancellable = true)
    private void potionsreborn$glowingBypassesLineOfSight(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof LivingEntity living && living.hasEffect(MobEffects.GLOWING)) {
            cir.setReturnValue(true);
        }
    }
}
