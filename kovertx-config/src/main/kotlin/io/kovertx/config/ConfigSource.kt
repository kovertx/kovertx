package io.kovertx.config

import io.kovertx.serialization.decodeFromJsonObject
import io.kovertx.serialization.toVertx
import io.vertx.config.ConfigRetriever
import io.vertx.config.ConfigRetrieverOptions
import io.vertx.config.ConfigStoreOptions
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject as VertxJsonObject
import io.vertx.core.json.JsonObject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject

@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class ConfigRetrieverBuilderMarker

@ConfigRetrieverBuilderMarker
class ConfigRetrieverBuilder {
    val options = ConfigRetrieverOptions()
    var scanPeriod: Long
        get() = options.scanPeriod
        set(value) {
            options.scanPeriod = value
        }

    init {
        scanPeriod = 0
    }

    fun addStore(store: ConfigStoreOptions): ConfigRetrieverBuilder {
        options.addStore(store)
        return this
    }

    fun json(json: VertxJsonObject) = addStore(ConfigStoreOptions().setType("json").setConfig(json))

    fun json(action: (@ConfigRetrieverBuilderMarker JsonObjectBuilder).() -> Unit) =
        json(buildJsonObject(action).toVertx())

    fun file(path: String, optional: Boolean = true) =
        addStore(
            ConfigStoreOptions()
                .setOptional(optional)
                .setConfig(VertxJsonObject.of("path", path))
                .setType(if (optional) "kovertx-file" else "file")
        )

    fun env() = addStore(ConfigStoreOptions().setType("env"))

    fun sys(cache: Boolean = true) =
        addStore(ConfigStoreOptions().setType("sys").setConfig(VertxJsonObject.of("cache", cache)))
}

abstract class TypedConfigRetriever<T>(protected val proxied: ConfigRetriever) {
    init {
        proxied.setConfigurationProcessor { json ->
            println("decoding config: ${json}")
            return@setConfigurationProcessor JsonObject.of("data", decode(json))
        }
    }

    protected abstract fun decode(json: VertxJsonObject): T

    val cachedConfig
        @Suppress("UNCHECKED_CAST")
        get() = proxied.cachedConfig.getValue("data") as T

    val config: Future<T>
        @Suppress("UNCHECKED_CAST")
        get() = proxied.config.map { it.getValue("data") as T }
}

/** DSL-style function for configuring and building a ConfigRetriever. */
inline fun buildConfigRetriever(
    vertx: Vertx,
    crossinline fn: ConfigRetrieverBuilder.() -> Unit
): ConfigRetriever {
    val options = ConfigRetrieverBuilder().also(fn).options
    val retriever = ConfigRetriever.create(vertx, options)
    return retriever
}

val configJson = Json { ignoreUnknownKeys = true }

/**
 * DSL-style function for configuring and building a ConfigRetriever that has a type-safe binding
 * via kotlinx serialization.
 */
inline fun <reified T> buildConfigRetriever(
    vertx: Vertx,
    crossinline fn: ConfigRetrieverBuilder.() -> Unit
): TypedConfigRetriever<T> {
    return object : TypedConfigRetriever<T>(buildConfigRetriever(vertx, fn)) {
        override fun decode(json: JsonObject): T = configJson.decodeFromJsonObject(json)
    }
}
