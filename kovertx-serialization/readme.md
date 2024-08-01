# Kovert.x Serialization

Kotlin reflectionless serialization support for Vert.x

## Features

### Encode and Decode with Vert.x Buffer

### Create and register MessageCodec that uses kotlinx.serialization

````kotlin

@Serializable
data class Greeting(val who: String)

````