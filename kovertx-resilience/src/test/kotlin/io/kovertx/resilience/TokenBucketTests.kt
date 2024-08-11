package io.kovertx.resilience

import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.temporal.ChronoUnit

class TokenBucketTests {
    @Test
    fun simple() {
        val clock = MockClock()
        val bucket = TokenBucket.builder {
            instantSource = clock
        }.build()

        assert(bucket.isAtCapacity)
        assert(bucket.tryConsume(1))
        assert(!bucket.isAtCapacity)
        assert(bucket.nextFullAt > clock.instant())
        assert(bucket.nextFullAt == clock.instant().plus(Duration.of(1, ChronoUnit.SECONDS)))

        clock.advance(Duration.of(1, ChronoUnit.SECONDS))
        assert(bucket.isAtCapacity)

        for (i in 0 until 100) {
            assert(bucket.tryConsume())
        }
        assert(!bucket.tryConsume())
        for (i in 0 until 99) {
            clock.advance(Duration.of(1, ChronoUnit.SECONDS))
            assert(!bucket.isAtCapacity)
        }
        clock.advance(Duration.of(1, ChronoUnit.SECONDS))
        assert(bucket.isAtCapacity)
    }
}

