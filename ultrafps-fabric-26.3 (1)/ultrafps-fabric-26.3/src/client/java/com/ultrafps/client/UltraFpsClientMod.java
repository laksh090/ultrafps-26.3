package com.ultrafps.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.ultrafps.client.config.UltraFpsConfig;
import com.ultrafps.client.culling.CullingManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main client entrypoint for UltraFPS Boost on Minecraft 26.3 (Wilderness Bound).
 * Uses Minecraft 26.3 unobfuscated Mojang mappings and SDL-backed InputConstants.
 */
public final class UltraFpsClientMod implements ClientModInitializer {
    public static final String MOD_ID = "ultrafps";
    public static final Logger LOGGER = LoggerFactory.getLogger("UltraFPS-26.3");

    private KeyMapping cyclePresetKey;
    private KeyMapping toggleOcclusionKey;
    private KeyMapping showStatsKey;

    @Override
    public void onInitializeClient() {
        UltraFpsConfig.load();

        KeyMapping.Category category = KeyMapping.Category.register(
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "general")
        );

        this.cyclePresetKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.ultrafps.cycle_preset",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_F8,
                category
        ));

        this.toggleOcclusionKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.ultrafps.toggle_occlusion",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_F9,
                category
        ));

        this.showStatsKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.ultrafps.show_stats",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_F10,
                category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(this::onEndClientTick);

        LOGGER.info("[UltraFPS 26.3] Initialized with preset: {}", UltraFpsConfig.get().activePreset.displayName());
    }

    private void onEndClientTick(Minecraft client) {
        CullingManager.tickTelemetry();

        if (client.level == null) {
            CullingManager.clearCache();
            return;
        }

        while (this.cyclePresetKey.consumeClick()) {
            UltraFpsConfig cfg = UltraFpsConfig.get();
            UltraFpsConfig.Preset next = cfg.activePreset.next();
            cfg.applyPreset(next);
            if (client.player != null) {
                client.player.displayClientMessage(
                        Component.literal("[UltraFPS] ")
                                .withStyle(ChatFormatting.GOLD)
                                .append(Component.literal("Preset: " + next.displayName())
                                        .withStyle(next == UltraFpsConfig.Preset.OFF ? ChatFormatting.RED : ChatFormatting.GREEN)),
                        true
                );
            }
        }

        while (this.toggleOcclusionKey.consumeClick()) {
            UltraFpsConfig cfg = UltraFpsConfig.get();
            cfg.entityOcclusionCulling = !cfg.entityOcclusionCulling;
            cfg.save();
            if (client.player != null) {
                client.player.displayClientMessage(
                        Component.literal("[UltraFPS] ")
                                .withStyle(ChatFormatting.GOLD)
                                .append(Component.literal("Raycast Entity Occlusion: "
                                        + (cfg.entityOcclusionCulling ? "ON" : "OFF"))
                                        .withStyle(cfg.entityOcclusionCulling ? ChatFormatting.GREEN : ChatFormatting.YELLOW)),
                        true
                );
            }
        }

        while (this.showStatsKey.consumeClick()) {
            if (client.player != null) {
                String msg = String.format(
                        "[UltraFPS] %d FPS | Culled/s: %d Entities, %d BlockEntities, %d Particles",
                        client.getFps(),
                        CullingManager.getLastSnapshotEntities(),
                        CullingManager.getLastSnapshotBlockEntities(),
                        CullingManager.getLastSnapshotParticles()
                );
                client.player.displayClientMessage(
                        Component.literal(msg).withStyle(ChatFormatting.AQUA),
                        true
                );
            }
        }
    }
}
