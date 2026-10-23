package com.ultrafps.client.mixin;

import com.ultrafps.client.config.UltraFpsConfig;
import com.ultrafps.client.culling.CullingManager;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Throttles excessive particle spawning, including Minecraft 26.3's new Dappled Forest
 * Poplar falling leaf particles (red_poplar_leaves, orange_poplar_leaves, yellow_poplar_leaves),
 * explosions, smoke, and rain splashes.
 */
@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {

    @Unique
    private int ultrafps$spawnCounter = 0;

    @Inject(method = "createParticle", at = @At("HEAD"), cancellable = true)
    private void ultrafps$throttleParticleSpawn(
            ParticleOptions options,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed,
            CallbackInfoReturnable<Particle> cir
    ) {
        UltraFpsConfig cfg = UltraFpsConfig.get();
        if (!cfg.enabled || !cfg.particleOptimization || options == null) {
            return;
        }

        ResourceLocation key = BuiltInRegistries.PARTICLE_TYPE.getKey(options.getType());
        if (key != null) {
            String path = key.getPath();

            // Throttle Minecraft 26.3 Poplar Leaf particles, Cherry leaves, Pale Oak leaves & spore blossoms
            if (path.contains("poplar_leaves") || path.contains("cherry_leaves") || path.contains("pale_oak_leaves") || path.contains("falling_spore")) {
                if (ThreadLocalRandom.current().nextDouble() > cfg.ambientLeafParticleKeepChance) {
                    CullingManager.recordParticleSkipped();
                    cir.setReturnValue(null);
                    return;
                }
            }

            // Throttle heavy smoke, campfire, and explosion particles during combat/farms
            if (path.contains("explosion") || path.contains("campfire") || path.contains("large_smoke")) {
                if (ThreadLocalRandom.current().nextDouble() > cfg.explosionAndSmokeKeepChance) {
                    CullingManager.recordParticleSkipped();
                    cir.setReturnValue(null);
                    return;
                }
            }

            // Throttle rain splash particles during storms
            if (path.equals("rain") || path.equals("Splash")) {
                if (ThreadLocalRandom.current().nextDouble() > cfg.rainSplashKeepChance) {
                    CullingManager.recordParticleSkipped();
                    cir.setReturnValue(null);
                    return;
                }
            }
        }

        // Global rolling window limiter to prevent particle storms from tanking frame times
        ultrafps$spawnCounter++;
        if (ultrafps$spawnCounter > cfg.maxActiveParticles) {
            if ((ultrafps$spawnCounter & 3) != 0) {
                CullingManager.recordParticleSkipped();
                cir.setReturnValue(null);
            }
            if (ultrafps$spawnCounter > cfg.maxActiveParticles * 2) {
                ultrafps$spawnCounter = 0;
            }
        }
    }
}
