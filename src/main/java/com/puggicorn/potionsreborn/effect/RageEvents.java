package com.puggicorn.potionsreborn.effect;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Drives the Burning Rage aggression behavior: enraged mobs hunt their attacker (pathfinding to it
 * even when unseen), then the nearest living thing, keeping the target until it dies or the rage
 * ends. When the rage wears off the mob is Stunned for 4 seconds. Enraged players have a small
 * chance to punch a mob they're staring at.
 */
public final class RageEvents {
    private static final int STUN_DURATION = 80; // 4 seconds
    /** Chance per second that an enraged ranged mob drops its weapon to melee. */
    private static final float FRENZY_CHANCE = 0.35F;

    /** Goals we injected per-mob, so they can be cleanly removed when the rage ends. */
    private static final Map<Mob, Goal[]> INJECTED = new WeakHashMap<>();
    /** The melee rage goal per-mob, so ranged goals can check the frenzy state. */
    private static final Map<Mob, RageAttackGoal> MELEE_GOALS = new WeakHashMap<>();
    /** The mob's own idle goals we suspended while enraged (goal -> original priority), restored on rage end. */
    private static final Map<Mob, java.util.Map<Goal, Integer>> SUSPENDED_IDLE = new WeakHashMap<>();
    /** Entities awaiting a deferred Stun application on their next tick (after a bulk effect clear). */
    private static final java.util.Set<java.util.UUID> PENDING_STUN = new java.util.HashSet<>();

    private RageEvents() {
    }

    /** @return true if this mob is currently in a melee frenzy (ranged goals should yield). */
    public static boolean isFrenzied(Mob mob) {
        RageAttackGoal goal = MELEE_GOALS.get(mob);
        return goal != null && goal.isFrenzied();
    }

    /** Injects the rage goals when the effect is applied to a mob. */
    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        if (!event.getEffectInstance().is(ModEffects.BURNING_RAGE)) {
            return;
        }
        if (event.getEntity() instanceof PathfinderMob mob) {
            injectRageGoals(mob);
        }
    }

    /**
     * Re-injects rage goals for mobs that (re)join the level already carrying the effect — this
     * keeps their behavior intact across world save/load, chunk reloads, and dimension travel,
     * since the runtime goals are not persisted in NBT.
     */
    @SubscribeEvent
    public static void onEntityJoinLevel(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof PathfinderMob mob) || event.getLevel().isClientSide) {
            return;
        }
        if (mob.hasEffect(ModEffects.BURNING_RAGE) && !INJECTED.containsKey(mob)) {
            injectRageGoals(mob);
        }
    }

    /** Injects the rage goal set, unless this mob already has them. */
    private static void injectRageGoals(PathfinderMob mob) {
        if (mob.level().isClientSide || INJECTED.containsKey(mob)) {
            return;
        }

        RageAttackGoal attack = new RageAttackGoal(mob, 1.2D, false);
        HuntAttackerGoal hunt = new HuntAttackerGoal(mob);
        RageWanderGoal wander = new RageWanderGoal(mob, 1.1D);
        NearestAttackableTargetGoal<LivingEntity> nearest = new NearestAttackableTargetGoal<>(
            mob, LivingEntity.class, 1, true, false,
            target -> target != mob && target.isAlive() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(target) && !target.isInvisible() && !(target instanceof ArmorStand));
        HurtByTargetGoal retaliate = new HurtByTargetGoal(mob);

        java.util.List<Goal> injected = new java.util.ArrayList<>();
        // Ranged attackers keep their weapon: a faster-firing bow/crossbow goal at higher priority
        // than melee, so they alternate between shooting and closing in for melee.
        java.util.List<Goal> ranged = RageRangedGoals.createFor(mob);
        for (Goal rangedGoal : ranged) {
            mob.goalSelector.addGoal(1, rangedGoal);
            injected.add(rangedGoal);
        }

        mob.goalSelector.addGoal(2, attack);
        mob.goalSelector.addGoal(3, hunt);
        mob.goalSelector.addGoal(4, wander);
        mob.targetSelector.addGoal(1, retaliate);
        mob.targetSelector.addGoal(2, nearest);
        injected.add(attack);
        injected.add(hunt);
        injected.add(wander);
        injected.add(nearest);
        injected.add(retaliate);
        INJECTED.put(mob, injected.toArray(new Goal[0]));
        MELEE_GOALS.put(mob, attack);

        // Suspend the mob's normal idle movement goals so the rage AI has sole control of
        // movement. Crucially this includes PanicGoal — Burning Rage sets the mob on fire, and the
        // vanilla "run around looking for water when burning" panic would otherwise override the
        // attack behavior entirely.
        java.util.Map<Goal, Integer> suspended = new java.util.HashMap<>();
        for (var wrapped : java.util.List.copyOf(mob.goalSelector.getAvailableGoals())) {
            Goal goal = wrapped.getGoal();
            if (goal instanceof net.minecraft.world.entity.ai.goal.RandomStrollGoal
                || goal instanceof net.minecraft.world.entity.ai.goal.RandomLookAroundGoal
                || goal instanceof net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
                || goal instanceof net.minecraft.world.entity.ai.goal.PanicGoal) {
                suspended.put(goal, wrapped.getPriority());
            }
        }
        for (Goal goal : suspended.keySet()) {
            mob.goalSelector.removeGoal(goal);
        }
        SUSPENDED_IDLE.put(mob, suspended);
    }

    /** Removes the rage goals and schedules the Stun cooldown when the effect expires or is cleared. */
    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        endRage(event.getEntity(), event.getEffectInstance(), false);
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        // Fired during effect-map iteration (e.g. milk), so the stun MUST be deferred to next tick
        // to avoid a ConcurrentModificationException.
        endRage(event.getEntity(), event.getEffectInstance(), true);
    }

    private static void endRage(LivingEntity entity, MobEffectInstance instance, boolean deferStun) {
        if (instance == null || !instance.is(ModEffects.BURNING_RAGE)) {
            return;
        }
        if (entity instanceof Mob mob && !entity.level().isClientSide) {
            Goal[] goals = INJECTED.remove(mob);
            MELEE_GOALS.remove(mob);
            if (goals != null) {
                for (Goal goal : goals) {
                    mob.goalSelector.removeGoal(goal);
                    mob.targetSelector.removeGoal(goal);
                }
            }
            mob.setTarget(null);
            mob.getNavigation().stop();
            // Restore the mob's suspended idle goals at their original priorities.
            java.util.Map<Goal, Integer> suspended = SUSPENDED_IDLE.remove(mob);
            if (suspended != null) {
                suspended.forEach((goal, priority) -> mob.goalSelector.addGoal(priority, goal));
            }
        }
        // Never re-add effects while the effect map is being mutated (milk / /effect clear).
        if (entity.level().isClientSide) {
            return;
        }
        if (deferStun) {
            PENDING_STUN.add(entity.getUUID());
        } else {
            applyStun(entity);
        }
    }

    private static void applyStun(LivingEntity entity) {
        // Apply the post-rage stun (4 seconds of doing nothing).
        entity.addEffect(new MobEffectInstance(ModEffects.STUN, STUN_DURATION, 0));
    }

    /** Restores AI when Stun ends. */
    @SubscribeEvent
    public static void onStunExpired(MobEffectEvent.Expired event) {
        clearStun(event.getEntity(), event.getEffectInstance());
    }

    @SubscribeEvent
    public static void onStunRemoved(MobEffectEvent.Remove event) {
        clearStun(event.getEntity(), event.getEffectInstance());
    }

    private static void clearStun(LivingEntity entity, MobEffectInstance instance) {
        if (instance != null && instance.is(ModEffects.STUN) && entity instanceof Mob mob) {
            mob.setNoAi(false);
        }
    }

    /** Triples damage dealt by enraged attackers. */
    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker
            && attacker.hasEffect(ModEffects.BURNING_RAGE)) {
            event.setAmount(event.getAmount() * 3.0F);
        }
    }

    /** Applies any deferred stuns at the start of the tick, once the effect map is stable. */
    @SubscribeEvent
    public static void onEntityTickPre(EntityTickEvent.Pre event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }
        if (event.getEntity() instanceof LivingEntity living
            && PENDING_STUN.remove(living.getUUID()) && !living.hasEffect(ModEffects.STUN)) {
            applyStun(living);
        }
    }

    /** Stunned players can't move: zero their horizontal velocity each tick. */
    @SubscribeEvent
    public static void onStunnedPlayerTick(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof Player player && player.hasEffect(ModEffects.STUN)) {
            player.setDeltaMovement(0.0D, player.getDeltaMovement().y, 0.0D);
            player.setJumping(false);
        }
    }

    /** Ticks down active melee frenzies and, once per second, may drop a ranged enraged mob into a frenzy. */
    @SubscribeEvent
    public static void onMobTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Mob mob) || mob.level().isClientSide) {
            return;
        }
        RageAttackGoal melee = MELEE_GOALS.get(mob);
        if (melee == null) {
            return;
        }
        melee.tickFrenzy();
        // Roll a new frenzy: an enraged ranged mob with a live target may lose itself to melee.
        if (!melee.isFrenzied() && RageState.isRangedMob(mob) && mob.getTarget() != null
            && mob.hasEffect(ModEffects.BURNING_RAGE)
            && mob.tickCount % 20 == 0 && mob.getRandom().nextFloat() < FRENZY_CHANCE) {
            melee.startFrenzy();
        }
    }

    // Enraged player outbursts are handled client-side (RagePlayerClientBehavior) so the camera
    // turn and arm swing are actually visible to the player.
}
