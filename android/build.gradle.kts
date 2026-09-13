plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.jprimero15.maryouai"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.jprimero15.maryouai"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        ndk { abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64") }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions { jvmTarget = "11" }

    sourceSets {
        getByName("main") {
            jniLibs.srcDir(layout.buildDirectory.dir("generated/jniLibs/main"))
        }
    }

    buildTypes {
        getByName("release") { isMinifyEnabled = false }
    }
}

val gdxVersion = "1.12.1"
val nativeOutputRoot = layout.buildDirectory.dir("generated/jniLibs/main")

val nativesArmeabiV7a by configurations.creating
val nativesArm64V8a by configurations.creating
val nativesX86 by configurations.creating
val nativesX86_64 by configurations.creating

dependencies {
    implementation(project(":core"))
    implementation("com.badlogicgames.gdx:gdx-backend-android:$gdxVersion")

    nativesArmeabiV7a("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-armeabi-v7a")
    nativesArm64V8a("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-arm64-v8a")
    nativesX86("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-x86")
    nativesX86_64("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-x86_64")
}

// LibGDX native JARs contain libgdx.so below an ABI-specific path such as
// natives/armeabi-v7a/libgdx.so. Android's jniLibs source set requires the
// ABI directory itself to be the immediate parent of the .so file.
//
// Keep one Sync task per ABI. The previous single Sync task used nested
// `into(...)` blocks and could produce `.../jniLibs/main/out/libgdx.so`,
// which Android then interpreted as an ABI named "out".
fun registerLibGdxNativeTask(
    taskName: String,
    abi: String,
    configuration: Configuration
) = tasks.register(taskName, Sync::class) {
    group = "build"
    description = "Extract LibGDX native library for $abi."
    into(nativeOutputRoot.map { it.resolve(abi) })
    from(configuration.files.map { zipTree(it) })
    include("**/libgdx.so")
    includeEmptyDirs = false
    eachFile {
        path = "libgdx.so"
    }
}

val copyLibGdxArmeabiV7a = registerLibGdxNativeTask(
    "copyLibGdxArmeabiV7a",
    "armeabi-v7a",
    nativesArmeabiV7a
)

val copyLibGdxArm64V8a = registerLibGdxNativeTask(
    "copyLibGdxArm64V8a",
    "arm64-v8a",
    nativesArm64V8a
)

val copyLibGdxX86 = registerLibGdxNativeTask(
    "copyLibGdxX86",
    "x86",
    nativesX86
)

val copyLibGdxX86_64 = registerLibGdxNativeTask(
    "copyLibGdxX86_64",
    "x86_64",
    nativesX86_64
)

tasks.named("preBuild") {
    dependsOn(
        copyLibGdxArmeabiV7a,
        copyLibGdxArm64V8a,
        copyLibGdxX86,
        copyLibGdxX86_64
    )
}
