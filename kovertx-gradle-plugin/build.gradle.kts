import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    `java-gradle-plugin`
    idea
    id("com.gradle.plugin-publish") version "1.2.1"
}

dependencies {
    implementation(kotlin("stdlib"))
    api("org.jetbrains.kotlin.plugin.serialization:org.jetbrains.kotlin.plugin.serialization.gradle.plugin:1.9.24")
    api("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.24")
}

gradlePlugin {
    website = "https://github.com/kovertx/kovertx/kovertx-gradle-plugin"
    vcsUrl = "https://github.com/kovertx/kovertx"

    plugins {
        create("kovertx-gradle-plugin") {
            id = "io.kovertx.kovertx-gradle-plugin"
            displayName = "Kovertx Gradle Plugin"
            description = "Opinionated gradle plugin for using Kovertx and Kotlin"
            tags = listOf("kovertx", "vertx")
            implementationClass = "io.kovertx.gradle.KovertxGradlePlugin"
        }
    }
}

val generatedSourcesPath = layout.buildDirectory.dir("generated/sources/kotlin/main")

sourceSets {
    main {
        kotlin {
            srcDir(generatedSourcesPath)
        }
    }
}

idea {
    module {
        generatedSourceDirs.add(generatedSourcesPath.get().asFile)
    }
}

tasks.register("generateDefaultsKt") {
    val output = layout.buildDirectory
        .file("generated/sources/kotlin/main/io/kovertx/gradle/Defaults.kt")
        .get().asFile
    doLast {
        output.parentFile.mkdirs()
        output.writeText("""
            package io.kovertx.gradle

            object Defaults {
                val kovertxVersion: String = "${project.version}"
            }
        """.trimIndent())
    }
}

tasks.withType<KotlinCompile> {
    dependsOn("generateDefaultsKt")
}