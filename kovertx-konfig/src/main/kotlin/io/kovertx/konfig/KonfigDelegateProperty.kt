package io.kovertx.konfig

import io.vertx.core.Future
import io.vertx.core.Vertx
import kotlin.reflect.KProperty

class KonfigDelegateProperty<T>(private val retrievers: List<KonfigRetriever<T>>) {
    private var value: Any? = UnitializedKonfigProperty

    fun resolve(vertx: Vertx): Future<Void> {
        return retrievers
            .fold(Future.failedFuture<T>("no resolver")) { future, retriever ->
                future.recover { t -> retriever(vertx) }
            }
            .onSuccess { value = it }
            .mapEmpty()
    }

    operator fun getValue(o: Any, p: KProperty<*>): T {
        if (value === UnitializedKonfigProperty) throw RuntimeException("Uninitialized konfig")
        return value as T
    }

    fun optional(): KonfigDelegateProperty<T?> {
        val optionalProperty =
            KonfigDelegateProperty(
                retrievers
                    .map { retriever -> { vertx: Vertx -> retriever(vertx).map { it } } }
                    .plus { Future.succeededFuture<T?>(null) }
            )
        return optionalProperty
    }
}
