package io.kovertx.resilience

import java.time.Duration
import java.time.Instant
import java.time.InstantSource
import java.time.temporal.ChronoUnit

data class TokenBucketBuilder(
    /**
     * Maximum tokens in the bucket. This controls burst capacity.
     */
    var capacity: Long = 100,
    /**
     * How many tokens to add per refill interval.
     */
    var refillAmount: Long = 1,
    /**
     * How long to wait between adding tokens.
     */
    var refillInterval: Duration = Duration.of(1, ChronoUnit.SECONDS),
    /**
     * A time source.
     */
    override var instantSource: InstantSource = InstantSource.system()
) : RateLimiterBuilder {

    override fun build(): RateLimiter = TokenBucket(
        capacity, refillAmount, refillInterval, instantSource
    )
}

/**
 * A simple rate limited based on the token-bucket algorithm. A "bucket" of tokens is filled
 * will tokens at a fixed interval until it reaches maximum capacity. Consumers can consume
 * tokens (up to the current filled amount), but will fail if there aren't enough available.
 */
class TokenBucket(
    private val capacity: Long,
    private val refillAmount: Long,
    private val refillInterval: Duration,
    private val instantSource: InstantSource
) : RateLimiter {

    companion object {
        fun builder() = TokenBucketBuilder()
        fun builder(fn: TokenBucketBuilder.() -> Unit) = builder().also(fn)
    }

    private var tokens = capacity

    /**
     * The last instant associated with token increases. Note that this isn't always the
     * the exact time that refill() was called, but an integer multiple of refillInterval
     * from the previous refill time.
     */
    private var lastRefill: Instant = instantSource.instant()

    override val nextFullAt: Instant get() {
        val missing = capacity - tokens
        missing / refillAmount
        val requiredIntervals = (missing + refillAmount - 1) / refillAmount
        return lastRefill.plus(refillInterval.multipliedBy(requiredIntervals))
    }

    override fun tryConsume(count: Long): Boolean {
        refill()
        return if (tokens >= count) {
            tokens -= count
            true
        } else {
            false
        }
    }

    override val isAtCapacity: Boolean get() {
        refill()
        return tokens == capacity
    }

    /**
     * Refill the bucket if one or more refillIntervals have passed since the lastRefill.
     */
    private fun refill() {
        val now = instantSource.instant()
        val elapsed = Duration.between(lastRefill, now)
        if (elapsed >= refillInterval) {
            val numIntervals = elapsed.dividedBy(refillInterval)
            tokens = capacity.coerceAtMost(tokens + numIntervals * refillAmount)
            lastRefill = lastRefill.plus(refillInterval.multipliedBy(numIntervals))
        }
    }
}