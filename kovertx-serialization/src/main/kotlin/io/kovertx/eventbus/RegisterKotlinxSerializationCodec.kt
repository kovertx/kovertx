package io.kovertx.eventbus

import io.kovertx.core.BufferInputStream
import io.kovertx.core.BufferOutputStream
import io.kovertx.serialization.BufferCodec
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.MessageCodec
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream

/**
 * Instantiates a MessageCodec that encodes/decodes the passed value using BufferEncoder or
 * BufferDecoder.
 */
inline fun <reified T> makeKotlinxSerializationCodec(): MessageCodec<T, T> {
    return object : MessageCodec<T, T> {
        override fun name() = T::class.qualifiedName

        override fun systemCodecID(): Byte = -1

        override fun transform(s: T) = s

        override fun encodeToWire(buffer: Buffer, s: T) = BufferCodec.encode(buffer, s)

        override fun decodeFromWire(pos: Int, buffer: Buffer): T = BufferCodec.decode(buffer, pos)
    }
}

/**
 * Creates and registers a codec for the passed type using kotlinx.serialization using BufferEncoder
 * and BufferDecoder
 *
 * @see makeKotlinxSerializationCodec
 */
inline fun <reified T> EventBus.registerKotlinxSerializationCodec() =
    registerDefaultCodec(T::class.java, makeKotlinxSerializationCodec())

inline fun <reified T> makeKotlinxSerializationJsonCodec(): MessageCodec<T, T> {
    return object : MessageCodec<T, T> {
        override fun name() = T::class.qualifiedName + ".Json"

        override fun systemCodecID(): Byte = -1

        override fun transform(s: T) = s

        override fun encodeToWire(buffer: Buffer, s: T) =
            Json.encodeToStream(s, BufferOutputStream(buffer))

        override fun decodeFromWire(pos: Int, buffer: Buffer): T =
            Json.decodeFromStream(BufferInputStream(buffer, pos))
    }
}

/**
 * Creates and registers a codec for the passed type using kotlinx.serialization Json encoding.
 *
 * @see makeKotlinxSerializationJsonCodec
 */
inline fun <reified T> EventBus.registerKotlinxSerializationJsonCodec() =
    registerDefaultCodec(T::class.java, makeKotlinxSerializationJsonCodec())
