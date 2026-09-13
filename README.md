# Maryou AI

A Kotlin + LibGDX Android platformer built as a lightweight, asset-free Mario-inspired adventure. The project has evolved from a small vertical slice into an **endless procedural platforming game** with responsive touch controls, vector artwork, procedural pipes, checkpoints, coins, scoring, power states, camera following, pause/restart/menu UI, and Android landscape support.

> **Package ID:** `com.maryou.ai`

## Current state

### Gameplay

- Endless procedural side-scrolling world — there is no fixed final level.
- Deterministic procedural generation with a world seed, giving each run a randomized but reproducible layout.
- New terrain is generated ahead of the player as they move.
- Random ground gaps, elevated coin routes, coin clusters, and standing pipe obstacles.
- Standing pipes are real collision obstacles, not decoration.
- Automatic forward world generation keeps the adventure going indefinitely.
- Progressive respawn checkpoints so long runs do not send the player all the way back to the beginning.
- Coins increase score.
- Player supports small/big power states.
- Lives and game-over flow remain in place.
- Camera follows the player continuously.

### Mobile controls

The game is designed for Android landscape play.

| Control | Action |
|---|---|
| Left | Move left |
| Right | Move right |
| Jump | Jump; hold for a higher jump |
| Pause | Pause the run |

Controls now use a dedicated bottom screen band, keeping the playable world above the controls instead of drawing the buttons over the character. Visual buttons are translucent while their touch hit areas remain comfortably larger for reliable taps.

The input system polls all available Android touch pointers every frame, allowing direction + jump and other simultaneous touches without sacrificing responsiveness.

## Visual design

The game uses resolution-independent LibGDX `ShapeRenderer` vector primitives instead of bitmap sprites for the core gameplay artwork.

Current vector artwork includes:

- Player character
- Coins with animated rotation
- Ground/grass tiles
- Standing pipe obstacles
- Direction controls
- Jump control
- Pause/restart/menu icons
- HUD panels
- Android launcher icon

The palette has been softened into a cohesive blue/green/gold/red theme with translucent UI layers so the controls and HUD blend into the game rather than dominating the scene.

## Android app

- **Application ID:** `com.maryou.ai`
- **Android namespace:** `com.maryou.ai`
- **App name:** `Maryou AI`
- **Minimum Android:** API 21
- **Compile SDK:** 34
- **Target SDK:** 34
- **Java:** 11
- **Kotlin:** Android/Kotlin DSL setup
- **LibGDX:** 1.12.1
- **Supported ABIs:** `armeabi-v7a`, `arm64-v8a`, `x86`, `x86_64`
- **Orientation:** sensor landscape, supporting both landscape rotations
- **Launcher icon:** custom Android vector drawable

The Android module also contains the LibGDX native extraction task. Native libraries are placed into the correct ABI directory structure before the Android build, preventing the previous `libgdx.so is not an ABI` packaging failure.

## Project structure

```text
super-mario-ai/
├── core/
│   └── src/main/kotlin/com/yourgame/mario/
│       ├── MarioGame.kt
│       ├── entities/
│       │   ├── Entity.kt
│       │   ├── Player.kt
│       │   └── Coin.kt
│       ├── input/
│       │   ├── InputController.kt
│       │   └── TouchInputController.kt
│       ├── physics/
│       │   ├── Physics.kt
│       │   └── CollisionHandler.kt
│       ├── screens/
│       │   ├── MainMenuScreen.kt
│       │   ├── PlayScreen.kt
│       │   └── GameOverScreen.kt
│       ├── ui/
│       │   ├── HUD.kt
│       │   └── VectorArt.kt
│       └── world/
│           └── Level.kt
│
├── android/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── kotlin/com/maryou/ai/AndroidLauncher.kt
│       └── res/drawable/ic_launcher_vector.xml
│
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

The Android application/package identity is now `com.maryou.ai`. The existing LibGDX core Kotlin package names are retained for compatibility with the current source tree; changing the Android application ID does not require changing every internal Kotlin package.

## Endless world architecture

`Level.kt` no longer contains one fixed hand-authored map. It generates world chunks from a seed as the player approaches unexplored terrain.

The generator creates:

1. Ground terrain and occasional short gaps.
2. Random coin placements.
3. Elevated coin routes.
4. Standing pipe obstacles of varying heights.
5. Additional chunks before the player can reach the end of generated content.

`CollisionHandler` supports adding new solid tiles at runtime, so newly generated terrain and pipes immediately become part of the collision grid.

This gives the game an effectively endless adventure without loading a giant map into memory at startup.

## Build

Requirements:

- Android Studio or a compatible Gradle/JDK environment
- Android SDK with API 34 installed
- Internet access for the initial dependency download

Typical build command:

```bash
gradle android:assembleDebug
```

Install directly to a connected Android device with:

```bash
gradle android:installDebug
```

The repository also contains the Android GitHub Actions build workflow for automated APK builds.

## Previous major build fix

The LibGDX Android native packaging pipeline previously produced:

```text
out/libgdx.so
```

instead of the required ABI-specific layout. The extraction task was corrected to produce:

```text
lib/armeabi-v7a/libgdx.so
lib/arm64-v8a/libgdx.so
lib/x86/libgdx.so
lib/x86_64/libgdx.so
```

That fixed the Android `mergeDebugNativeLibs` failure where Gradle reported that `libgdx.so` was not an ABI. The game was subsequently confirmed working before the current endless-world/UI expansion.

## Design direction

Maryou AI is intentionally staying lightweight:

- Kotlin
- LibGDX
- Android
- Procedural/vector gameplay
- No large sprite pack required
- No external database required for the core game
- No fixed level map required for endless mode

The architecture leaves room for future additions such as audio, more power-ups, bosses, richer procedural structures, saved run statistics, and more advanced enemy AI without replacing the current rendering/input foundation.

## License

This repository is a personal development project. Add a project-specific license here if/when the project is released publicly.