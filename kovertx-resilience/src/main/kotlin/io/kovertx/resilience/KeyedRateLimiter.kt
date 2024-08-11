package io.kovertx.resilience

import java.time.Instant
import java.time.InstantSource
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * A keyed rate limiter is really just an abstraction around multiple rate limiters that are
 * accessed by some key.
 *
 * The basic assumption is that a new rate limited is constructed on-demand for keys that are
 * requested, where the keys correspond to individual resources that should be rate-limited
 * independently.
 */
class KeyedRateLimiter<T>(
    private val builder: RateLimiterBuilder,
    private val instanceSource: InstantSource) {

    private val buckets = ConcurrentHashMap<T, RateLimiter>()

    /**
     * Rate limiters are pushed into a priority queue to track when they're next eligible for
     * removal. We assume new rate limiters are constructed at maximum capacity, so if an
     * unused limiter hits that capacity it's eligible to be culled.
     */
    private val cleanupQueue = PriorityQueue<ScheduledCleanup<T>> { a, b ->
        a.recordedNextFullTime.compareTo(b.recordedNextFullTime)
    }

    fun tryConsume(key: T, numTokens: Long = 1): Boolean {
        val bucket = buckets.computeIfAbsent(key) {
            val tmp = builder.build()
            cleanupQueue.add(ScheduledCleanup(key, tmp.nextFullAt, tmp))
            tmp
        }
        val test = bucket.tryConsume(numTokens)
        cleanup()
        return test
    }

    private fun cleanup() {
        val now = instanceSource.instant()
        // iterate through any rate limiters that were recorded as being expected to be full
        // before now
        while (cleanupQueue.peek()?.recordedNextFullTime?.isBefore(now) == true) {
            val item = cleanupQueue.poll()
            if (item.limiter.isAtCapacity) {
                // if it's actually at capacity (e.g. hasn't been consumed recently) remove
                // then cache entry (we can safely re-construct it in a full state again later)
                buckets.remove(item.key)
            } else {
                // if it's not at capacity (e.g. more tokens have been consumed since last
                // scheduled) re-schedule the next cleanup task for it
                cleanupQueue.add(item.copy(recordedNextFullTime = item.limiter.nextFullAt))
            }
        }
    }

    private data class ScheduledCleanup<T>(
        val key: T,
        val recordedNextFullTime: Instant,
        val limiter: RateLimiter
    )
}

fun <T> RateLimiterBuilder.buildIndexed() = KeyedRateLimiter<T>(this, instantSource)