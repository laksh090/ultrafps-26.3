package com.ultrafps.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Persistent configuration and preset manager for UltraFPS on Minecraft 26.3.
 */
public final class UltraFpsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("ultrafps-26.3.json");

    private static UltraFpsConfig instance = new UltraFpsConfig();

    public enum Preset {
        BALANCED("Balanced"),
        ULTRA_FPS("Ultra FPS"),
        COMPETITIVE("Competitive Max"),
        OFF("Disabled");

        private final String displayName;

        Preset(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return displayName;
        }

        public Preset next() {
            Preset[] values = values();
            return values[(this.ordinal() + 1) % values.length];
        }
    }

    public boolean enabled = true;
    public Preset activePreset = Preset.ULTRA_FPS;

    // Entity Culling Settings
    public boolean entityDistanceCulling = true;
    public boolean entityOcclusionCulling = true;
    public int occlusionCacheTicks = 5;
    public double passiveEntityMaxDistance = 44.0;
    public double hostileEntityMaxDistance = 64.0;
    public double itemEntityMaxDistance = 28.0;
    public double armorStandMaxDistance = 32.0;

    // Block Entity Culling Settings
    public boolean blockEntityCulling = true;
    public double blockEntityMaxDistance = 48.0;
    public boolean blockEntityFrustumDotCheck = true;

    // Particle Optimization Settings (Includes 26.3 Dappled Forest Poplar Leaf Throttling)
    public boolean particleOptimization = true;
    public int maxActiveParticles = 1200;
    public double ambientLeafParticleKeepChance = 0.25;
    public double explosionAndSmokeKeepChance = 0.35;
    public double rainSplashKeepChance = 0.30;

    // Weather, Sky & Fog Optimization
    public boolean disableDistantTerrainFog = true;
    public boolean adaptiveWeatherSimplification = true;
    public int adaptiveFpsThreshold = 60;

    // Dynamic Unfocused SDL Window FPS Limiter
    public boolean limitUnfocusedFps = true;
    public int unfocusedFpsCap = 15;

    public static UltraFpsConfig get() {
        return instance;
    }

    public void applyPreset(Preset preset) {
        this.activePreset = preset;
        switch (preset) {
            case BALANCED -> {
                this.enabled = true;
                this.entityDistanceCulling = true;
                this.entityOcclusionCulling = true;
                this.passiveEntityMaxDistance = 56.0;
                this.hostileEntityMaxDistance = 72.0;
                this.itemEntityMaxDistance = 36.0;
                this.armorStandMaxDistance = 40.0;
                this.blockEntityCulling = true;
                this.blockEntityMaxDistance = 56.0;
                this.particleOptimization = true;
                this.maxActiveParticles = 2500;
                this.ambientLeafParticleKeepChance = 0.50;
                this.explosionAndSmokeKeepChance = 0.60;
                this.rainSplashKeepChance = 0.50;
                this.disableDistantTerrainFog = false;
                this.adaptiveWeatherSimplification = true;
            }
            case ULTRA_FPS -> {
                this.enabled = true;
                this.entityDistanceCulling = true;
                this.entityOcclusionCulling = true;
                this.passiveEntityMaxDistance = 40.0;
                this.hostileEntityMaxDistance = 56.0;
                this.itemEntityMaxDistance = 24.0;
                this.armorStandMaxDistance = 28.0;
                this.blockEntityCulling = true;
                this.blockEntityMaxDistance = 42.0;
                this.particleOptimization = true;
                this.maxActiveParticles = 1000;
                this.ambientLeafParticleKeepChance = 0.20;
                this.explosionAndSmokeKeepChance = 0.30;
                this.rainSplashKeepChance = 0.20;
                this.disableDistantTerrainFog = true;
                this.adaptiveWeatherSimplification = true;
            }
            case COMPETITIVE -> {
                this.enabled = true;
                this.entityDistanceCulling = true;
                this.entityOcclusionCulling = true;
                this.passiveEntityMaxDistance = 32.0;
                this.hostileEntityMaxDistance = 48.0;
                this.itemEntityMaxDistance = 20.0;
                this.armorStandMaxDistance = 20.0;
                this.blockEntityCulling = true;
                this.blockEntityMaxDistance = 32.0;
                this.particleOptimization = true;
                this.maxActiveParticles = 400;
                this.ambientLeafParticleKeepChance = 0.05;
                this.explosionAndSmokeKeepChance = 0.15;
                this.rainSplashKeepChance = 0.05;
                this.disableDistantTerrainFog = true;
                this.adaptiveWeatherSimplification = true;
            }
            case OFF -> this.enabled = false;
        }
        save();
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                UltraFpsConfig loaded = GSON.fromJson(reader, UltraFpsConfig.class);
                if (loaded != null) {
                    instance = loaded;
                }
            } catch (IOException ignored) {
            }
        } else {
            instance.save();
        }
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException ignored) {
        }
    }
}
