package io.kovertx.serialization.json

import kotlinx.serialization.json.*

/**
 * A Zipper implementation for JsonElement.
 *
 * JsonElements are immutable. A zipper keeps track of the context around a current element.
 * Modifying the element pointed to by a zipper is essentially a replacement that then reconstructs
 * parent elements around it.
 *
 * Note that JsonZipper itself is thus immutable. Calling update(...) will return a new JsonZipper
 * that points to the updated element in a new JsonElement hierarchy.
 */
class JsonZipper(
    private val current: JsonElement,
    private val parent: JsonZipper? = null,
    private val path: JsonPathItem? = null
) {
    /**
     * Get the root node of this zipper.
     */
    fun root(): JsonZipper = parent?.root() ?: this

    /**
     * Get this zipper's immediate ancestor.
     * @return the parent JsonZipper
     */
    fun up() = parent

    /**
     * Creates a new zipper pointing to the immediate child of this zipper's current element.
     * If the current element is an array key will be parsed as a Int index, otherwise it will be
     * used as an object property.
     * @return the child zipper, or null if no such child exists
     */
    fun down(key: String): JsonZipper? {
        if (current is JsonArray) return down(key.toInt())
        if (current !is JsonObject) return null
        val child = current[key] ?: return null
        return JsonZipper(child, this, JsonPathProperty(key))
    }

    /**
     * Creates a new zipper pointing to the immediate child of this zipper's current element.
     * The current element is expected to be a JsonArray.
     * @return this child zipper, or null if no such index exists
     */
    fun down(index: Int): JsonZipper? {
        if (current !is JsonArray) return null
        val child = current.getOrNull(index) ?: return null
        return JsonZipper(child, this, JsonPathIndex(index))
    }

    /**
     * Creates a new zipper with the current JsonElement replaced with a new value. The created
     * zipper will have a new parent if it isn't itself a root node.
     */
    fun update(newValue: JsonElement): JsonZipper {
        return when (path) {
            is JsonPathProperty -> {
                val newParent = parent!!.update { oldParentElement ->
                    JsonObject(oldParentElement.jsonObject.plus(path.key to newValue))
                }
                JsonZipper(newValue, newParent, path)
            }
            is JsonPathIndex -> {
                val newParent = parent!!.update { oldParentElement ->
                    JsonArray(oldParentElement.jsonArray.toMutableList().apply {
                        set(path.index, newValue)
                    })
                }
                JsonZipper(newValue, newParent, path)
            }
            null -> JsonZipper(newValue, parent, path)
        }
    }

    /**
     * Creates a new zipper with the current JsonElement value replaced with the value returned by
     * passing it to fn.
     * @see update
     */
    fun update(fn: (JsonElement) -> JsonElement): JsonZipper = update(fn(current))

    /**
     * Gets the current JsonElement being pointed to.
     */
    fun toJson(): JsonElement {
        return current
    }
}

sealed interface JsonPathItem
private data class JsonPathProperty(val key: String) : JsonPathItem
private data class JsonPathIndex(val index: Int) : JsonPathItem