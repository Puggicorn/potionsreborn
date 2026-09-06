package com.puggicorn.potionsreborn.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.Wolf;

/**
 * Sets the user ablaze in exchange for speed and greatly increased attack damage (3×).
 * Re-ignites every tick so the effect cannot be trivially cancelled by water.
 */
public class BurningRageEffect extends MobEffect {
    private static final ResourceLocation SPEED_ID = ResourceLocation.fromNamespaceAndPath("potionsreborn", "burning_rage_speed");

    /** Whether this variant sets the entity on fire. Fixed per effect instance (effects are singletons). */
    private final boolean burning;

    public BurningRageEffect() {
        this(true);
    }

    public BurningRageEffect(boolean burning) {
        super(MobEffectCategory.HARMFUL, 0xE25822);
        this.burning = burning;
    }

    public BurningRageEffect withModifiers() {
        // +40% movement speed. The 3x attack damage is applied via LivingIncomingDamageEvent in
        // RageEvents so it also works for passive mobs that lack an ATTACK_DAMAGE attribute.
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, SPEED_ID, 0.4, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        return this;
    }

    /** A variant that rages without setting the entity on fire. */
    public static BurningRageEffect burnless() {
        return new BurningRageEffect(false);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        // When wet (in water or under rain) the fire can't catch, so we skip the constant ignition
        // (and its sizzling loop) and just vent steamy, fiery particles instead.
        if (entity.isInWaterOrRain() || !burning) {
            if (entity.tickCount % 30 == 0 && entity.level() instanceof ServerLevel level) {
                spawnWetParticles(level, entity, burning);
            }
            return true;
        }

        // Keep the entity burning for as long as the rage lasts.
        if (burning && (!entity.isOnFire() || entity.getRemainingFireTicks() < 20)) {
            entity.igniteForTicks(40);
        }
        return true;
    }

    /** Puffs of smoke, flame and lava sparks around a raging mob that's too wet to burn. */
    private static void spawnWetParticles(ServerLevel level, LivingEntity entity, boolean burning) {
        double x = entity.getX();
        double y = entity.getY() + entity.getBbHeight() * 0.6D;
        double z = entity.getZ();
        var random = entity.getRandom();
        for (int i = 0; i < 4; i++) {
            double ox = (random.nextDouble() - 0.5D) * entity.getBbWidth();
            double oy = random.nextDouble() * entity.getBbHeight();
            double oz = (random.nextDouble() - 0.5D) * entity.getBbWidth();
            // Burning-but-wet: smoke/flame/lava in equal thirds. Burnless: angry villager dominates
            // (~75%), with the occasional smoke wisp for flavor.
            var type = burning
                ? switch (random.nextInt(3)) {
                    case 0 -> ParticleTypes.LARGE_SMOKE;
                    case 1 -> ParticleTypes.FLAME;
                    default -> ParticleTypes.LAVA;
                }
                : random.nextInt(4) == 0 ? ParticleTypes.LARGE_SMOKE : ParticleTypes.ANGRY_VILLAGER;
            level.sendParticles(type, x + ox, y + oy, z + oz, 1, 0.0D, 0.05D, 0.0D, 0.0D);
        }
    }
}
