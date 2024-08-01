package io.kovertx.core

import io.vertx.core.buffer.Buffer
import java.io.OutputStream

class BufferOutputStream(val buffer: Buffer, private var pos: Int = 0) : OutputStream() {
    override fun write(b: Int) {
        buffer.setByte(pos, b.toByte())
    }

    override fun write(b: ByteArray) {
        buffer.setBytes(pos, b)
        pos += b.size
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        buffer.setBytes(pos, b, off, len)
        pos += len
    }
}
