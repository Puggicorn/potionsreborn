package com.puggicorn.potionsreborn.effect;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.RangedAttackMob;

/**
 * Shared helpers for the Burning Rage behavior, so the melee and ranged rage goals can coordinate
 * (melee frenzy overrides ranged behavior).
 */
public final class RageState {
    private RageState() {
    }

    /** @return true if this mob has a usual ranged attack pattern (bow or crossbow). */
    public static boolean isRangedMob(Mob mob) {
        return mob instanceof RangedAttackMob || mob instanceof CrossbowAttackMob;
    }
}
