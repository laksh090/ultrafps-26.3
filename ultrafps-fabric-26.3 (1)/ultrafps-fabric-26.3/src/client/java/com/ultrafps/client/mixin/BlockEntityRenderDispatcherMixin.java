package com.ultrafps.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ultrafps.client.culling.CullingManager;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Skips rendering off-cone and distant BlockEntities (chests, signs, banners, campfires, shulker boxes).
 */
@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private <E extends BlockEntity> void ultrafps$onRenderBlockEntity(
            E blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            CallbackInfo ci
    ) {
        if (CullingManager.shouldCullBlockEntity(blockEntity)) {
            ci.cancel();
        }
    }
}
