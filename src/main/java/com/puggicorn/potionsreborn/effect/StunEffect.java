package com.puggicorn.potionsreborn.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/**
 * Applied after Burning Rage wears off. Prevents the mob from doing anything by toggling NoAI
 * while active; restored when the effect is removed or expires (handled in {@link RageEvents}).
 */
public class StunEffect extends MobEffect {
    public StunEffect() {
        super(MobEffectCategory.HARMFUL, 0x555555);
    }

    @Override
    public void onEffectAdded(LivingEntity entity, int amplifier) {
        if (entity instanceof Mob mob) {
            mob.setNoAi(true);
        }
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false; // NoAI does all the work; no per-tick logic needed.
    }
}
