package io.kovertx.resilience

import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.temporal.ChronoUnit

class KeyedRateLimiterTests {
    @Test
    fun test() {
        val clock = MockClock()
        val limiter = TokenBucket.builder {
            instantSource = clock
            refillAmount = 1
            refillInterval = Duration.of(1, ChronoUnit.HOURS)
            capacity = 5
        }.buildIndexed<Int>()

        assert(limiter.tryConsume(1, 1))
        assert(limiter.tryConsume(1, 1))
        assert(limiter.tryConsume(1, 1))
        assert(limiter.tryConsume(1, 1))
        assert(limiter.tryConsume(1, 1))
        assert(!limiter.tryConsume(1, 1))

        clock.advance(Duration.of(1, ChronoUnit.HOURS))
        assert(limiter.tryConsume(1, 1))
        assert(!limiter.tryConsume(1, 1))

        clock.advance(Duration.of(1, ChronoUnit.HOURS))
        assert(limiter.tryConsume(1, 1))
        assert(!limiter.tryConsume(1, 1))

        clock.advance(Duration.of(2, ChronoUnit.HOURS))
        assert(limiter.tryConsume(1, 2))
        assert(!limiter.tryConsume(1, 1))

        clock.advance(Duration.of(10, ChronoUnit.HOURS))
        assert(limiter.tryConsume(2, 3))
        assert(limiter.tryConsume(3, 3))
    }
}