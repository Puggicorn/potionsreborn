package com.puggicorn.potionsreborn.effect;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

/**
 * Directional search for an enraged mob with nothing to chase. Picks a compass direction and walks
 * it until the path runs out or a random duration elapses, then picks a new direction. Abandons the
 * moment a target or attacker appears, so attack always wins.
 */
public class RageWanderGoal extends Goal {
    /** How far ahead (in blocks) each leg of the search tries to reach. */
    private static final double LEG_DISTANCE = 12.0D;
    /** Min/max ticks a mob commits to one direction before turning. */
    private static final int MIN_LEG_TICKS = 60;
    private static final int MAX_LEG_TICKS = 160;

    private final PathfinderMob mob;
    private final double speedModifier;
    private double directionX;
    private double directionZ;
    private int legTicks;

    public RageWanderGoal(PathfinderMob mob, double speedModifier) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // Only search while enraged with nothing to chase.
        return this.mob.hasEffect(ModEffects.BURNING_RAGE)
            && this.mob.getTarget() == null
            && this.mob.getLastHurtByMob() == null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void start() {
        this.pickNewDirection();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.legTicks--;
        // Turn to a new direction when the current leg ran out of time or the path is blocked/done.
        if (this.legTicks <= 0 || this.mob.getNavigation().isDone()) {
            this.pickNewDirection();
        }
    }

    @Override
    public void stop() {
        this.mob.getNavigation().stop();
    }

    /** Picks a random compass direction and starts pathfinding along it. */
    private void pickNewDirection() {
        double angle = this.mob.getRandom().nextDouble() * Math.PI * 2.0D;
        this.directionX = Math.cos(angle);
        this.directionZ = Math.sin(angle);
        this.legTicks = MIN_LEG_TICKS + this.mob.getRandom().nextInt(MAX_LEG_TICKS - MIN_LEG_TICKS);

        BlockPos base = this.mob.blockPosition();
        BlockPos target = base.offset(
            (int)(this.directionX * LEG_DISTANCE),
            0,
            (int)(this.directionZ * LEG_DISTANCE));

        // Try to path to the target; if unreachable, nudge around the search height a few times.
        Path path = this.mob.getNavigation().createPath(target, 0);
        if (path == null) {
            for (int dy = 0; dy <= 3 && path == null; dy++) {
                path = this.mob.getNavigation().createPath(target.above(dy), 0);
            }
            for (int dy = 1; dy <= 3 && path == null; dy++) {
                path = this.mob.getNavigation().createPath(target.below(dy), 0);
            }
        }

        if (path != null) {
            this.mob.getNavigation().moveTo(path, this.speedModifier);
        } else {
            // No path found this way; give up on this leg early so a new direction is tried soon.
            this.legTicks = Math.min(this.legTicks, 10);
        }

        // Face the direction of travel.
        Vec3 look = Vec3.atCenterOf(target);
        this.mob.getLookControl().setLookAt(look.x, this.mob.getEyeY(), look.z);
    }
}
