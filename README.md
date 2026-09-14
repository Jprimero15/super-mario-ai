# Maryou AI

Maryou AI is an **Android-only 2D endless auto-runner** built with **Godot 4.7.2** and the **GL Compatibility** renderer. The player runs continuously through a stylized forest landscape, jumping over gaps, pipes, and enemies while collecting a small number of valuable coins and occasional shields.

> Package ID: `com.maryou.ai`

## Current state

The Godot implementation on `master` is the active game. The old LibGDX/Kotlin implementation is kept out of the active project so the Android build stays focused and maintainable.

```text
super-mario-ai/
├── project.godot
├── icon.svg
├── export_presets.cfg
├── assets/
│   └── sprites/
│       ├── maryou.svg
│       ├── maryou_frames.tres
│       ├── enemies.svg
│       ├── enemy_frames.tres
│       ├── coin.svg
│       └── coin_frames.tres
├── scenes/main.tscn
├── scripts/
│   ├── game.gd
│   ├── player.gd
│   ├── enemy.gd
│   ├── world_generator.gd
│   ├── chunk.gd
│   ├── collectible.gd
│   ├── hazard.gd
│   ├── difficulty_curve.gd
│   ├── score_manager.gd
│   ├── hud.gd
│   └── audio_manager.gd
├── tests/
└── .github/workflows/
```

Gameplay orchestration is separated from world generation, difficulty, scoring, UI, collectibles, hazards, enemies, and audio settings. The active characters, enemies, and coin now use lightweight original SVG sprite atlases with Godot `SpriteFrames` animation resources; the forest backdrop and terrain remain procedural so the game stays lightweight.

## Sprite system

- Original Maryou player sprite atlas with idle, run, jump, fall, hurt, and dead animation rows.
- Six original enemy archetype rows with four animation frames each.
- Animated four-frame coin atlas.
- Godot 4.7.2 `SpriteFrames` resources use `AtlasTexture` regions and nearest filtering for crisp rendering.
- `AnimatedSprite2D` is used by the player, enemies, and coins instead of the old procedural character/coin drawings.
- Sprite assets live under `res://assets/sprites/` and can be replaced independently without changing gameplay logic.

## Gameplay

- Endless auto-running movement with responsive mobile touch controls.
- Landscape Android layout with resolution-independent touch zones and per-finger multitouch.
- Coyote time and jump buffering for forgiving jumps.
- Smooth continuous speed progression through centralized difficulty tiers.
- Fair chunk generation with a safe opening and spacing around hazards.
- **Classic pipe obstacles are back**, with solid pipe bodies and hazardous side/lower contact while their tops remain landable.
- Ground is presented as a grassy forest-running path with trees, hills, and soil layers.
- **Sparse coins:** only a few coins are generated per chunk instead of filling the route with collectibles.
- Occasional shield pickups after the run has progressed.
- Multiple enemy behaviors including walker, flying/hopping, chaser, shielded and additional advanced variants.
- Hit-stop, camera shake, knockback, invulnerability timing, stomp feedback, and score multiplier.
- Godot pause system using `get_tree().paused`.
- Signal-driven collectible and hazard interactions rather than per-frame rectangle polling.
- Local best-score persistence.

## UI and settings

- Custom rounded mobile-friendly HUD controls.
- Animated pause/game-over panel transitions.
- Music and SFX volume controls with persisted settings.
- Large touch controls designed for phones.
- Original sprite-based gameplay visuals plus a procedural forest environment and dedicated application icon.

## Android build

- Godot: **4.7.2**
- Renderer: **GL Compatibility**
- Orientation: **Landscape**
- Minimum Android SDK: **API 24**
- Target Android SDK: **API 35**
- Architectures: `armeabi-v7a`, `arm64-v8a`
- Java in CI: **OpenJDK 17**

The debug workflow produces a signed APK for device testing. The release workflow produces a signed Android App Bundle (AAB) when the production signing secrets are configured.

## CI validation

`.github/workflows/android-build.yml` validates the Android project, installs the matching Godot Android export template, runs the headless smoke test, builds the debug APK, signs it with a temporary CI keystore, verifies the APK signature, and uploads the artifact.

`.github/workflows/release-aab.yml` handles release AAB export and expects these GitHub Actions secrets:

- `MARYOU_RELEASE_KEYSTORE_B64`
- `MARYOU_RELEASE_KEY_ALIAS`
- `MARYOU_RELEASE_KEY_PASSWORD`

The production keystore is reconstructed only inside the CI runner and is never committed to the repository.

## Local validation

```bash
godot --headless --path . --editor --quit
godot --headless --path . --script tests/smoke_test_v2.gd
godot --headless --path . --export-debug "Android Debug" build/android/maryou-ai-debug.apk
```

## Audio architecture

The project has separate Music and SFX buses and persisted volume settings. Audio assets can be placed under `res://audio/` without changing the gameplay architecture.

Recommended layout:

```text
res://audio/
├── music/
│   ├── menu.ogg
│   └── gameplay.ogg
└── sfx/
    ├── jump.wav
    ├── land.wav
    ├── coin.wav
    ├── stomp.wav
    ├── hit.wav
    ├── shield.wav
    ├── game_over.wav
    └── ui_click.wav
```

Use CC0/royalty-free assets or original generated audio and keep license/source notes in the repository.

## Google Play

Do not commit a production keystore or its password. The release workflow intentionally receives the signing material through GitHub Actions secrets. Google Play distribution should use the signed AAB and a non-debug production signing key.
