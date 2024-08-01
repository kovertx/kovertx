package io.kovertx.core

import io.vertx.core.Future
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface ReadOnlyKoverticleProperty<T> : ReadOnlyProperty<Koverticle, T>

interface ReadWriteKoverticleProperty<T> :
    ReadWriteProperty<Koverticle, T>, ReadOnlyKoverticleProperty<T>

class KoverticlePropertyImpl<T> : ReadWriteKoverticleProperty<T> {
    private var value: Any = object {}

    fun setValue(value: T) {
        this.value = value as Any
    }

    override operator fun getValue(thisRef: Koverticle, property: KProperty<*>): T {
        return value as T
    }

    override operator fun setValue(thisRef: Koverticle, property: KProperty<*>, value: T) {
        this.value = value as Any
    }
}

/**
 * Creates a new instance of KoverticleProperty<T> that uses the specified aysnc initializer to
 * fulfill the value. The constructed instance will run as a Koverticle warmup action, ensuring the
 * property is fulfilled (or causes start to fail) before startAsync or startSync is run.
 */
fun <T> Koverticle.async(fn: () -> Future<T>): ReadOnlyKoverticleProperty<T> {
    val delegate = KoverticlePropertyImpl<T>()
    warmup { -> fn().onSuccess { delegate.setValue(it) }.mapEmpty() }
    return delegate
}
