# Maryou AI

A Kotlin + LibGDX Android endless platform runner with responsive touch controls, procedural hazards, vector artwork, progressive difficulty, and locally recorded run statistics.

> **Package ID:** `com.maryou.ai`

## Current gameplay

- **Auto-run:** the character continuously runs forward.
- **Progressive speed:** run speed increases every 100 steps, gradually up to a safe cap.
- **Endless world:** procedural chunks are generated ahead of the player with no fixed finish line.
- **Monster progression:** monster encounters begin after the opening and scale from small to medium to large as distance increases, with the major tier progression changing every 200 steps.
- **Standing pipes:** pipes are real collision obstacles with varied heights and spacing.
- **Pipe rule:** landing on the top of a pipe is safe; running into its side costs a life/power state.
- **Monsters:** jumping on a monster defeats it and gives bonus score; side contact costs a life/power state.
- **Holes:** deliberate 1–2 tile gaps are generated with readable dark vector interiors and safe spacing from other hazards.
- **Coins:** coins are placed with overlap protection so they do not visually stack with pipes or other obstacles.
- **Checkpoints:** progress checkpoints move forward during long runs.
- **Game over:** falling into a hole or taking fatal damage ends the run after lives are exhausted.

## Run records

Every completed run records its best values locally on the device using LibGDX preferences:

- Best score
- Best coins collected
- Best steps reached

The current run also displays score, lives, steps, coins, and current speed in the HUD. The game-over screen shows the final run statistics.

## Controls

The game is designed for Android landscape play.

| Control | Action |
|---|---|
| Left | Steer left while auto-running |
| Right | Steer right while auto-running |
| Jump | Jump; hold for a higher jump |
| Pause | Pause the run |

The controls occupy a dedicated bottom band, keeping them away from the character and gameplay. Their visual buttons are compact while the invisible touch targets are larger for reliable mobile input. The input controller samples multiple touch pointers every frame, so steering and jumping can be used together.

## UI/UX direction

The current UI uses:

- Measured/centered HUD text to prevent overlapping labels.
- Four evenly spaced top HUD cards for score, lives, steps, and coins.
- A compact lower status line for auto-run, steps, coins, and speed.
- Soft translucent panels instead of heavy opaque controls.
- Consistent blue/green/gold/red accent colors.
- A redesigned pause panel with clean restart/menu actions.
- A redesigned main menu with clear hierarchy and saved-record preview.
- Responsive landscape viewport separation so the gameplay area remains visually clear above the touch-control strip.

## Vector artwork

Gameplay artwork is rendered with LibGDX `ShapeRenderer` primitives, keeping the core visuals resolution-independent without a large bitmap asset pack.

Vector-style artwork includes:

- Player
- Small/medium/large monsters
- Animated coins
- Ground and grass tiles
- Standing pipes
- Designed holes
- Touch controls
- Pause/restart/menu icons
- HUD panels
- Android launcher icon

## Endless procedural architecture

`Level.kt` generates deterministic chunks from a seed as the player approaches unexplored terrain. Each chunk can contain:

1. Ground terrain.
2. Deliberate holes with readable spacing.
3. Elevated coin routes.
4. Standing pipes with varying heights.
5. Monster encounters whose tiers scale with distance.

Generated solids are added to the runtime collision grid through `CollisionHandler`, so newly generated terrain and pipes immediately participate in physics.

The world is generated incrementally rather than storing a giant map in memory.

## Android

- **Application ID:** `com.maryou.ai`
- **Android namespace:** `com.maryou.ai`
- **App name:** `Maryou AI`
- **Minimum Android:** API 21
- **Compile SDK:** 34
- **Target SDK:** 34
- **Java:** 11
- **LibGDX:** 1.12.1
- **Supported ABIs:** `armeabi-v7a`, `arm64-v8a`, `x86`, `x86_64`
- **Orientation:** sensor landscape
- **Launcher icon:** custom Android vector drawable

## Project structure

```text
super-mario-ai/
├── core/
│   └── src/main/kotlin/com/yourgame/mario/
│       ├── MarioGame.kt
│       ├── entities/
│       │   ├── Entity.kt
│       │   ├── Player.kt
│       │   ├── Coin.kt
│       │   └── WalkerEnemy.kt
│       ├── input/
│       ├── physics/
│       ├── screens/
│       ├── ui/
│       └── world/
│           └── Level.kt
├── android/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── res/drawable/ic_launcher_vector.xml
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Build

Requirements:

- Android SDK API 34
- JDK/Gradle environment compatible with the project
- Internet access for dependency downloads

Debug APK:

```bash
gradle android:assembleDebug
```

Install to a connected device:

```bash
gradle android:installDebug
```

The repository also includes a GitHub Actions Android build workflow.

## Native LibGDX packaging

The Android build contains the corrected LibGDX native extraction pipeline. Native libraries must remain inside ABI-specific directories:

```text
lib/armeabi-v7a/libgdx.so
lib/arm64-v8a/libgdx.so
lib/x86/libgdx.so
lib/x86_64/libgdx.so
```

This avoids the previous `mergeDebugNativeLibs` failure where Gradle reported that `libgdx.so` was not an ABI.

## Design direction

Maryou AI is intentionally lightweight and asset-efficient:

- Kotlin
- LibGDX
- Android
- Vector-style procedural rendering
- Endless procedural gameplay
- Local run records
- No external database required for the core game

The foundation can be extended with more enemy behaviors, bosses, power-ups, audio, richer procedural structures, and additional game modes without replacing the current rendering and input architecture.
