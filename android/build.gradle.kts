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
            // Native LibGDX libraries are generated into an ABI-rooted tree.
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

val copyLibGdxNatives by tasks.registering {
    description = "Extract LibGDX native libraries into ABI-specific Android jniLibs directories."
    group = "build"
    outputs.dir(nativeOutputDir)

    doLast {
        // Resolve only during task execution and explicitly restore the ABI
        // directory. The previous zipTree/Sync approach flattened libgdx.so
        // to the output root, which Android Gradle Plugin rejects because
        // every native library must live under lib/<ABI>/.
        libGdxNatives.resolve().forEach { nativeJar ->
            val abi = nativeJar.name
                .substringAfter("natives-", "")
                .substringBeforeLast(".jar")

            require(abi in setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")) {
                "Unsupported LibGDX native ABI artifact: ${nativeJar.name}"
            }

            project.copy {
                from(zipTree(nativeJar)) {
                    include("**/*.so")
                    eachFile {
                        relativePath = RelativePath(true, "lib", abi, name)
                    }
                    includeEmptyDirs = false
                }
                into(nativeOutputDir)
            }
        }
    }
}

tasks.named("preBuild") {
    dependsOn(copyLibGdxNatives)
}
