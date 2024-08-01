package io.kovertx.konfig

import io.vertx.core.Future
import io.vertx.core.Vertx

open class Konfigurable : Runnable {
    private val properties = mutableListOf<KonfigDelegateProperty<*>>()

    fun <T> konfig(block: KonfigBuilder<T>.() -> Unit): KonfigDelegateProperty<T> {
        val property = KonfigBuilderImpl<T>().apply(block).build()
        properties.add(property)
        return property
    }

    fun resolve(vertx: Vertx): Future<Void> {
        val future =
            properties.fold(Future.succeededFuture<Void>()) { prev, retriever ->
                prev.compose { retriever.resolve(vertx) }
            }
        future.onSuccess { run() }
        return future
    }

    override fun run() {
        // default no-op
    }
}
