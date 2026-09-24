# Maryou AI

Maryou AI is an **Android-focused 2D endless auto-runner** built with **Godot 4.7.2** and the **GL Compatibility** renderer.

The player runs through a procedurally generated forest, collects coins, avoids gaps and hazards, defeats enemies by stomping, and progresses through increasing difficulty tiers.

> Package ID: `com.maryou.ai`

## Project status

The active implementation is the **GDScript/Godot version on `master`**.

The main scene is:

```text
scenes/main.tscn
    └── scripts/game_with_forest.gd
            └── scripts/game.gd
```

`game_with_forest.gd` extends the canonical game controller and adds the foreground terrain rendering used by the main scene.

## Features

### Gameplay

- Endless auto-running movement with keyboard and large mobile touch controls.
- Coyote time and jump buffering for more forgiving platforming.
- Variable jump height through jump-button release.
- Progressive movement speed and difficulty tiers.
- Procedurally generated 640px chunks.
- Per-run random seed with deterministic chunk generation within that run.
- Ground gaps with visual depth cues.
- Coins with animated sprites and persistent best-score tracking.
- Enemy variants with patrol/chase/flying behavior.
- Four enemy types: fire, water, thunder, and shadow.
- Enemy stomping and player damage/invulnerability handling.
- Checkpoints that can be used on the next restart after death.
- Moving conveyors, rocks, saws, lava, spikes, acid, mace hazards, bounce pads, crates, platforms, and other obstacle scenes.
- Camera smoothing, shake effects, landing squash/stretch, and death feedback.

### Forest and parallax background

The background uses the existing artwork:

```text
assets/world/parallax/
├── back.png
├── far.png
└── middle.png
```

Rendering order is:

```text
Back → Far → Middle → foreground terrain → gameplay objects
```

The background renderer:

- Tiles the layers around the active camera instead of using a fixed world-width limit.
- Uses different parallax factors for each depth layer.
- Keeps the three source textures aligned to a shared visual height.
- Fills the unused lower screen area with a forest/ground treatment.
- Adds lightweight procedural fireflies and drifting leaves.
- Avoids creating additional scene nodes for those ambient effects.

This camera-relative approach is intentional: Godot's 2D rendering supports custom drawing and parallax techniques, while CanvasLayer/CanvasItem separation can keep screen-space UI independent from camera movement. urlGodot 4.7 documentationhttps://docs.godotengine.org/en/4.7/

### UI

The HUD is generated from `scripts/hud.gd` and includes:

- Steps/distance counter.
- Coin counter.
- HP state.
- Difficulty tier.
- Pause button.
- Large left/right touch controls.
- Jump button.
- Pause and game-over overlays.
- Restart confirmation.
- Checkpoint notification.
- Responsive sizing for smaller displays.
- Automatic release of virtual input actions when pausing, ending, or leaving the scene.

The HUD uses a `CanvasLayer`, keeping it independent of the gameplay camera.

## Architecture

```text
project.godot
│
├── scenes/
│   ├── main.tscn
│   └── obstacles/
│
├── scripts/
│   ├── game.gd                 # Core run loop, camera-relative background
│   ├── game_with_forest.gd     # Main-scene foreground terrain renderer
│   ├── player.gd               # Player movement, jumping, camera, animation
│   ├── world_generator.gd      # Procedural chunk/obstacle/enemy generation
│   ├── chunk.gd                # Chunk data and pit rendering
│   ├── enemy.gd                # Enemy behavior and combat
│   ├── difficulty_curve.gd     # Speed, holes, enemy density and tiers
│   ├── hud.gd                  # Runtime UI
│   ├── score_manager.gd        # Score, coins and best record
│   ├── game_state.gd           # Checkpoint state
│   ├── audio_manager.gd        # Procedural music/SFX and settings
│   ├── conveyor.gd             # Conveyor movement
│   ├── rock.gd                 # Rolling rock hazard
│   └── checkpoint.gd           # Checkpoint trigger
│
├── assets/
│   ├── sprites/
│   └── world/
│
├── tests/
│   └── smoke_test.gd
│
└── .github/workflows/
    ├── android-build.yml
    └── release-aab.yml
```

### Runtime flow

1. `game.gd` creates the enemy container, procedural world, player, and HUD.
2. `world_generator.gd` creates chunks ahead of the player.
3. Old chunks and distant enemies are pruned behind the player.
4. `player.gd` handles movement, jumping, camera behavior, animation, damage, and death.
5. The game controller updates enemy AI and score progression.
6. The HUD remains screen-space while the world follows the camera.
7. `ScoreManager`, `GameState`, and `AudioManager` are autoload singletons.

## Difficulty system

Difficulty is calculated from the player's distance in steps.

| Tier | Distance |
| --- | ---: |
| 1 | 0–299 |
| 2 | 300–749 |
| 3 | 750–1399 |
| 4 | 1400+ |

The current curve increases:

- Run speed from approximately 300 to 520.
- Ground-hole probability from approximately 4.5% to 14%.
- Enemy count as distance increases.
- Enemy speed by difficulty tier.
- Enemy variety at higher tiers.

The progression is intentionally capped so difficulty does not grow without bound.

## Audio

`AudioManager` creates Music and SFX buses at runtime when necessary.

Current audio is procedural:

- 8-second arcade-style background loop.
- Jump, coin, stomp, hit, game-over, and UI tones.
- Music/SFX volume settings are persisted in `user://settings.cfg`.

The music restarts a completed WAV stream rather than relying on a WAV loop boundary. This is intended to avoid platform-specific loop-boundary problems.

Original or CC0/royalty-free audio can later be added under `res://audio/` without changing the bus architecture.

## Android configuration

- **Godot:** 4.7.2
- **Renderer:** GL Compatibility
- **Orientation:** Landscape
- **Minimum Android SDK:** API 24
- **Target Android SDK:** API 36
- **Android NDK:** 28.1.13356709 in CI
- **Build tools:** 35.0.1
- **Java:** OpenJDK 17
- **Architectures:** `arm64-v8a`

The project uses a 1280×720 logical viewport with `canvas_items` stretch mode.

## CI

### Android debug build

`.github/workflows/android-build.yml` runs on pushes and pull requests targeting `master`.

It:

1. Checks out the repository.
2. Installs Java 17 and Android SDK/NDK dependencies.
3. Installs Godot 4.7.2 and matching export templates.
4. Imports/validates the Godot project.
5. Runs `tests/smoke_test.gd`.
6. Exports a signed debug APK.
7. Verifies the APK signature.
8. Uploads the APK as a workflow artifact.

### Android release build

`.github/workflows/release-aab.yml` is manually triggered and produces a signed Android App Bundle.

Required GitHub Actions secrets:

- `MARYOU_RELEASE_KEYSTORE_B64`
- `MARYOU_RELEASE_KEY_ALIAS`
- `MARYOU_RELEASE_KEY_PASSWORD`

The production keystore is reconstructed only inside the CI runner and is not committed to the repository.

## Local validation

With Godot 4.7.2 installed:

```bash
godot --headless --path . --editor --quit
godot --headless --path . --script tests/smoke_test.gd
godot --headless --path . --export-debug "Android Debug" build/android/maryou-ai-debug.apk
```

## Development notes

### Rendering

The project deliberately uses the Compatibility renderer for Android-oriented deployment. Godot documents Compatibility as the OpenGL-based renderer intended for lower-end desktop and mobile hardware. urlGodot renderer documentationhttps://docs.godotengine.org/en/4.7/about/list_of_features.html

The game background currently uses custom `CanvasItem` drawing rather than a large collection of background nodes. This keeps the endless background lightweight while allowing the camera-relative tiling to continue indefinitely.

### Asset scaling

The Maryou character uses a 1024×1024, 4×4 sprite atlas. Each 256×256 frame is displayed at 25% scale to produce the current gameplay character size.

The project currently does not ship with a custom pixel/arcade font. Adding an original or appropriately licensed font remains an optional visual enhancement.

## Current audit notes

The recent background/parallax pass fixed the previous fixed-width background behavior and the large empty lower-screen area visible during gameplay. The main scene now uses that renderer through `game_with_forest.gd`.

The remaining areas for future work are primarily polish and gameplay-depth improvements rather than replacing the current rendering architecture:

- More visual variety between procedural chunks.
- Breakable crates reward successful stomps with a coin.
- Enemy stomps reward one run coin.
- More background decoration as optional low-cost effects.
- Additional automated gameplay tests beyond the current smoke test.
- A custom licensed game font.
- More explicit graphics/performance settings for lower-end Android devices.

## License and assets

Check the repository and individual asset files for their applicable licenses before redistributing the game or its assets.

Do not commit production signing keys, passwords, or other secrets.
