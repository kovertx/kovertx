package io.kovertx.sql

import io.kovertx.serialization.decodeFromVertxJson
import io.kovertx.serialization.toKotlinx
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.Tuple
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

/**
 * Get a JSON column at pos and decode it to T using kotlinx serialization.
 *
 * @see Row.getJson
 */
inline fun <reified T> Row.decodeJson(pos: Int, json: Json = Json) =
    json.decodeFromVertxJson<T>(getJson(pos).let { if (it === Tuple.JSON_NULL) null else it })

/** @see Row.decodeJson */
inline fun <reified T> Row.decodeJson(column: String, json: Json = Json) {
    val pos = getColumnIndex(column)
    return decodeJson(pos, json)
}

/** @see Row.decodeJson */
inline fun <reified T> Row.decodeNullableJson(pos: Int, json: Json = Json): T? {
    val tmp = getJson(pos)
    if (tmp == null || tmp === Tuple.JSON_NULL) return null
    return json.decodeFromVertxJson<T>(tmp)
}

/** @see Row.decodeJson */
inline fun <reified T> Row.decodeNullableJson(column: String, json: Json = Json): T? {
    val pos = getColumnIndex(column)
    return decodeNullableJson(pos, json)
}

/** @see Row.decodeJson */
inline fun <reified T> Row.decodeJsonArray(pos: Int, json: Json = Json): List<T> {
    val arr = getJsonArray(pos) ?: return emptyList()
    return json.decodeFromJsonElement(
        ListSerializer(json.serializersModule.serializer<T>()),
        arr.toKotlinx()
    )
}

/** @see Row.decodeJson */
inline fun <reified T> Row.decodeJsonArray(column: String, json: Json = Json): List<T> {
    val pos = getColumnIndex(column)
    return decodeJsonArray(pos, json)
}

/** @see Row.decodeJson */
inline fun <reified T> Row.decodeNullableJsonArray(pos: Int, json: Json = Json): List<T>? {
    val arr = getJsonArray(pos) ?: return null
    return json.decodeFromJsonElement(
        ListSerializer(json.serializersModule.serializer<T>()),
        arr.toKotlinx()
    )
}

/** @see Row.decodeJson */
inline fun <reified T> Row.decodeNullableJsonArray(column: String, json: Json = Json): List<T>? {
    val pos = getColumnIndex(column)
    return decodeNullableJsonArray(pos, json)
}
