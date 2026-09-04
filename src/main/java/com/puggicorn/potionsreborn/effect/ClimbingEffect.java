package com.puggicorn.potionsreborn.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Lets the affected entity scale walls like a spider. The movement itself is handled
 * in {@link ClimbingEvents} since players have no climbable-block hook.
 */
public class ClimbingEffect extends MobEffect {
    public ClimbingEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x7A49A5);
    }
}
