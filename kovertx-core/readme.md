# Kovertx Core

## Features

### Koverticle / AbstractKoverticle

The `Koverticle` interface extends `io.vertx.Verticle` to add niceties for working with Kotlin and
Kovert.x.

Notably it adds a concept of "warmup" actions - actions that happen between the normal `init` and
`start` methods of a Verticle.

````
class MyVerticle : AbstractKoverticle() {
    init {
        warmup { ->
            println("Warming up")
        }
        
        warmup { warmupPromise ->
            println("Also warmup up...")
            warmupPromise.complete()
        }
    }
    
    override fun startSync() {
        // all warmup actions have completed successfully if we get here
        println("starting")
    }
}
````

These enable some nice quality of life features like property delegates that depend on
initialization (e.g. to get a `Vertx` instance) but should be run before startup.

````
class MyVerticle : AbstractKoverticle() {
    val foo : String by async {
        Future.succeeded("foo")
    }
}
````

### Wrap Vert.x Buffer as InputStream/OutputStream
