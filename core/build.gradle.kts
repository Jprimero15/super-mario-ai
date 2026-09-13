plugins {
    kotlin("jvm")
}

val gdxVersion = "1.12.1"

dependencies {
    api("com.badlogicgames.gdx:gdx:$gdxVersion")
}

// Target Java 11 bytecode without requiring a JDK 11 toolchain to be
// installed (jvmToolchain(11) demands an actual matching JDK be found or
// downloaded, which fails on machines/CI images that only have JDK 17).
// This compiles with whatever JDK is already running Gradle, matching how
// android/build.gradle.kts sets its own jvmTarget/sourceCompatibility.
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "11"
}
