package io.kovertx.konfig

import io.vertx.core.Future
import io.vertx.core.Vertx

class KonfigBuilderImpl<T> : KonfigBuilder<T> {
    private val retrievers = mutableListOf<(Vertx) -> Future<T>>()

    override fun retriever(retriever: (Vertx) -> Future<T>) {
        retrievers.add(retriever)
    }

    fun build(): KonfigDelegateProperty<T> = KonfigDelegateProperty(retrievers)
}
