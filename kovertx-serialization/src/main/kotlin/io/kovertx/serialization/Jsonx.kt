package io.kovertx.serialization

import io.kovertx.core.BufferInputStream
import io.kovertx.core.BufferOutputStream
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject as VertxJsonObject
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*

/**
 * Serializes given value to buffer using UTF-8 encoding and serializer retrieved from the reified
 * type parameter.
 *
 * @param position position in the buffer to write to. Data before this index in the buffer will be
 *   left unchanged, and the buffer will be grown as needed to serialize the value.
 */
@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> Json.encodeToBuffer(value: T, buffer: Buffer, position: Int = 0) {
    encodeToStream(value, BufferOutputStream(buffer, position))
}

/**
 * Serializes given value to a new Buffer using UTF-8 encoding and serializer retrieved from the
 * reified type parameter.
 */
inline fun <reified T> Json.encodeToBuffer(value: T): Buffer {
    val buffer = Buffer.buffer()
    encodeToBuffer(value, buffer)
    return buffer
}

inline fun <reified T> Json.encodeToVertxJson(value: T): Any? = encodeToJsonElement(value).toVertx()

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> Json.decodeFromBuffer(buffer: Buffer, position: Int = 0) =
    decodeFromStream<T>(BufferInputStream(buffer, position))

inline fun <reified T> Json.decodeFromJsonObject(o: VertxJsonObject) =
    decodeFromJsonElement<T>(o.toKotlinx())

inline fun <reified T> Json.decodeFromVertxJson(o: Any?) =
    decodeFromJsonElement<T>(vertxJsonToKotlinx(o))
