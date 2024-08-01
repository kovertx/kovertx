package io.kovertx.serialization

import io.vertx.core.buffer.Buffer
import kotlinx.serialization.serializer

object BufferCodec {
    inline fun <reified T> encode(buffer: Buffer, value: T) {
        val encoder = BufferEncoder(buffer)
        encoder.encodeSerializableValue(serializer(), value)
    }

    inline fun <reified T> decode(buffer: Buffer, pos: Int = 0): T {
        val decoder = BufferDecoder(buffer)
        return decoder.decodeSerializableValue(serializer())
    }
}
