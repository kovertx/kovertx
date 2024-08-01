package io.kovertx.konfig

import io.vertx.core.Future
import io.vertx.core.Vertx

open class MappedKonfigBuilder<A, B>(
    private val proxiedBuilder: KonfigBuilder<B>,
    private val mapper: (A) -> B
) : KonfigBuilder<A> {
    override fun retriever(retriever: (Vertx) -> Future<A>) {
        proxiedBuilder.retriever { retriever(it).map(mapper) }
    }
}

fun <A, B> KonfigBuilder<B>.mapping(mapper: (A) -> B, block: KonfigBuilder<A>.() -> Unit) {
    block(MappedKonfigBuilder(this, mapper))
}

fun KonfigBuilder<Int>.parseInt(block: KonfigBuilder<String>.() -> Unit) {
    mapping(java.lang.Integer::parseInt, block)
}

fun KonfigBuilder<Long>.parseLong(block: KonfigBuilder<String>.() -> Unit) {
    mapping(java.lang.Long::parseLong, block)
}

fun KonfigBuilder<Float>.parseFloat(block: KonfigBuilder<String>.() -> Unit) {
    mapping(java.lang.Float::parseFloat, block)
}

fun KonfigBuilder<Double>.parseDouble(block: KonfigBuilder<String>.() -> Unit) {
    mapping(java.lang.Double::parseDouble, block)
}

fun KonfigBuilder<Boolean>.truthy(block: KonfigBuilder<String>.() -> Unit) {
    mapping({ str ->
        setOf("1", "yes", "true", "t", "y", "ok").contains(str.trim().lowercase())
    }, block)
}