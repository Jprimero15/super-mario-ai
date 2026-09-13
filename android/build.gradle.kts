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
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
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
    from(libGdxNatives.elements.map { elements ->
        elements.map { element ->
            val artifact = element.asFile
            val abi = artifact.name.substringAfter("natives-").substringBeforeLast(".")
            zipTree(artifact).matching { include("**/*.so") }.map { file ->
                file.relativeTo(zipTree(artifact).files.first().parentFile)
            }
        }
    })
    into(nativeOutputDir)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.named("preBuild") { dependsOn(copyLibGdxNatives) }
