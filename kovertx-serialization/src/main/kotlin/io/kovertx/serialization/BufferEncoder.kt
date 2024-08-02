package io.kovertx.serialization

import io.vertx.core.buffer.Buffer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

@OptIn(ExperimentalSerializationApi::class)
class BufferEncoder(val output: Buffer) : AbstractEncoder() {
    override val serializersModule: SerializersModule = EmptySerializersModule()

    override fun encodeBoolean(value: Boolean) {
        output.appendByte(if (value) 1 else 0)
    }

    override fun encodeByte(value: Byte) {
        output.appendByte(value)
    }

    override fun encodeShort(value: Short) {
        output.appendShort(value)
    }

    override fun encodeInt(value: Int) {
        output.appendInt(value)
    }

    override fun encodeLong(value: Long) {
        output.appendLong(value)
    }

    override fun encodeFloat(value: Float) {
        output.appendFloat(value)
    }

    override fun encodeDouble(value: Double) {
        output.appendDouble(value)
    }

    override fun encodeChar(value: Char) {
        output.appendInt(value.code)
    }

    override fun encodeString(value: String) {
        val bytes: ByteArray = value.toByteArray(Charsets.UTF_8)
        output.appendInt(bytes.size)
        output.appendBytes(bytes)
    }

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        output.appendInt(index)
    }

    override fun beginCollection(
        descriptor: SerialDescriptor,
        collectionSize: Int
    ): CompositeEncoder {
        output.appendInt(collectionSize)
        return this
    }

    override fun encodeNull() = encodeBoolean(false)

    override fun encodeNotNullMark() = encodeBoolean(true)
}
