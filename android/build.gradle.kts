plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.yourgame.mario.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.yourgame.mario.android"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // Keep the APK focused on the ABIs we actually ship native LibGDX
        // libraries for. ARM64 is the primary modern Android ABI.
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    sourceSets {
        getByName("main") {
            // LibGDX's platform artifacts are JARs containing the native
            // libraries. Extract them into Android's standard jniLibs tree.
            jniLibs.srcDir(layout.buildDirectory.dir("generated/jniLibs/main"))
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}

val gdxVersion = "1.12.1"
val nativeOutputDir = layout.buildDirectory.dir("generated/jniLibs/main")

val libGdxNatives by configurations.creating

dependencies {
    implementation(project(":core"))
    implementation("com.badlogicgames.gdx:gdx-backend-android:$gdxVersion")

    libGdxNatives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-armeabi-v7a")
    libGdxNatives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-arm64-v8a")
    libGdxNatives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-x86")
    libGdxNatives("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-x86_64")
}

val copyLibGdxNatives by tasks.registering(Sync::class) {
    description = "Extract LibGDX native libraries into Android jniLibs."
    group = "build"
    into(nativeOutputDir)

    // Resolve the native configuration as a task input rather than during
    // project configuration. Gradle 8 otherwise warns about configuration
    // being resolved during configuration time.
    from(libGdxNatives.elements.map { files(it).map { artifact -> zipTree(artifact) } }) {
        include("**/*.so")
        includeEmptyDirs = false
        // Some LibGDX native artifacts can expose the same native entry more
        // than once. Do not fail the build over identical archive entries.
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}

tasks.named("preBuild") {
    dependsOn(copyLibGdxNatives)
}
