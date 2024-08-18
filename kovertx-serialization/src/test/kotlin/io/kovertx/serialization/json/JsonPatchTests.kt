package io.kovertx.serialization.json

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromStream
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.stream.Collectors
import java.util.stream.Stream

class JsonPatchTests {

    @Serializable
    data class TestCase(
        val comment: String? = null,
        val doc: JsonElement,
        val patch: JsonElement,
        val expected: JsonElement? = null,
        val error: String? = null,
        val disabled: Boolean? = null) {
        override fun toString(): String {
            return "TestCase(${comment ?: "noname"})"
        }
    }

    val jsonIgnoringUnknown = Json {
        ignoreUnknownKeys = true
    }

    @ParameterizedTest
    @MethodSource("provideTestCases")
    fun runTestCase(test: TestCase) {
        if (test.disabled == true) {
            println("Disabled: ${test.comment}")
            return
        }

        println("Testing: ${test.comment}")
        println("Doc: ${test.doc}")
        println("Patch: ${test.patch}")
        if (test.expected != null) println("Expect: ${test.expected}")
        if (test.error != null) println("Error: ${test.error}")

        try {
            val patch = jsonIgnoringUnknown.decodeFromString<List<JsonPatchOp>>(test.patch.toString())
            val doc = test.doc
            val actual = JsonPatcher.apply(doc, patch)

            if (test.expected != null) {
                Assertions.assertEquals(test.expected, actual)
            }

        } catch (e: Exception) {
            if (test.error == null) {
                Assertions.fail<Unit>(e)
            } else {
                println("Expected failure: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    companion object {
        fun getResourceFileAsString(fileName: String): String {
            val classLoader = ClassLoader.getSystemClassLoader()
            classLoader.getResourceAsStream(fileName).use { `is` ->
                InputStreamReader(`is`).use { isr -> BufferedReader(isr).use { reader -> return reader.lines().collect(Collectors.joining(System.lineSeparator())) } }
            }
        }

        @OptIn(ExperimentalSerializationApi::class)
        @JvmStatic
        fun provideTestCases(): Stream<TestCase> {
            return ClassLoader.getSystemClassLoader()
                .getResourceAsStream("json_patch_tests.json")!!
                .use { stream ->
                    Json.decodeFromStream<List<TestCase>>(stream)
                        .stream()
                }
        }
    }
}