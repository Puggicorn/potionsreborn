package com.puggicorn.potionsreborn.effect;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * While enraged, pathfinds toward the mob's last attacker even when it isn't visible, so the mob
 * "hunts" the attacker. Once in range / line of sight, the attack goal takes over.
 */
public class HuntAttackerGoal extends Goal {
    private final PathfinderMob mob;
    private LivingEntity attacker;

    public HuntAttackerGoal(PathfinderMob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!this.mob.hasEffect(ModEffects.BURNING_RAGE)) {
            return false;
        }
        this.attacker = this.mob.getLastHurtByMob();
        return this.attacker != null && this.attacker.isAlive() && this.attacker != this.mob;
    }

    @Override
    public boolean canContinueToUse() {
        return this.mob.hasEffect(ModEffects.BURNING_RAGE)
            && this.attacker != null
            && this.attacker.isAlive()
            && !this.mob.getNavigation().isDone();
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.attacker, 1.1);
    }

    @Override
    public void tick() {
        if (this.attacker != null && this.mob.getNavigation().isDone()) {
            this.mob.getNavigation().moveTo(this.attacker, 1.1);
        }
    }

    @Override
    public void stop() {
        this.attacker = null;
        this.mob.getNavigation().stop();
    }
}
