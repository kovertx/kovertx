package io.kovertx.serialization.json

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * Utility class for applying JSON Patch Operations to a JsonElement.
 */
object JsonPatcher {

    /**
     * Applies a sequence of JSON Patch Operations in order.
     * @see apply
     * @param root the JSON tree to patch
     * @param patch the list of JSON Patch Operations to apply.
     */
    fun apply(root: JsonElement, patch: JsonPatch): JsonElement {
        return patch.fold(root) { curr, op -> apply(curr, op) }
    }

    /**
     * Apply a single JSON Patch Operation. If any patch fails the whole operation will fail. The
     * root JsonElement passed in will not be modified, instead a new JsonElement is returned.
     * @param root the JSON tree to patch
     * @param op the JSON Patch Operation to apply
     */
    fun apply(root: JsonElement, op: JsonPatchOp): JsonElement {
        return when (op) {
            is JsonAddOp -> add(root, op.path, op.value)
            is JsonReplaceOp -> replace(root, op.path, op.value)
            is JsonRemoveOp -> remove(root, op.path)
            is JsonCopyOp -> copy(root, op.from, op.path)
            is JsonMoveOp -> move(root, op.from, op.path)
            is JsonTestOp -> {
                val value = root.zipperTo(op.path).toJson()
                if (value != op.value) throw IllegalStateException("Test failed")
                return root
            }
        }
    }

    fun add(root: JsonElement, path: JsonPointer, value: JsonElement): JsonElement {
        if (path.isRoot()) return value
        val lastPath = path.lastPart()
        val zipper = root.zipperTo(path.parent())
        return zipper.update { parent ->
            when (parent) {
                is JsonObject -> {
                    JsonObject(parent.plus(lastPath to value))
                }
                is JsonArray -> {
                    if (lastPath == "-") {
                        JsonArray(parent.plus(value))
                    } else {
                        val idx = lastPath.toInt()
                        val arr = parent.toMutableList()
                        arr.add(idx, value)
                        JsonArray(arr)
                    }
                }
                else -> throw IllegalArgumentException("Cannot add to non-array/object")
            }
        }.root().toJson()
    }

    fun replace(root: JsonElement, path: JsonPointer, value: JsonElement): JsonElement {
        if (path.isRoot()) return value
        val lastPath = path.lastPart()
        val zipper = root.zipperTo(path.parent())
        return zipper.update { parent ->
            when (parent) {
                is JsonObject -> JsonObject(parent.plus(lastPath to value))
                is JsonArray -> {
                    if (lastPath == "-") {
                        JsonArray(parent.plus(value))
                    } else {
                        val idx = lastPath.toInt()
                        JsonArray(parent.toMutableList().apply { set(idx, value) })
                    }
                }
                else -> throw IllegalArgumentException("Cannot add to non-array/object")
            }
        }.root().toJson()
    }

    fun remove(root: JsonElement, path: JsonPointer): JsonElement {
        val lastPath = path.lastPart()
        val zipper = root.zipperTo(path.parent())
        return zipper.update { parent ->
            when (parent) {
                is JsonObject -> JsonObject(parent.minus(lastPath))
                is JsonArray -> {
                    val idx = lastPath.toInt()
                    JsonArray(parent.toMutableList().apply { removeAt(idx) })
                }
                else -> throw IllegalArgumentException("Cannot remove from non-array/object")
            }
        }.root().toJson()
    }

    fun copy(root: JsonElement, from: JsonPointer, path: JsonPointer): JsonElement {
        val value = root.zipperTo(from).toJson()
        val lastPath = path.lastPart()
        val zipper = root.zipperTo(path.parent())
        return zipper.update { parent ->
            when (parent) {
                is JsonObject -> {
                    JsonObject(parent.plus(lastPath to value))
                }
                is JsonArray -> {
                    if (lastPath == "-") {
                        JsonArray(parent.plus(value))
                    } else {
                        val idx = lastPath.toInt()
                        JsonArray(parent.toMutableList().apply {
                            if (idx >= size) add(value) else set(idx, value)
                        })
                    }
                }
                else -> throw IllegalArgumentException("Cannot add to non-array/object")
            }
        }.root().toJson()
    }

    fun move(root: JsonElement, from: JsonPointer, path: JsonPointer): JsonElement {
        if (from == path) return root
        return remove(copy(root, from, path), from)
    }
}