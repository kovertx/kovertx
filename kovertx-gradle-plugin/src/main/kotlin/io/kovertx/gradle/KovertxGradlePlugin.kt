package io.kovertx.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlinx.serialization.gradle.SerializationGradleSubplugin
import org.slf4j.LoggerFactory

class KovertxGradlePlugin : Plugin<Project> {
    private val logger = LoggerFactory.getLogger(KovertxGradlePlugin::class.java);

    override fun apply(project: Project) {
        project.extensions.create("kovertx", KovertxExtension::class.java, project)
        applyDependencyPlugins(project)
        project.afterEvaluate {
            val kovertxExt = project.extensions.getByName("kovertx") as KovertxExtension
            logger.debug("Kovertx version: ${kovertxExt.version}")
            applyDependencies(project)
        }
    }

    private fun applyDependencyPlugins(project: Project) {
        project.pluginManager.apply {
            apply("java")
            apply("org.jetbrains.kotlin.jvm")
            apply(SerializationGradleSubplugin::class.java)
        }
        logger.info("Applied kotlin jvm and serialization plugins")
    }

    private fun applyDependencies(project: Project) {
        val kovertxExt = project.extensions.getByName("kovertx") as KovertxExtension
        project.dependencies.apply {
            add("api", platform("io.kovertx:kovertx-bom:${kovertxExt.version}"))
            add("implementation", "io.kovertx:kovertx-core")
        }
        logger.info("Added dependencies on kovertx-bom and kovertx-core")
    }
}