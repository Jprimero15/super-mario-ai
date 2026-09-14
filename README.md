# Maryou AI

Maryou AI is an **Android-only 2D endless auto-runner** built with **Godot 4.7.2** and the **GL Compatibility** renderer. The player runs continuously through a stylized forest landscape, jumping over gaps, pipes, and enemies while collecting coins and occasional shields.

> Package ID: `com.maryou.ai`

## Current state

The Godot implementation on `master` is the active game. The project is focused on a lightweight, original platforming experience built around the Maryou character.

The gameplay presentation has been scaled up for better visibility on mobile and desktop testing while retaining the original movement and collision behavior.

```text
super-mario-ai/
├── project.godot
├── icon.svg
├── export_presets.cfg
├── assets/
│   ├── sprites/
│   └── world/
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

Gameplay orchestration is separated from world generation, difficulty, scoring, UI, collectibles, hazards, enemies, and audio settings. Characters and collectibles use lightweight original SVG sprite assets with Godot `SpriteFrames` animation resources, while the forest environment and terrain remain procedural.

## Character and sprite system

- Original Maryou player character with idle, run, jump, fall, hurt, and dead animations.
- Multiple original enemy archetypes with animated sprite frames.
- Animated coin and shield collectibles.
- Nearest-neighbor filtering keeps pixel-art visuals crisp.
- Sprite assets can be replaced independently without changing gameplay logic.
- The game uses an original character and visual identity and is not intended to reproduce existing game characters or artwork.

## Gameplay

- Endless auto-running movement with responsive mobile touch controls.
- Landscape Android layout with resolution-independent touch zones and multitouch support.
- Coyote time and jump buffering for forgiving jumps.
- Smooth speed progression through centralized difficulty tiers.
- Procedurally generated chunks with a safe opening and spacing around hazards.
- Enlarged gameplay presentation for improved readability and visibility.
- Classic pipe obstacles with solid bodies and hazardous contact areas while their tops remain landable.
- Grassy forest-running terrain with trees, hills, and layered soil.
- Sparse coins placed throughout the route rather than constant collectible spam.
- Occasional shield pickups as the run progresses.
- Multiple enemy behaviors including walkers, flying/hopping enemies, chasers, shielded enemies, and advanced variants.
- Hit-stop, camera shake, knockback, invulnerability timing, stomp feedback, and score multipliers.
- Godot pause system using `get_tree().paused`.
- Signal-driven collectible and hazard interactions.
- Local best-score persistence.

## UI and settings

- Custom rounded mobile-friendly HUD controls.
- Animated pause and game-over panels.
- Music and SFX volume controls with persisted settings.
- Large touch controls designed for phones.
- Pixel-art gameplay visuals combined with a procedural forest environment.

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

The project has separate Music and SFX buses with persisted volume settings. Audio assets can be placed under `res://audio/` without changing the gameplay architecture.

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

Do not commit a production keystore or its password. The release workflow intentionally receives signing material through GitHub Actions secrets. Google Play distribution should use the signed AAB and a non-debug production signing key.
