package io.kovertx.konfig

import io.vertx.core.Future

interface KonfigBuilder<T> {
    fun retriever(retriever: KonfigRetriever<T>)
}

fun KonfigBuilder<String>.file(path: String) = retriever { vertx ->
    vertx.fileSystem().readFile(path).map { it.toString(Charsets.UTF_8) }
}

fun KonfigBuilder<String>.prop(property: String) = retriever {
    val value = System.getProperty(property)
    if (value == null) {
        Future.failedFuture("No property ${property}")
    } else {
        Future.succeededFuture(value)
    }
}

fun KonfigBuilder<String>.env(variable: String) = retriever {
    val value = System.getenv(variable)
    if (value == null) {
        Future.failedFuture("No such environment variable ${variable}")
    } else {
        Future.succeededFuture(value)
    }
}

fun <T> KonfigBuilder<T>.default(value: T) = retriever { Future.succeededFuture(value) }
