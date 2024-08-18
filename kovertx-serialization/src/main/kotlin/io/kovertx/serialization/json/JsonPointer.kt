package io.kovertx.serialization.json

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement

@Serializable(with = JsonPointerSerializer::class)
data class JsonPointer(val parts: List<String>) {
    fun parent() = JsonPointer(parts.subList(0, parts.size - 1))
    fun isRoot() = parts.isEmpty()
    fun lastPart() = parts.last()

    override fun toString(): String {
        if (isRoot()) return ""
        val sb = StringBuilder("")
        parts.forEach { part ->
            sb.append('/')
            part.forEach { c ->
                if (c == '~') sb.append("~0")
                else if (c == '/') sb.append("~1")
                else sb.append(c)
            }
        }
        return sb.toString()
    }

    companion object {
        fun parse(path: CharSequence): JsonPointer {
            if (path.isEmpty()) return JsonPointer(emptyList())
            val parts = mutableListOf<String>()
            val tmp = StringBuilder()
            var i = 0
            while (i < path.length) {
                val c = path[i]
                if (c == '/') {
                    if (i > 0) parts.add(tmp.toString())
                    tmp.clear()
                } else if (c == '~') {
                    if (path[i + 1] == '0') tmp.append('~')
                    else if (path[i + 1] == '1') tmp.append('/')
                    else throw IllegalArgumentException("Unknown escape ~${path[i + 1]}")
                    i += 1
                } else {
                    tmp.append(c)
                }
                i += 1
            }
            parts.add(tmp.toString())
            return JsonPointer(parts)
        }

        @JvmStatic
        fun main(args: Array<String>) {
            val ptr0 = parse("")
            val ptr1 = parse("/")
            val ptr2 = parse("/2/~0th~1ree/one")
            println(parse("/"))
        }
    }
}

object JsonPointerSerializer : KSerializer<JsonPointer> {
    override val descriptor = PrimitiveSerialDescriptor("JsonPointer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): JsonPointer {
        return JsonPointer.parse(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: JsonPointer) {
        encoder.encodeString(value.toString())
    }

}

fun JsonElement.zipperTo(path: JsonPointer): JsonZipper {
    return path.parts.fold(JsonZipper(this)) { zipper, part ->
        zipper.down(part) ?: throw IllegalArgumentException("Couldn't descend to index: $part")
    }
}
