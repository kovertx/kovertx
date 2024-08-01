package io.kovertx.core

import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.eventbus.MessageConsumer

class EventBusBuilder(val eventBus: EventBus) {
    fun <T> consume(address: String, handler: Message<T>.() -> Unit): MessageConsumer<T> {
        return eventBus.consumer(address, handler)
    }

    fun <T> consumeLocal(address: String, handler: Message<T>.() -> Unit): MessageConsumer<T> {
        return eventBus.localConsumer(address, handler)
    }
}

fun Vertx.buildEventBus(action: EventBusBuilder.() -> Unit) = action(EventBusBuilder(eventBus()))

fun <T> EventBus.publishLocal(address: String, message: T) =
    publish(address, message, DeliveryOptions().setLocalOnly(true))
