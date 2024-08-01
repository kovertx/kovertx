package io.kovertx.konfig

import io.vertx.core.Future
import kotlinx.serialization.json.Json

class JsonKonfigBuilder<T>(proxiedBuilder: KonfigBuilder<T>, mapper: (String) -> T) :
    MappedKonfigBuilder<String, T>(proxiedBuilder, mapper) {

    fun defaultEmpty() = retriever { Future.succeededFuture("{}") }
}

inline fun <reified T> KonfigBuilder<T>.json(block: JsonKonfigBuilder<T>.() -> Unit) {
    JsonKonfigBuilder(this) { Json.decodeFromString(it) }.block()
}
