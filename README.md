# Maryou AI

Maryou AI is an Android-only 2D endless auto-runner built with **Godot 4.7.2** and the **GL Compatibility** renderer.

> Package ID: `com.maryou.ai`

## Current architecture

The Godot master branch is now the active implementation. The old LibGDX/Kotlin source and Gradle modules have been removed from `master`; the historical implementation remains available on the backup branch.

```text
super-mario-ai/
├── project.godot
├── export_presets.cfg
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

Gameplay orchestration is separated from world generation, difficulty, scoring, UI, collectibles, hazards, enemies, and audio settings. The core visuals remain procedural/vector-style with no heavy sprite pack.

## Gameplay improvements

- Continuous speed curve instead of integer-division speed jumps.
- Centralized difficulty tiers and spawn curves.
- Coyote time and jump buffering for more forgiving mobile controls.
- Resolution-independent touch zones.
- Per-finger multitouch tracking.
- Godot pause system via `get_tree().paused`.
- Signal-driven coins, shield pickups, and pipe hazards instead of per-frame collectible/hazard rectangle polling.
- Pipe tops remain physically landable while side/lower pipe contact is hazardous.
- Three enemy behaviors: walker, flying/hopping enemy, and fast chaser.
- Hit-stop feedback and invulnerability/knockback handling.
- Hardened local best-score persistence.
- Settings UI with independent Music/SFX buses and persisted volume values.

## Android

- Godot: 4.7.2
- Renderer: GL Compatibility
- Orientation: landscape
- Minimum Android SDK: API 21
- Target Android SDK: API 35
- Architectures: `armeabi-v7a`, `arm64-v8a`
- Java in CI: OpenJDK 17

The debug export is an APK intended for device testing. The release workflow exports a signed Android App Bundle for Google Play when the release keystore secrets are configured. Godot's Android documentation confirms that Google Play distribution uses AAB and a non-debug signing key. citeturn0search0turn0search2

## CI

### Debug APK

`.github/workflows/android-build.yml` runs on pushes and pull requests to `master` and:

1. Installs Java, Android SDK and matching Godot 4.7.2 export templates.
2. Validates the project.
3. Runs the headless gameplay smoke test.
4. Creates a temporary debug keystore.
5. Exports the signed Android debug APK.
6. Verifies the APK with `apksigner`.
7. Uploads the APK artifact.

### Release AAB

`.github/workflows/release-aab.yml` is manually triggered and expects these GitHub Actions secrets:

- `MARYOU_RELEASE_KEYSTORE_B64`
- `MARYOU_RELEASE_KEY_ALIAS`
- `MARYOU_RELEASE_KEY_PASSWORD`

The keystore is reconstructed only inside the CI runner and is never committed to the repository.

Godot's Gradle Android build is required for AAB export, and its command-line tooling supports installing the Android build template before export. citeturn2search1turn2search2

## Local validation

```bash
godot --headless --path . --editor --quit
godot --headless --path . --script tests/smoke_test_v2.gd
godot --headless --path . --export-debug "Android Debug" build/android/maryou-ai-debug.apk
```

## Audio

The project currently contains the audio architecture but no bundled music/SFX assets. `audio_manager.gd` creates separate Music and SFX buses and persists their volume settings. Audio assets can be added later under `res://audio/` without changing gameplay architecture.

Recommended future asset layout:

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

Use CC0/royalty-free assets or original generated audio and keep the corresponding license/source notes in the repository.

## Google Play signing

Do not commit a production keystore or its password. The release workflow intentionally receives the release keystore through GitHub Actions secrets. Google Play uploads require a non-debug signing key and AAB packaging. citeturn0search0
