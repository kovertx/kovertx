package io.kovertx.gradle

import org.gradle.api.Project

open class KovertxExtension(private val project: Project) {
    var version: String = Defaults.kovertxVersion
}

/**
 * Extension method to make easier the configuration of the plugin when used with the Gradle Kotlin DSL
 */
fun Project.kovertx(configure: KovertxExtension.() -> Unit) =
    extensions.configure(KovertxExtension::class.java, configure)