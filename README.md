# Maryou AI

Maryou AI is an **Android-focused 2D endless auto-runner** built with **Godot 4.7.2** and the **GL Compatibility** renderer. The player runs continuously through a stylized forest landscape, jumping over gaps, enemies, moving hazards, and collecting coins.

> Package ID: `com.maryou.ai`

## Current state

The Godot implementation on `master` is the active game. Gameplay is organized around the Maryou character, procedural chunks, responsive movement, checkpoints, and lightweight original sprite assets.

```text
super-mario-ai/
├── project.godot
├── icon.svg
├── export_presets.cfg
├── assets/
│   ├── sprites/
│   └── world/
├── scenes/main.tscn
├── scenes/obstacles/
├── scripts/
│   ├── game.gd
│   ├── player.gd
│   ├── enemy.gd
│   ├── world_generator.gd
│   ├── chunk.gd
│   ├── difficulty_curve.gd
│   ├── score_manager.gd
│   ├── game_state.gd
│   ├── hud.gd
│   ├── audio_manager.gd
│   ├── conveyor.gd
│   ├── rock.gd
│   └── checkpoint.gd
├── tests/smoke_test.gd
└── .github/workflows/
```

## Gameplay

- Endless auto-running movement with large mobile touch controls.
- Coyote time and jump buffering for forgiving jumps.
- Smooth speed, gap, and enemy-density progression.
- Procedurally generated chunks with a new run seed for replayable variety and deterministic regeneration within that run.
- Bounded ground-enemy patrols plus chase/flying variants instead of every enemy endlessly reversing toward the player.
- Four elemental enemy variants: fire, water, thunder, and shadow.
- Checkpoints persist in `GameState` and can be used on the next restart after a death.
- Conveyors actively influence the player's horizontal velocity.
- Rocks roll back and forth as moving hazards.
- Camera zoom feedback and a short death beat make game-over transitions clearer.
- Local best-score persistence.

## UI and settings

- Responsive HUD sizing for smaller screens.
- Large touch buttons are the single touch-control path, so visual controls and gameplay input stay aligned.
- Pause and game-over panels have separate states; Resume is hidden after death.
- Restart uses a confirmation tap to reduce accidental resets.
- Music and SFX settings include percentage readouts and mute/unmute controls.
- Stat text includes a one-hit HP indicator and shows the shield state when active.
- Labels use strong outlines for readability over the parallax background.

A custom pixel/arcade font is **not included yet**. Supplying a font asset is the remaining asset-dependent typography enhancement from the audit.

## Audio

`AudioManager` now creates a looping multi-note procedural arcade track at runtime, so the Music bus is no longer a single one-shot beep. SFX remain procedural and volume settings are persisted under `user://settings.cfg`.

For production, original or CC0/royalty-free audio assets can still be added under `res://audio/` and wired in later without changing the bus architecture.

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

`.github/workflows/android-build.yml` validates the Godot project, installs the matching Godot Android export template, runs `tests/smoke_test.gd`, builds the debug APK, verifies its signature, and uploads the artifact.

`.github/workflows/release-aab.yml` handles release AAB export and expects these GitHub Actions secrets:

- `MARYOU_RELEASE_KEYSTORE_B64`
- `MARYOU_RELEASE_KEY_ALIAS`
- `MARYOU_RELEASE_KEY_PASSWORD`

The production keystore is reconstructed only inside the CI runner and is never committed to the repository.

## Local validation

```bash
godot --headless --path . --editor --quit
godot --headless --path . --script tests/smoke_test.gd
godot --headless --path . --export-debug "Android Debug" build/android/maryou-ai-debug.apk
```

## Google Play

Do not commit a production keystore or its password. The release workflow intentionally receives signing material through GitHub Actions secrets. Google Play distribution should use the signed AAB and a non-debug production signing key.
