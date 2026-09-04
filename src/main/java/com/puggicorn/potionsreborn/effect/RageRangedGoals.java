package com.puggicorn.potionsreborn.effect;

import java.util.List;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.RangedAttackMob;

/**
 * Ranged attackers keep their identity while enraged: a faster-firing copy of their usual
 * bow/crossbow attack is injected, coexisting with the melee goal (melee covers close range).
 */
final class RageRangedGoals {
    /** Vanilla bow interval is 20 ticks; rage halves it for faster firing. */
    private static final int RAGE_BOW_INTERVAL = 8;

    private RageRangedGoals() {
    }

    /**
     * @return a faster ranged-attack goal appropriate to the mob's weapon, or an empty list if the
     * mob has no ranged attack pattern.
     */
    static List<Goal> createFor(PathfinderMob mob) {
        if (mob instanceof CrossbowAttackMob) {
            // Pillagers / crossbow piglins.
            return List.of(new RagedCrossbowGoal(mob, 1.1D, 16.0F));
        }
        if (mob instanceof RangedAttackMob) {
            // Skeletons and other bow users.
            return List.of(new RagedBowGoal<>(castMob(mob), 1.1D, RAGE_BOW_INTERVAL, 16.0F));
        }
        return List.of();
    }

    @SuppressWarnings("unchecked")
    private static <T extends Mob & RangedAttackMob> T castMob(PathfinderMob mob) {
        return (T) mob;
    }

    /** Bow goal with a shortened attack interval; only active while enraged. */
    private static final class RagedBowGoal<T extends Mob & RangedAttackMob> extends RangedBowAttackGoal<T> {
        private final Mob ragedMob;

        RagedBowGoal(T mob, double speedModifier, int attackInterval, float attackRadius) {
            super(mob, speedModifier, attackInterval, attackRadius);
            this.ragedMob = mob;
        }

        @Override
        public boolean canUse() {
            return this.ragedMob.hasEffect(ModEffects.BURNING_RAGE) && !RageEvents.isFrenzied(this.ragedMob) && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return this.ragedMob.hasEffect(ModEffects.BURNING_RAGE) && !RageEvents.isFrenzied(this.ragedMob) && super.canContinueToUse();
        }
    }

    /** Crossbow goal that only activates while enraged. */
    private static final class RagedCrossbowGoal extends RangedCrossbowAttackGoal {
        private final Mob ragedMob;

        @SuppressWarnings("unchecked")
        RagedCrossbowGoal(PathfinderMob mob, double speedModifier, float attackRadius) {
            super((Mob & CrossbowAttackMob) mob, speedModifier, attackRadius);
            this.ragedMob = mob;
        }

        @Override
        public boolean canUse() {
            return this.ragedMob.hasEffect(ModEffects.BURNING_RAGE) && !RageEvents.isFrenzied(this.ragedMob) && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return this.ragedMob.hasEffect(ModEffects.BURNING_RAGE) && !RageEvents.isFrenzied(this.ragedMob) && super.canContinueToUse();
        }
    }
}
