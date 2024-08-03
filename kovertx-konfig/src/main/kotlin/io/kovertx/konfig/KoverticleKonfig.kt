package io.kovertx.konfig

import io.kovertx.core.Koverticle
import io.kovertx.core.async

object UnitializedKonfigProperty

fun <T> Koverticle.konfig(block: KonfigBuilder<T>.() -> Unit): KonfigDelegateProperty<T> {
    val property = KonfigBuilderImpl<T>().apply(block).build()
    warmup { -> property.resolve(vertx) }
    return property
}

fun <T : Konfigurable> Koverticle.resolveKonfig(konfig: T) = async {
    return@async konfig.resolve(vertx).map(konfig)
}
