package io.kovertx.resilience

import java.time.InstantSource

interface RateLimiterBuilder {
    var instantSource: InstantSource
    fun build(): RateLimiter
}