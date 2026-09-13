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
val nativeOutputDir = layout.buildDirectory.dir("generated/jniLibs/main")

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

/**
 * Extract LibGDX native libraries into the Android jniLibs layout:
 *   generated/jniLibs/main/<abi>/libgdx.so
 *
 * The ABI directory is assigned directly in each FileCopyDetails path so
 * Android's mergeDebugNativeLibs task cannot mistake the ABI name for a file.
 */
val copyLibGdxNatives by tasks.registering(Sync::class) {
    description = "Extract LibGDX natives into Android ABI directories."
    group = "build"
    into(nativeOutputDir)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(nativesArmeabiV7a.files.map { zipTree(it) }) {
        include("**/libgdx.so")
        eachFile { path = "armeabi-v7a/libgdx.so" }
        includeEmptyDirs = false
    }

    from(nativesArm64V8a.files.map { zipTree(it) }) {
        include("**/libgdx.so")
        eachFile { path = "arm64-v8a/libgdx.so" }
        includeEmptyDirs = false
    }

    from(nativesX86.files.map { zipTree(it) }) {
        include("**/libgdx.so")
        eachFile { path = "x86/libgdx.so" }
        includeEmptyDirs = false
    }

    from(nativesX86_64.files.map { zipTree(it) }) {
        include("**/libgdx.so")
        eachFile { path = "x86_64/libgdx.so" }
        includeEmptyDirs = false
    }
}

tasks.named("preBuild") {
    dependsOn(copyLibGdxNatives)
}
