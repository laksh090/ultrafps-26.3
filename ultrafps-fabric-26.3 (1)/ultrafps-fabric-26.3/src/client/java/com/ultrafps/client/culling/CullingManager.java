package com.ultrafps.client.culling;

import com.ultrafps.client.config.UltraFpsConfig;
import it.unimi.dsi.fastutil.ints.Int2ByteOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * High-speed entity and block-entity visibility evaluator with tick-cached raycast occlusion.
 */
public final class CullingManager {
    private static final Int2LongOpenHashMap LAST_CHECK_TICK = new Int2LongOpenHashMap();
    private static final Int2ByteOpenHashMap OCCLUSION_STATE = new Int2ByteOpenHashMap();

    private static int entitiesCulledThisSecond = 0;
    private static int blockEntitiesCulledThisSecond = 0;
    private static int particlesSkippedThisSecond = 0;
    private static int lastSnapshotEntities = 0;
    private static int lastSnapshotBlockEntities = 0;
    private static int lastSnapshotParticles = 0;
    private static long lastResetTimeMs = System.currentTimeMillis();

    private CullingManager() {
    }

    public static void tickTelemetry() {
        long now = System.currentTimeMillis();
        if (now - lastResetTimeMs >= 1000L) {
            lastSnapshotEntities = entitiesCulledThisSecond;
            lastSnapshotBlockEntities = blockEntitiesCulledThisSecond;
            lastSnapshotParticles = particlesSkippedThisSecond;
            entitiesCulledThisSecond = 0;
            blockEntitiesCulledThisSecond = 0;
            particlesSkippedThisSecond = 0;
            lastResetTimeMs = now;
        }
    }

    public static void clearCache() {
        LAST_CHECK_TICK.clear();
        OCCLUSION_STATE.clear();
    }

    public static void recordParticleSkipped() {
        particlesSkippedThisSecond++;
    }

    public static int getLastSnapshotEntities() {
        return lastSnapshotEntities;
    }

    public static int getLastSnapshotBlockEntities() {
        return lastSnapshotBlockEntities;
    }

    public static int getLastSnapshotParticles() {
        return lastSnapshotParticles;
    }

    /**
     * Determines whether an Entity should be skipped during the current render pass.
     */
    public static boolean shouldCullEntity(Entity entity, Vec3 cameraPos) {
        UltraFpsConfig cfg = UltraFpsConfig.get();
        if (!cfg.enabled || entity == null) {
            return false;
        }

        // Never cull players, custom-named entities, glowing entities, bosses, or ridden entities
        if (entity instanceof Player
                || entity.hasCustomName()
                || entity.isCurrentlyGlowing()
                || entity.isPassenger()
                || entity.isVehicle()
                || !entity.canChangeDimensions(entity.level(), entity.level())) {
            return false;
        }

        double distSq = entity.distanceToSqr(cameraPos);

        // 1. Category-specific squared distance culling
        if (cfg.entityDistanceCulling) {
            double maxDist = getMaxDistanceForEntity(entity, cfg);
            if (distSq > maxDist * maxDist) {
                entitiesCulledThisSecond++;
                return true;
            }
        }

        // 2. Cached 2-point bounding-box raycast occlusion culling (skip entities > 12 blocks away behind solid walls)
        if (cfg.entityOcclusionCulling && distSq > 144.0) {
            Minecraft mc = Minecraft.getInstance();
            Level level = mc.level;
            if (level != null && mc.player != null) {
                long gameTime = level.getGameTime();
                int id = entity.getId();
                long lastTick = LAST_CHECK_TICK.getOrDefault(id, -999L);

                if (gameTime - lastTick < cfg.occlusionCacheTicks) {
                    boolean cachedCulled = OCCLUSION_STATE.get(id) == (byte) 1;
                    if (cachedCulled) {
                        entitiesCulledThisSecond++;
                    }
                    return cachedCulled;
                }

                boolean occluded = isEntityBoxOccluded(level, mc.player, cameraPos, entity.getBoundingBox());
                LAST_CHECK_TICK.put(id, gameTime);
                OCCLUSION_STATE.put(id, (byte) (occluded ? 1 : 0));
                if (occluded) {
                    entitiesCulledThisSecond++;
                    return true;
                }
            }
        }

        return false;
    }

    private static double getMaxDistanceForEntity(Entity entity, UltraFpsConfig cfg) {
        if (entity instanceof ItemEntity) {
            return cfg.itemEntityMaxDistance;
        }
        if (entity instanceof ArmorStand) {
            return cfg.armorStandMaxDistance;
        }
        if (entity instanceof Enemy || entity.getType().getCategory() == MobCategory.MONSTER) {
            return cfg.hostileEntityMaxDistance;
        }
        return cfg.passiveEntityMaxDistance;
    }

    private static boolean isEntityBoxOccluded(Level level, Entity viewer, Vec3 cameraPos, AABB box) {
        // Sample center and top-center of the entity's AABB to prevent false-positive culling over half-slabs
        Vec3 center = box.getCenter();
        if (hasLineOfSight(level, viewer, cameraPos, center)) {
            return false;
        }
        Vec3 topCenter = new Vec3(center.x, box.maxY - 0.1, center.z);
        return !hasLineOfSight(level, viewer, cameraPos, topCenter);
    }

    private static boolean hasLineOfSight(Level level, Entity viewer, Vec3 start, Vec3 end) {
        ClipContext ctx = new ClipContext(
                start,
                end,
                ClipContext.Block.VISUAL,
                ClipContext.Fluid.NONE,
                viewer
        );
        return level.clip(ctx).getType() == HitResult.Type.MISS;
    }

    /**
     * Determines whether a BlockEntity should be skipped during the current render pass.
     */
    public static boolean shouldCullBlockEntity(BlockEntity blockEntity) {
        UltraFpsConfig cfg = UltraFpsConfig.get();
        if (!cfg.enabled || !cfg.blockEntityCulling || blockEntity == null) {
            return false;
        }

        // Always render beacons so beam rendering is uninterrupted
        if (blockEntity instanceof BeaconBlockEntity) {
            return false;
        }

        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 camPos = camera.getPosition();
        BlockPos pos = blockEntity.getBlockPos();

        double dx = (pos.getX() + 0.5) - camPos.x;
        double dy = (pos.getY() + 0.5) - camPos.y;
        double dz = (pos.getZ() + 0.5) - camPos.z;
        double distSq = dx * dx + dy * dy + dz * dz;

        double maxDist = cfg.blockEntityMaxDistance;
        if (distSq > maxDist * maxDist) {
            blockEntitiesCulledThisSecond++;
            return true;
        }

        // Fast view-cone dot product check for block entities further than 8 blocks away
        if (cfg.blockEntityFrustumDotCheck && distSq > 64.0) {
            var lookVec = camera.getLookVector();
            double invLen = 1.0 / Math.sqrt(distSq);
            double dot = (dx * invLen) * lookVec.x() + (dy * invLen) * lookVec.y() + (dz * invLen) * lookVec.z();
            if (dot < -0.35) {
                blockEntitiesCulledThisSecond++;
                return true;
            }
        }

        return false;
    }
}
