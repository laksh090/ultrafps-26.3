# UltraFPS Boost for Minecraft Java Edition 26.3 (Fabric)

A lightweight, client-side FPS boost mod built specifically for **Minecraft: Java Edition 26.3 (Wilderness Bound)** using Fabric's unobfuscated Mojang mappings (`net.fabricmc.fabric-loom` 1.17, Gradle 9.6.0, Java 25, Fabric Loader 0.19.5+, and Fabric API 0.165.0+26.3).

## Key Optimizations
1. **Smart Entity Distance & Raycast Occlusion Culling (`EntityRenderDispatcherMixin`)**:
   - Skips rendering passive mobs, hostile mobs, dropped items, and armor stands beyond category-specific distances.
   - Performs a 2-point bounding-box raycast (`ClipContext.Block.VISUAL`) cached for 5 ticks per entity ID to skip entities hidden behind solid walls while keeping players, bosses, glowing entities, and custom-named entities visible.
2. **Block Entity Frustum & Distance Culling (`BlockEntityRenderDispatcherMixin`)**:
   - Culls distant and behind-camera BlockEntities (chests, signs, banners, campfires, beds, shulker boxes) while preserving beacon beams.
3. **Particle Storm Limiter & 26.3 Poplar Leaf Throttling (`ParticleEngineMixin`)**:
   - Caps active particle bursts and throttles falling leaf particles—including Minecraft 26.3's new Dappled Forest Poplar leaves (`red_poplar_leaves`, `orange_poplar_leaves`, `yellow_poplar_leaves`)—alongside explosion, campfire smoke, and rain splash particles.
4. **Adaptive Weather Geometry Pass (`LevelRendererMixin`)**:
   - Skips heavy snow and rain quad passes when in Competitive mode or during severe frame drops.
5. **Dynamic Unfocused SDL Window FPS Limiter (`MinecraftClientMixin`)**:
   - Automatically caps FPS to 15 when the Minecraft 26.3 SDL window is unfocused.

## In-Game Hotkeys (Configurable in Options > Controls > Key Binds)
- **`F8`**: Cycle Optimization Preset (`Ultra FPS` -> `Competitive Max` -> `Balanced` -> `Disabled`)
- **`F9`**: Toggle Raycast Entity Occlusion Culling (`ON` / `OFF`)
- **`F10`**: Display real-time FPS and per-second culling telemetry on the action bar

## How to Build the `.jar`
1. Install **JDK 25** (required by Minecraft 26.1–26.3).
2. Extract `ultrafps-fabric-26.3.zip` and open a terminal in the project folder.
3. Run:
   ```bash
   gradle build
   ```
4. Copy `build/libs/ultrafps-boost-1.0.0+26.3.jar` into your `.minecraft/mods/` folder alongside **Fabric Loader 0.19.5+** and **Fabric API 0.165.0+26.3**.
