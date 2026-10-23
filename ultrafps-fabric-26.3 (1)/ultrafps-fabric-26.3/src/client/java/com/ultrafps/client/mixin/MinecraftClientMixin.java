package com.ultrafps.client.mixin;

import com.ultrafps.client.config.UltraFpsConfig;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Dynamically throttles maximum framerate when the Minecraft 26.3 SDL window is unfocused,
 * freeing GPU/CPU cycles and lowering system temperatures in the background.
 */
@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {

    @Inject(method = "getFramerateLimit", at = @At("HEAD"), cancellable = true)
    private void ultrafps$limitUnfocusedFps(CallbackInfoReturnable<Integer> cir) {
        UltraFpsConfig cfg = UltraFpsConfig.get();
        if (!cfg.enabled || !cfg.limitUnfocusedFps) {
            return;
        }
        Minecraft self = (Minecraft) (Object) this;
        if (!self.isWindowActive()) {
            cir.setReturnValue(cfg.unfocusedFpsCap);
        }
    }
}
