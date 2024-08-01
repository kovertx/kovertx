plugins {
    kotlin("plugin.serialization") version "1.9.23"
}

dependencies {
    api(project(":kovertx-core"))
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
}
