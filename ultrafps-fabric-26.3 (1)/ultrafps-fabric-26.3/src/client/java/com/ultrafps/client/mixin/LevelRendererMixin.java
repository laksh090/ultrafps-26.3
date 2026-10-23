package com.ultrafps.client.mixin;

import com.ultrafps.client.config.UltraFpsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Optimizes heavy weather rendering when client FPS drops below the adaptive threshold.
 */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Inject(method = "renderSnowAndRain", at = @At("HEAD"), cancellable = true)
    private void ultrafps$adaptiveWeatherSkip(CallbackInfo ci) {
        UltraFpsConfig cfg = UltraFpsConfig.get();
        if (!cfg.enabled || !cfg.adaptiveWeatherSimplification) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        // In Competitive mode or when FPS drops below threshold during a storm, skip heavy rain/snow geometry quads
        if (cfg.activePreset == UltraFpsConfig.Preset.COMPETITIVE
                || (mc.getFps() > 0 && mc.getFps() < cfg.adaptiveFpsThreshold / 2)) {
            ci.cancel();
        }
    }
}
