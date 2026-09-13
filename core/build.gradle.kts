plugins {
    kotlin("jvm")
}

val gdxVersion = "1.12.1"

dependencies {
    api("com.badlogicgames.gdx:gdx:$gdxVersion")
}

// Keep Java and Kotlin bytecode targets aligned for Gradle 8+ / JDK 17 CI.
tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = JavaVersion.VERSION_11.toString()
    targetCompatibility = JavaVersion.VERSION_11.toString()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "11"
}
