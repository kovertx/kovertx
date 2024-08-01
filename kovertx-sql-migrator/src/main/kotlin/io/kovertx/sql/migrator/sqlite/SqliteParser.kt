package io.kovertx.sql.migrator.postgres

import io.kovertx.sql.migrator.common.SqlVariantParser
import io.kovertx.sql.migrator.common.antlr
import io.kovertx.sql.migrator.grammar.SQLiteLexer
import io.kovertx.sql.migrator.grammar.SQLiteParser
import io.kovertx.sql.migrator.grammar.SQLiteParser.Sql_stmtContext

val sqliteParser = SqlVariantParser {
    antlr(::SQLiteLexer, ::SQLiteParser, { it.parse() }, setOf(Sql_stmtContext::class.java))
}
