# Maryou AI

Maryou AI is an Android-only 2D endless auto-runner rebuilt in **Godot 4**. The current version is a vector-style, procedurally generated runner with touch controls, hazards, enemies, coins, shield power-ups, progressive difficulty, combo scoring, pause/game-over flows, and locally stored best scores.

> **Package ID:** `com.maryou.ai`
> **Engine:** Godot 4.7.2

## Current status

The project is being actively ported from the previous LibGDX implementation to Godot 4. The old LibGDX state is preserved in the `backup/libgdx-before-godot4-port` branch so the migration can be rolled back if necessary.

The `master` branch is the Godot version.

## Gameplay

- **Auto-run:** the player continuously moves forward.
- **Touch controls:** dedicated left, right, and jump zones for Android landscape play.
- **Endless world:** terrain is generated procedurally as the player advances.
- **Deterministic generation:** the world uses a fixed seed for reproducible layouts.
- **Holes:** deliberate gaps are generated with spacing intended to keep the run readable and fair.
- **Pipes:** standing pipes act as collision hazards; landing on top is safe while side contact causes damage.
- **Enemies:** enemies can be stomped for bonus score; side contact causes damage.
- **Coins:** collectible coins are generated throughout the run.
- **Shield power-ups:** temporary protection is available during runs.
- **Difficulty:** speed, hazards, and enemy pressure increase as distance progresses.
- **Combo scoring:** successful actions can build the player's score multiplier.
- **Death:** falling into a hole is fatal; damage is fatal once the player's protection/lives are exhausted.
- **Pause:** gameplay and gameplay input are disabled while the pause overlay is open.

## Godot architecture

The Godot port uses a small, focused runtime architecture:

- `CharacterBody2D` for the player.
- Procedural chunk generation and bounded world cleanup.
- Vector drawing for the player, enemies, terrain, pipes, coins, power-ups, and HUD elements.
- Camera smoothing for a cleaner scrolling experience.
- Local best-score persistence using Godot's `ConfigFile` storage.
- Android export through Godot's Android exporter.

The project intentionally avoids a large bitmap/sprite asset pack for the core gameplay visuals.

## Controls

The game is designed for Android landscape orientation.

| Control | Action |
|---|---|
| Left | Move left |
| Right | Move right |
| Jump | Jump |
| Pause | Pause the run |

The touch areas are separated so movement and jumping do not unintentionally overlap.

## Android

- **Application ID:** `com.maryou.ai`
- **App name:** `Maryou AI`
- **Godot:** 4.7.2
- **Minimum Android:** API 21
- **Android build environment:** API 35 / Build Tools 35.0.1
- **Java:** OpenJDK 17 in CI
- **ABIs:** `armeabi-v7a`, `arm64-v8a`
- **Orientation:** landscape
- **Renderer:** Godot GL Compatibility renderer

The Android export is configured as a **signed debug APK** for device testing. CI creates a temporary Android debug keystore, exports the APK, and verifies its signature with `apksigner` before publishing the artifact.

For a Google Play release, a permanent release keystore must be used instead. Godot's Android documentation requires APK/AAB uploads for Play to use a non-debug signing key. citeturn0search0turn1search0

## Build locally

Install Godot 4.7.2 and the Android SDK. Godot recommends OpenJDK 17 for Android export. citeturn0search5

Validate the project:

```bash
godot --headless --path . --editor --quit
```

Export a debug APK:

```bash
godot --headless --path . --export-debug "Android" build/android/maryou-ai-debug.apk
```

The local Godot editor can also export the Android preset directly.

## GitHub Actions

The repository includes `.github/workflows/android-build.yml`.

On pushes and pull requests targeting `master`, the workflow:

1. Installs Java 17 and the required Android SDK packages.
2. Downloads Godot 4.7.2 and matching export templates.
3. Creates a temporary Android debug keystore.
4. Validates the Godot project.
5. Exports a signed Android debug APK.
6. Runs `apksigner verify` against the generated APK.
7. Uploads the verified APK as the `maryou-ai-debug-apk` workflow artifact.

A signed APK is required for normal Android installation; Godot exposes separate debug-keystore settings for Android exports. citeturn1search0turn0search4

## Installing the CI APK

Download the `maryou-ai-debug-apk` artifact from the successful GitHub Actions run and install the APK on an Android device.

If Android reports that an existing `com.maryou.ai` installation has a different signing key, uninstall the existing copy first and then install the new debug APK. Godot documents this as a common Android installation issue when the same package name is signed with a different key. citeturn0search5

The CI build now signs the debug APK consistently within each build, so a previous installation signed by another key may still need to be removed once when switching builds.

## Project structure

```text
super-mario-ai/
├── project.godot
├── export_presets.cfg
├── scenes/
│   └── main.tscn
├── scripts/
│   ├── game.gd
│   ├── player.gd
│   └── enemy.gd
├── .github/
│   └── workflows/
│       └── android-build.yml
└── README.md
```

## Backup branch

The previous LibGDX implementation is preserved at:

```text
backup/libgdx-before-godot4-port
```

This branch is kept as a rollback/reference point while the Godot 4 version is stabilized.

## Development direction

The current priority is to make the Godot 4 Android build stable and installable, then continue improving gameplay, mobile UX, procedural generation, effects, audio, and release packaging.

A production Google Play build will use a dedicated release keystore and should be exported as an Android App Bundle (AAB). New Google Play apps are distributed as AABs, and Play uploads must use non-debug signing. citeturn0search0
