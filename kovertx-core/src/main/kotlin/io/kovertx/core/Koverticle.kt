package io.kovertx.core

import io.vertx.core.*

interface Koverticle : Verticle {

    /**
     * Register a pre-start action that should be allowed to complete before other startup
     * activities implemented in Verticle.start(Promise<Void>).
     */
    fun warmup(action: () -> Future<Void>)

    /** @see warmup */
    fun warmup(action: (Promise<Void>) -> Unit) {
        val promise = Promise.promise<Void>()
        warmup { ->
            action(promise)
            promise.future()
        }
    }

    /**
     * Appends a warmup action to be executed before startup, with the expectation that the action
     * will complete when action has completed.
     *
     * @see warmup
     */
    fun warmupSync(action: () -> Unit) {
        warmup { promise ->
            try {
                action()
                promise.complete()
            } catch (t: Throwable) {
                promise.fail(t)
            }
        }
    }
}

open class AbstractKoverticle : Koverticle {
    private var vertx: Vertx? = null
    private var context: Context? = null

    private val warmupActions = mutableListOf<() -> Future<Void>>()

    override fun getVertx(): Vertx = this.vertx!!

    override fun init(vertx: Vertx, context: Context) {
        this.vertx = vertx
        this.context = context
    }

    /** @see Koverticle.warmup */
    override fun warmup(action: () -> Future<Void>) {
        warmupActions.add(action)
    }

    /** @see Verticle.start */
    final override fun start(startPromise: Promise<Void>) {
        println("Koverticle starting with ${warmupActions.size} prestarts")
        val warmupFutures = warmupActions.map { it() }
        Future.all(warmupFutures)
            .onSuccess { startAsync(startPromise) }
            .onFailure { startPromise.fail(it) }
    }

    open fun startAsync(startPromise: Promise<Void>) {
        try {
            startSync()
            startPromise.complete()
        } catch (t: Throwable) {
            startPromise.fail(t)
        }
    }

    open fun startSync() {
        // no-op default
    }

    override fun stop(stopPromise: Promise<Void>) {
        try {
            stop()
            stopPromise.complete()
        } catch (t: Throwable) {
            stopPromise.fail(t)
        }
    }

    open fun stop() {
        // no-op default
    }
}
