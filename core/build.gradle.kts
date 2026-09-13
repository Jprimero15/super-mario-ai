plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(11)
}

val gdxVersion = "1.12.1"

dependencies {
    api("com.badlogicgames.gdx:gdx:$gdxVersion")
}
