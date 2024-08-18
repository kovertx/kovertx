package io.kovertx.serialization.json

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonElement

/**
 * A JSON Patch is just a list of operations.
 */
typealias JsonPatch = List<JsonPatchOp>

/**
 * Base class for all JSON Patch Operations.
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("op")
sealed class JsonPatchOp

/**
 * Adds a value into an object or array.
 */
@Serializable
@SerialName("add")
data class JsonAddOp(val path: JsonPointer, val value: JsonElement) : JsonPatchOp()

/**
 * Removes a value from an object or array.
 */
@Serializable
@SerialName("remove")
data class JsonRemoveOp(val path: JsonPointer) : JsonPatchOp()

/**
 * Replaces a value. Logically identical to using remove and then add.
 */
@Serializable
@SerialName("replace")
data class JsonReplaceOp(val path: JsonPointer, val value: JsonElement) : JsonPatchOp()

/**
 * Copies a value from one path to another by adding the value at a specified location to another
 * location.
 */
@Serializable
@SerialName("copy")
data class JsonCopyOp(val from: JsonPointer, val path: JsonPointer) : JsonPatchOp()

/**
 * Moves a value from one place to another by removing from one location and adding to another.
 */
@Serializable
@SerialName("move")
data class JsonMoveOp(val from: JsonPointer, val path: JsonPointer) : JsonPatchOp()

/**
 * Tests for equality at a certain path for a certain value.
 */
@Serializable
@SerialName("test")
data class JsonTestOp(val path: JsonPointer, val value: JsonElement) : JsonPatchOp()