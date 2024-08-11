package io.kovertx.resilience

import java.time.Duration
import java.time.Instant
import java.time.InstantSource

class MockClock(start: Instant = Instant.now()) : InstantSource {
    private var now: Instant = start

    override fun instant(): Instant = now

    fun set(time: Instant) {
        now = time
    }

    fun advance(duration: Duration) {
        now = now.plus(duration)
    }
}