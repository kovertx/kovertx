package io.kovertx.serialization

import io.vertx.core.buffer.Buffer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule


@ExperimentalSerializationApi
class BufferDecoder(val buffer: Buffer, private var pos: Int = 0, var elementsCount: Int = 0) :
    AbstractDecoder() {
    private var elementIndex = 0

    override val serializersModule: SerializersModule = EmptySerializersModule()

    override fun decodeBoolean() = decodeByte().toInt() == 1

    override fun decodeByte() = buffer.getByte(pos++)

    override fun decodeShort() = buffer.getShort(pos).also { pos += 2 }

    override fun decodeInt() = buffer.getInt(pos).also { pos += 4 }

    override fun decodeLong() = buffer.getLong(pos).also { pos += 8 }

    override fun decodeFloat() = buffer.getFloat(pos).also { pos += 4 }

    override fun decodeDouble() = buffer.getDouble(pos).also { pos += 8 }

    override fun decodeChar() = decodeInt().toChar()

    override fun decodeString(): String {
        val len = decodeInt()
        val s = buffer.getString(pos, pos + len)
        pos += len
        return s
    }

    override fun decodeEnum(enumDescriptor: SerialDescriptor) = decodeInt()

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (elementIndex == elementsCount) return CompositeDecoder.DECODE_DONE
        return elementIndex++
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder =
        BufferDecoder(buffer, descriptor.elementsCount)

    override fun decodeSequentially() = true

    override fun decodeCollectionSize(descriptor: SerialDescriptor) =
        decodeInt().also { elementsCount = it }

    override fun decodeNotNullMark() = decodeBoolean()
}
