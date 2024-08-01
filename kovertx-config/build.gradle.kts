plugins {
    kotlin("plugin.serialization") version "1.9.24"
}

dependencies {
    api("io.vertx:vertx-config")
    api(project(":kovertx-serialization"))
}