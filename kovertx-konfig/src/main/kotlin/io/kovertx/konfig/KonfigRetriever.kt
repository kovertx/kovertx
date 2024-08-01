package io.kovertx.konfig

import io.vertx.core.Future
import io.vertx.core.Vertx

typealias KonfigRetriever<T> = (Vertx) -> Future<T>
