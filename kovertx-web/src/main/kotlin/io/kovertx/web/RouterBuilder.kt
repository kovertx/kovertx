package io.kovertx.web

import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.AllowForwardHeaders
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import java.util.function.Function

class RouterBuilder(private val vertx: Vertx) {
    val router = Router.router(vertx)

    var allowForward: AllowForwardHeaders
        get() = TODO()
        set(value) {
            router.allowForward(value)
        }

    fun route(block: RouteBuilder.() -> Unit) =
        RouteBuilder(vertx, router.route()).also(block).route

    fun route(path: String, block: RouteBuilder.() -> Unit) =
        RouteBuilder(vertx, router.route(path)).also(block).route

    fun route(method: HttpMethod, path: String, block: RouteBuilder.() -> Unit) =
        RouteBuilder(vertx, router.route(method, path)).also(block).route

    fun routeWithRegex(regex: String, block: RouteBuilder.() -> Unit) =
        RouteBuilder(vertx, router.routeWithRegex(regex)).also(block).route

    fun routeWithRegex(method: HttpMethod, regex: String, block: RouteBuilder.() -> Unit) =
        RouteBuilder(vertx, router.route(method, regex)).also(block).route

    fun get(block: RouteBuilder.() -> Unit) = RouteBuilder(vertx, router.get()).also(block).route

    fun get(path: String, block: RouteBuilder.() -> Unit) =
        RouteBuilder(vertx, router.get(path)).also(block).route

    fun getWithRegex(regex: String, block: RouteBuilder.() -> Unit) =
        RouteBuilder(vertx, router.getWithRegex(regex)).also(block).route

    fun head(block: RouteBuilder.() -> Unit) = RouteBuilder(vertx, router.head()).also(block).route

    fun head(path: String, block: RouteBuilder.() -> Unit) =
        RouteBuilder(vertx, router.head(path)).also(block).route

    fun headWithRegex(regex: String, block: RouteBuilder.() -> Unit) =
        RouteBuilder(vertx, router.headWithRegex(regex)).also(block).route

    fun options(block: RouteBuilder.() -> Unit) =
        RouteBuilder(vertx, router.options()).also(block).route

    fun options(path: String, block: RouteBuilder.() -> Unit) =
        RouteBuilder(vertx, router.options(path)).also(block).route

    fun optionsWithRegex(regex: String, block: RouteBuilder.() -> Unit) =
        RouteBuilder(vertx, router.optionsWithRegex(regex)).also(block).route

    fun put(block: RouteBuilder.() -> Unit) = RouteBuilder(vertx, router.put()).also(block).route

    fun put(path: String, block: RouteBuilder.() -> Unit) =
        RouteBuilder(vertx, router.put(path)).also(block).route

    fun putWithRegex(regex: String, block: RouteBuilder.() -> Unit) =
        RouteBuilder(vertx, router.putWithRegex(regex)).also(block).route

    fun post(block: RouteBuilder.() -> Unit) = RouteBuilder(vertx, router.post()).also(block).route

    fun post(path: String, block: RouteBuilder.() -> Unit) =
        RouteBuilder(vertx, router.post(path)).also(block).route

    fun postWithRegex(regex: String, block: RouteBuilder.() -> Unit) =
        RouteBuilder(vertx, router.postWithRegex(regex)).also(block).route

    fun delete(block: RouteBuilder.() -> Unit) =
        RouteBuilder(vertx, router.delete()).also(block).route

    fun delete(path: String, block: RouteBuilder.() -> Unit) =
        RouteBuilder(vertx, router.delete(path)).also(block).route

    fun deleteWithRegex(regex: String, block: RouteBuilder.() -> Unit) =
        RouteBuilder(vertx, router.deleteWithRegex(regex)).also(block).route

    fun trace(block: RouteBuilder.() -> Unit) =
        RouteBuilder(vertx, router.trace()).also(block).route

    fun trace(path: String, block: RouteBuilder.() -> Unit) =
        RouteBuilder(vertx, router.trace(path)).also(block).route

    fun traceWithRegex(regex: String, block: RouteBuilder.() -> Unit) =
        RouteBuilder(vertx, router.traceWithRegex(regex)).also(block).route

    fun connect(block: RouteBuilder.() -> Unit) =
        RouteBuilder(vertx, router.connect()).also(block).route

    fun connect(path: String, block: RouteBuilder.() -> Unit) =
        RouteBuilder(vertx, router.connect(path)).also(block).route

    fun connectWithRegex(regex: String, block: RouteBuilder.() -> Unit) =
        RouteBuilder(vertx, router.connectWithRegex(regex)).also(block).route

    fun patch(block: RouteBuilder.() -> Unit) =
        RouteBuilder(vertx, router.patch()).also(block).route

    fun patch(path: String, block: RouteBuilder.() -> Unit) =
        RouteBuilder(vertx, router.patch(path)).also(block).route

    fun patchWithRegex(regex: String, block: RouteBuilder.() -> Unit) =
        RouteBuilder(vertx, router.patchWithRegex(regex)).also(block).route

    fun modifiedHandler(handler: Handler<Router>) {
        router.modifiedHandler(handler)
    }
}

class RouteBuilder(private val vertx: Vertx, val route: Route) {
    val metadata = route.metadata()

    fun putMetadata(key: String, value: Any) {
        route.putMetadata(key, value)
    }

    fun method(method: HttpMethod) {
        route.method(method)
    }

    val isRegexPath
        get() = route.isRegexPath

    var path: String
        get() = route.path
        set(value) {
            route.path(value)
        }

    fun consumes(contentType: String) {
        route.consumes(contentType)
    }

    fun produces(contentType: String) {
        route.produces(contentType)
    }

    fun handler(handler: Handler<RoutingContext>) {
        route.handler(handler)
    }

    fun blockingHandler(handler: Handler<RoutingContext>) {
        route.blockingHandler(handler)
    }

    fun <T> respond(function: Function<RoutingContext, Future<T>>) {
        route.respond(function)
    }

    fun enable() {
        route.enable()
    }

    fun disable() {
        route.disable()
    }

    fun subRouter(router: Router) {
        route.subRouter(router)
    }

    fun subRouter(block: RouterBuilder.() -> Unit) {
        subRouter(buildRouter(vertx, block))
    }
}

fun buildRouter(vertx: Vertx, block: RouterBuilder.() -> Unit): Router {
    return RouterBuilder(vertx).also(block).router
}
