# Kovert.x

Nice things in Vert.x for Kotlin.

Kovert.x is a set of libraries that mostly provide wrappers, extensions, and utilities around
[Vert.x](https://vertx.io/).

## Getting Started

This isn't on a major repository yet, so you're stuck with mine:

````
maven {
    name = "KovertxReleases"
    url = uri("https://mvn.kovertx.io/kovertx-releases")
}
````

...if you slap that in both settings.gradle.kts and build.gradle.kts, you can use this handy plugin:

````
id("io.kovertx.kovertx-gradle-plugin") version "0.0.2"
````

... that will automatically apply java/kotlin/kotlinx serialization plugins, and add a dependency
on `io.kovertx:kovertx-bom` (which also depends on `io.vertx:vertx-stack-depchain`). For easier use:

````

dependencies {
    implementation("io.kovertx:kovertx-web")
}
````

## Libraries

[kovertx-config](kovertx-config/readme.md)

Some wrappers around vertx-config, probably going away in favor of `kovertx-konfig`.

[kovertx-core](kovertx-core/readme.md)

Core utilities around `vertx-core`. `Koverticle` is probably the coolest bit, it adds an async
warmup step that's kinda neat.

[kovertx-konfig](kovertx-konfig/readme.md)

Sort of a replacement for `vertx-config`. Loses some of the update stuff (for now), but adds more
type safety and allows for resolving different parts of configuration in different ways.

[kovertx-serialization](kovertx-serialization/readme.md)

Glue to make it `kotlinx.serialization` fit better.

[kovertx-sql-client](kovertx-sql-client/readme.md)

General utilities on top of `vertx-sql-client` (like deserializing JSON with kotlinx.)

[kovertx-sql-migrator](kovertx-sql-migrator/readme.md)

A very simple library for automating SQL migrations using `vertx-sql-client` implementations.
Very early in development - please don't rely on it for anything important right now.

[kovertx-web](kovertx-web/readme.md)

Utilities around `vertx-web`, right now that's mostly just a Kotlin DSL for building routers.
