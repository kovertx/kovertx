# Kovert.x Web

## Features

### buildRouter DSL

A DSL wrapper around `Router` and `Route`.

````
buildRouter(vertx) {
    get("/greeting") {
        handler { ctx ->
            ctx.body("Hello")
        }
    }
}
````
