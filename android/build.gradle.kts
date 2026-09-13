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

val copyLibGdxNatives by tasks.registering(Sync::class) {
    description = "Extract LibGDX natives into the correct Android ABI directories."
    group = "build"
    into(nativeOutputDir)

    into("armeabi-v7a") {
        from(nativesArmeabiV7a.files.map { zipTree(it) })
        include("**/libgdx.so")
        includeEmptyDirs = false
        eachFile { path = "libgdx.so" }
    }

    into("arm64-v8a") {
        from(nativesArm64V8a.files.map { zipTree(it) })
        include("**/libgdx.so")
        includeEmptyDirs = false
        eachFile { path = "libgdx.so" }
    }

    into("x86") {
        from(nativesX86.files.map { zipTree(it) })
        include("**/libgdx.so")
        includeEmptyDirs = false
        eachFile { path = "libgdx.so" }
    }

    into("x86_64") {
        from(nativesX86_64.files.map { zipTree(it) })
        include("**/libgdx.so")
        includeEmptyDirs = false
        eachFile { path = "libgdx.so" }
    }
}

tasks.named("preBuild") {
    dependsOn(copyLibGdxNatives)
}
