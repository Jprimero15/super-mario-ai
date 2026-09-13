# Kotlin Mario-Like — Android Vertical Slice

Kotlin + LibGDX platformer targeting **Android only**. Momentum-based
movement, coyote time + jump buffering, gravity, AABB tile collision, one
patrol enemy (stomp-kill), coins/score, a Big/Small power state, a
scroll-following camera, a HUD, pause, win/lose screens — and on-screen touch
buttons (no keyboard on a phone/tablet).

## Important: I could not actually build this in my sandbox

My execution environment has no internet access and no Android SDK/Gradle
installed, so I could not run a real build here — I can't honestly claim
"verified to compile." What I did instead:

- Used LibGDX/Android APIs I'm confident are correct (`AndroidApplication`,
  `AndroidApplicationConfiguration`, the `gdx-backend-android` /
  `gdx-platform:natives-*` Maven coordinates, `Viewport.unproject`,
  `Gdx.input.isTouched(pointer)` / `getX(pointer)` / `getY(pointer)`,
  `Rectangle.contains(Vector2)`, the AGP 8.x Kotlin DSL `android { }` block
  shape, etc.)
- Kept the manifest minimal (no custom `res/` folder needed — it only uses a
  built-in Android framework theme) to remove one whole category of
  first-build failure
- Checked brace/paren balance across every source file

But a real Android Studio build is the actual test. If it errors, send me the
exact message and I'll fix the specific line.

## Why rectangles instead of sprites

No image/audio asset files are bundled — entities render as flat colored
rectangles via `ShapeRenderer` (green = ground, red = player, maroon = enemy,
gold = coins, gray = touch buttons). Zero missing-asset errors to debug
before you've even seen it run. Swapping in real sprites is step 4 of the
original master prompt.

## How to build & run

You need **Android Studio** (bundles the Android SDK) and a device or
emulator running Android 5.0 (API 21) or newer.

1. Open the `kotlin-mario-game/` folder in Android Studio.
2. Let it sync Gradle — first sync downloads Kotlin, LibGDX, AGP, and the
   Android SDK platform/build-tools from Google's and Maven Central's
   repositories, so it needs internet access on *your* machine (my sandbox
   has none, which is why I couldn't pre-verify this).
3. Run the `android` configuration on a connected device/emulator (green
   Run ▶ button, or `Run > Run 'android'`).

Command line, if you have the Android SDK + a `local.properties` pointing at
it already set up:
```bash
cd kotlin-mario-game
gradle wrapper --gradle-version 8.7   # one-time, generates gradlew
./gradlew android:installDebug
```

## Controls (on-screen, bottom of screen)

| Button | Action |
|---|---|
| `<` (bottom-left) | Move left |
| `>` (bottom-left) | Move right |
| `JUMP` (bottom-right) | Jump — hold longer for a higher jump |
| `\|\|` (top-right) | Pause |

Multi-touch works, so you can hold a direction and tap jump at once.

## Project layout

```
kotlin-mario-game/
├── core/                 # Platform-independent game code (pure Kotlin/JVM)
│   └── src/main/kotlin/com/yourgame/mario/
│       ├── MarioGame.kt          # Game entry, screen switching
│       ├── screens/              # MainMenu, Play, GameOver
│       ├── entities/             # Player, WalkerEnemy, Coin, Entity base
│       ├── physics/              # Gravity constants, AABB collision resolver
│       ├── world/                # Level (string-grid tile format)
│       ├── input/                # InputController interface + TouchInputController
│       └── ui/                   # HUD
├── android/              # Android module (LibGDX Android backend)
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── kotlin/com/yourgame/mario/android/AndroidLauncher.kt
└── build.gradle.kts / settings.gradle.kts
```

## Known simplifications vs. the full master prompt

- No Tiled `.tmx` maps — `Level.kt` uses a hardcoded string grid. Only one
  level exists (`Level.level1()`).
- No sprite animation, no audio, no fire-flower/star power-ups, no boss —
  these are the "steps 4–9" items from the original master prompt's build
  order, left for a follow-up pass.
- No score persistence (save/load) yet.
- `minSdk 21` / `compileSdk 34` / `targetSdk 34` are reasonable current
  defaults, but if Android Studio's installed SDK platforms don't include 34
  it'll prompt you to install it — that's normal, not a bug in the project.

## Continuous integration

`.github/workflows/android-build.yml` builds the debug APK on every push/PR
via GitHub Actions — it installs its own JDK 17, Android SDK, and a pinned
Gradle 8.7 (no wrapper needed), then runs `gradle android:assembleDebug` and
uploads the resulting APK as a downloadable build artifact. Push this repo to
GitHub and check the **Actions** tab; this is also the fastest way to get a
real, independent "does it actually compile" answer, since it runs on a full
Ubuntu machine with real internet access.

## If it doesn't build

Most likely causes, in rough order of probability: an installed SDK platform
that doesn't match `compileSdk`/`targetSdk` (Android Studio will offer to
install the missing one), an AGP/Gradle version mismatch with whatever
Gradle version Android Studio picks, or a transcription issue in one file.
Paste me the exact error and I'll fix the specific line.
