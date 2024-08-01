package io.kovertx.core

import io.vertx.core.buffer.Buffer
import java.io.InputStream
import java.io.OutputStream

class BufferInputStream(val buffer: Buffer, private var pos: Int = 0) : InputStream() {

    private var mark: Int = pos

    override fun available() = (buffer.length() - pos)

    override fun read() =
        if (pos < buffer.length()) {
            buffer.getByte(pos++).toInt() and 0xFF
        } else {
            -1
        }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (len <= 0) return 0
        val actual = len.coerceAtMost(available())
        buffer.getBytes(pos, pos + actual, b, off)
        pos += actual
        return actual
    }

    override fun readAllBytes(): ByteArray {
        val result = buffer.getBytes(pos, buffer.length())
        pos = buffer.length()
        return result
    }

    override fun readNBytes(b: ByteArray, off: Int, len: Int): Int {
        val n = read(b, off, len)
        return if (n == -1) 0 else n
    }

    override fun transferTo(out: OutputStream?): Long {
        if (out is BufferOutputStream) {
            val slice = buffer.slice(pos, buffer.length())
            out.buffer.appendBuffer(slice)
            pos = buffer.length()
            return slice.length().toLong()
        }
        return super.transferTo(out)
    }
}
