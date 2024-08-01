package io.kovertx.serialization

import io.vertx.core.json.Json
import io.vertx.core.json.JsonArray as VertxJsonArray
import io.vertx.core.json.JsonObject as VertxJsonObject
import kotlinx.serialization.json.*

fun JsonElement.toVertx(): Any? =
    when (this) {
        JsonNull -> null
        is JsonObject -> toVertx()
        is JsonArray -> toVertx()
        is JsonPrimitive -> toVertx()
    }

fun JsonPrimitive.toVertx(): Any? = if (this.isString) content else Json.decodeValue(content)

fun JsonObject.toVertx(): VertxJsonObject =
    VertxJsonObject().also { result ->
        forEach { key, value ->
            when (value) {
                JsonNull -> result.putNull(key)
                is JsonObject -> result.put(key, value.toVertx())
                is JsonArray -> result.put(key, value.toVertx())
                is JsonPrimitive -> result.put(key, value.toVertx())
            }
        }
    }

fun JsonArray.toVertx(): VertxJsonArray =
    VertxJsonArray().also { result ->
        forEach { value ->
            when (value) {
                JsonNull -> result.addNull()
                is JsonObject -> result.add(value.toVertx())
                is JsonArray -> result.add(value.toVertx())
                is JsonPrimitive -> result.add(value.toVertx())
            }
        }
    }

fun vertxJsonToKotlinx(value: Any?): JsonElement =
    when (value) {
        null -> JsonNull
        is VertxJsonObject -> value.toKotlinx()
        is VertxJsonArray -> value.toKotlinx()
        is Number -> JsonPrimitive(value)
        is String -> JsonPrimitive(value)
        else -> TODO()
    }

fun VertxJsonObject.toKotlinx(): JsonObject = buildJsonObject {
    this@toKotlinx.forEach { (key, value) ->
        when (value) {
            null -> putNull(key)
            is VertxJsonObject -> put(key, value.toKotlinx())
            is VertxJsonArray -> put(key, value.toKotlinx())
            is String -> put(key, value)
            is Number -> put(key, value)
            is Boolean -> put(key, value)
        }
    }
}

fun VertxJsonArray.toKotlinx(): JsonArray = buildJsonArray {
    this@toKotlinx.forEach { value ->
        when (value) {
            null -> addNull()
            is VertxJsonObject -> add(value.toKotlinx())
            is VertxJsonArray -> add(value.toKotlinx())
            is String -> add(value)
            is Number -> add(value)
            is Boolean -> add(value)
        }
    }
}
