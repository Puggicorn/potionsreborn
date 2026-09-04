package com.puggicorn.potionsreborn.effect.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Client-side enraged outburst for the local player. Runs on the client so the camera actually
 * turns and the arm actually swings. Smoothly turns toward a nearby living entity and punches it,
 * or (10% chance) turns to a nearby block and beats on it — with the real crack animation — until
 * it breaks or the burst ends.
 */
@OnlyIn(Dist.CLIENT)
public final class RagePlayerClientBehavior {
    private static final double ENTITY_RANGE = 4.0D;
    private static final double BLOCK_REACH = 4.0D;
    private static final float BLOCK_OUTBURST_CHANCE = 0.10F;
    /** Ticks spent hammering a block (~2 seconds). */
    private static final int BLOCK_PUNCH_DURATION = 40;
    /** Ticks before considering a new outburst while idle (~1 second). */
    private static final int OUTBURST_INTERVAL = 20;

    private enum Phase { NONE, TURNING, PUNCHING_ENTITY, PUNCHING_BLOCK }

    private static Phase phase = Phase.NONE;
    private static LivingEntity entityTarget;
    private static BlockPos blockTarget;
    private static int turnTicks;
    private static int punchTicks;
    private static float turnSpeed;

    private RagePlayerClientBehavior() {
    }

    /** Called every client tick. */
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            reset();
            return;
        }

        boolean enraged = player.hasEffect(com.puggicorn.potionsreborn.effect.ModEffects.BURNING_RAGE);
        if (!enraged || player.isSpectator() || player.isCreative()) {
            reset();
            return;
        }

        switch (phase) {
            case NONE -> {
                if (player.tickCount % OUTBURST_INTERVAL == 0) {
                    startOutburst(player);
                }
            }
            case TURNING -> tickTurning(player);
            case PUNCHING_ENTITY -> tickPunchingEntity(player, mc);
            case PUNCHING_BLOCK -> tickPunchingBlock(player, mc);
        }
    }

    private static void reset() {
        phase = Phase.NONE;
        entityTarget = null;
        blockTarget = null;
    }

    private static void startOutburst(LocalPlayer player) {
        float roll = player.getRandom().nextFloat();
        turnSpeed = roll < 0.02F ? 5.0F : (roll < 0.5F ? 10.0F : 20.0F);

        if (player.getRandom().nextFloat() < BLOCK_OUTBURST_CHANCE) {
            BlockPos block = pickBlock(player);
            if (block != null) {
                blockTarget = block;
                entityTarget = null;
                phase = Phase.TURNING;
                turnTicks = 0;
                return;
            }
        }

        LivingEntity entity = pickEntity(player);
        if (entity != null) {
            entityTarget = entity;
            blockTarget = null;
            phase = Phase.TURNING;
            turnTicks = 0;
        }
    }

    private static void tickTurning(LocalPlayer player) {
        Vec3 target = targetPoint(player);
        if (target == null) {
            phase = Phase.NONE;
            return;
        }
        faceToward(player, target, turnSpeed);
        turnTicks++;

        if (isFacing(player, target) || turnTicks > 40) {
            phase = entityTarget != null ? Phase.PUNCHING_ENTITY : Phase.PUNCHING_BLOCK;
            punchTicks = 0;
        }
    }

    private static void tickPunchingEntity(LocalPlayer player, Minecraft mc) {
        LivingEntity target = entityTarget;
        if (target == null || !target.isAlive() || player.distanceTo(target) > ENTITY_RANGE + 1.0D) {
            phase = Phase.NONE;
            entityTarget = null;
            return;
        }
        // Swing on the client (shows the arm) and let the client game mode deliver the hit.
        player.swing(InteractionHand.MAIN_HAND);
        if (mc.gameMode != null) {
            mc.gameMode.attack(player, target);
        }
        phase = Phase.NONE;
        entityTarget = null;
    }

    private static void tickPunchingBlock(LocalPlayer player, Minecraft mc) {
        BlockPos pos = blockTarget;
        if (pos == null) {
            phase = Phase.NONE;
            return;
        }
        BlockState block = mc.level.getBlockState(pos);
        if (block.isAir()) {
            phase = Phase.NONE;
            blockTarget = null;
            return;
        }

        // Real crack progress + swing: drive the client destroy loop on the block.
        Direction face = Direction.getNearest(player.getLookAngle().x, player.getLookAngle().y, player.getLookAngle().z);
        if (mc.gameMode != null) {
            if (punchTicks == 0) {
                mc.gameMode.startDestroyBlock(pos, face);
            } else {
                mc.gameMode.continueDestroyBlock(pos, face);
            }
        }
        punchTicks++;
        if (punchTicks >= BLOCK_PUNCH_DURATION) {
            phase = Phase.NONE;
            blockTarget = null;
        }
    }

    private static Vec3 targetPoint(LocalPlayer player) {
        if (entityTarget != null && entityTarget.isAlive()) {
            return entityTarget.getEyePosition();
        }
        if (blockTarget != null) {
            return Vec3.atCenterOf(blockTarget);
        }
        return null;
    }

    /** Nearest living entity in range (not creative/spectator). */
    private static LivingEntity pickEntity(LocalPlayer player) {
        return player.level().getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(ENTITY_RANGE),
                e -> e != player && e.isAlive() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(e))
            .stream()
            .min(java.util.Comparator.comparingDouble(player::distanceToSqr))
            .orElse(null);
    }

    /** Weighted block pick: prefer blocks near eye level / ahead, within reach. */
    private static BlockPos pickBlock(LocalPlayer player) {
        BlockPos eye = BlockPos.containing(player.getEyePosition());
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockPos best = null;
        double bestScore = Double.MAX_VALUE;
        int reach = (int)Math.ceil(BLOCK_REACH);
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -reach; dz <= reach; dz++) {
                    cursor.set(eye.getX() + dx, eye.getY() + dy, eye.getZ() + dz);
                    BlockState block = player.level().getBlockState(cursor);
                    if (block.isAir() || block.getDestroySpeed(player.level(), cursor) < 0.0F) {
                        continue;
                    }
                    double dist = Math.sqrt(cursor.distSqr(eye));
                    if (dist > BLOCK_REACH) {
                        continue;
                    }
                    double score = dist + Math.abs(dy) * 1.5;
                    if (score < bestScore) {
                        bestScore = score;
                        best = cursor.immutable();
                    }
                }
            }
        }
        return best;
    }

    private static void faceToward(LocalPlayer player, Vec3 target, float maxDegrees) {
        Vec3 eye = player.getEyePosition();
        Vec3 dir = target.subtract(eye);
        double horizontal = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
        float targetYaw = (float)(Mth.atan2(dir.z, dir.x) * (180.0 / Math.PI)) - 90.0F;
        float targetPitch = (float)(-(Mth.atan2(dir.y, horizontal) * (180.0 / Math.PI)));
        player.setYRot(Mth.approachDegrees(player.getYRot(), targetYaw, maxDegrees));
        player.setXRot(Mth.approachDegrees(player.getXRot(), targetPitch, maxDegrees));
        player.yHeadRot = player.getYRot();
    }

    private static boolean isFacing(LocalPlayer player, Vec3 target) {
        Vec3 dir = target.subtract(player.getEyePosition()).normalize();
        Vec3 look = player.getLookAngle().normalize();
        return look.dot(dir) > 0.995D;
    }
}
