package io.kovertx.sql.migrator.common

interface SqlParser {
    fun split(content: String): List<String>
    fun hash(content: String): String
}