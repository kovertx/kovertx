package io.kovertx.resilience

import java.time.Instant

interface RateLimiter {
    /**
     * Attempt to consume resources.
     * @return true if resources were consumed, otherwise false
     */
    fun tryConsume(count: Long = 1): Boolean

    /**
     * Is this rate limiter at maximum capacity (e.g. is it possible to consume the maximum
     * possible resource count immediately)
     */
    val isAtCapacity: Boolean

    /**
     * The instant this rate limited is expected to reach maximum capacity, assuming no
     * further resources are consumed before then.
     */
    val nextFullAt: Instant
}