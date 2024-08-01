package io.kovertx.config

import io.kovertx.core.Koverticle
import io.kovertx.core.async

inline fun <reified T> Koverticle.config(crossinline fn: ConfigRetrieverBuilder.() -> Unit) =
    async {
        buildConfigRetriever<T>(vertx, fn).config
    }
