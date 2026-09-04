package com.puggicorn.potionsreborn.effect;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

/**
 * Melee attack goal for enraged mobs. Unlike vanilla, it works even for passive mobs whose
 * attribute map has no {@code ATTACK_DAMAGE}: the hit falls back to a flat 2 damage instead of
 * throwing. Damage is still scaled up by the Burning Rage attribute modifier when present.
 */
public class RageAttackGoal extends MeleeAttackGoal {
    private final PathfinderMob mob;
    private static final float FALLBACK_DAMAGE = 2.0F;
    /** Ticks a melee frenzy lasts before the mob may snap back to its weapon. */
    private static final int FRENZY_DURATION = 60;
    private int frenzyTicks;

    public RageAttackGoal(PathfinderMob mob, double speedModifier, boolean followUnseen) {
        super(mob, speedModifier, followUnseen);
        this.mob = mob;
    }

    /** True while this mob is in a melee frenzy (ranged goals should yield). */
    public boolean isFrenzied() {
        return this.frenzyTicks > 0;
    }

    /** Starts a melee frenzy, causing ranged goals to yield until it ends. */
    public void startFrenzy() {
        this.frenzyTicks = FRENZY_DURATION;
    }

    /** Counts down an active frenzy. Called once per tick from the rage event handler. */
    public void tickFrenzy() {
        if (this.frenzyTicks > 0 && !this.mob.hasEffect(ModEffects.BURNING_RAGE)) {
            this.frenzyTicks = 0;
        } else if (this.frenzyTicks > 0) {
            this.frenzyTicks--;
        }
    }

    @Override
    protected void checkAndPerformAttack(LivingEntity target) {
        if (this.canPerformAttack(target)) {
            this.resetAttackCooldown();
            this.mob.swing(InteractionHand.MAIN_HAND);
            if (this.mob.getAttributes().hasAttribute(Attributes.ATTACK_DAMAGE)) {
                this.mob.doHurtTarget(target);
            } else {
                target.hurt(this.mob.damageSources().mobAttack(this.mob), FALLBACK_DAMAGE);
            }
        }
    }

    // MeleeAttackGoal's canPerformAttack/resetAttackCooldown are protected; reuse them.
    @Override
    protected boolean canPerformAttack(LivingEntity entity) {
        return super.canPerformAttack(entity);
    }

    @Override
    protected void resetAttackCooldown() {
        super.resetAttackCooldown();
    }

    @Override
    public boolean canUse() {
        if (!this.mob.hasEffect(ModEffects.BURNING_RAGE)) {
            this.frenzyTicks = 0;
            return false;
        }
        return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.mob.hasEffect(ModEffects.BURNING_RAGE)) {
            this.frenzyTicks = 0;
            return false;
        }
        return super.canContinueToUse();
    }
}
