# Kovertx Konfig

Konfig is meant as a solution to bringing configuration data into Vert.x.

## Basics

Konfig uses Kotlin DSLs to define how to retrieve configuration information for a property.

## Differences from Vert.x Config

This fills a similar role as [Vert.X Config](#TODO), but takes a different approach:

- Retrievers are defined pragmatically using Kotlin DSLs, no SPI required.
- Retrievers are defined in a more fine-grained manner - e.g. you define individual properties
  you want and where to look for them.

## Examples

### Konfig

The `Konfig` class can be used to define a set of re-usable konfig options.

````kotlin
class MySettings : Konfigurable() {
    val name by konfig {
        env("USER_NAME")
        file("./conf/user_name")
        // no default implies loading an instance will fail
    }
}

````

### Koverticle Integration

Easily pull configuration data when a Koverticle is started (properties delegated using
`konfig` will be initialized during the Koverticle "warmup" phase, before start)

````kotlin

class Settings : Konfigurable() {
    val port by konfig {
        parseInt {
            env("LISTEN_PORT")
        }
        default(8080)
    }
}

@Serializable
data class JsonSettings(
    val metadata: String = "foobar"
)

class MyVerticle : AbstractKoverticle {
    // A block of konfigs from a Konfigurable can be resolved, useful if you have groups of
    // things to re-use across multiple verticles
    val settings by resolveKonfig(Settings())
    
    // Single properties can be resolved using `konfig` (AbstractKoverticle isn't a Konfigurable,
    // but also has a konfig method like it)
    val username by konfig {
        // try to read file contents
        file("/run/secrets/username")
        // next look for an environment variable
        env("MY_USERNAME")
        // we can provide a default in case other retrievers fail
        default("DefaultUser")
    }
    
    // konfigs don't need to be strings, a json {} block will attempt to parse the results of
    // retrievers as a Json encoded object.
    val jsonSettings by konfig<JsonSettings> {
      json {
        file("conf/settings.json")
      }
    }
    
    fun startSync() {
        // startSync will only run if all konfigs are resolved during warmup (startup will fail if
        // those fail)
        println("Hello, ${username} (${jsonSettings.metadata})")
        println("Starting server on port ${settings.port}")
    }
}

````