package com.ultrafps.client.mixin;

import com.ultrafps.client.culling.CullingManager;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Intercepts EntityRenderDispatcher#shouldRender to cull distant and wall-occluded entities
 * before expensive entity render state extraction occurs in Minecraft 26.3.
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void ultrafps$onShouldRender(
            E entity,
            Frustum frustum,
            double camX,
            double camY,
            double camZ,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (CullingManager.shouldCullEntity(entity, new Vec3(camX, camY, camZ))) {
            cir.setReturnValue(false);
        }
    }
}
