package io.kovertx.sql.migrator.postgres

import io.kovertx.sql.migrator.common.SqlVariantParser
import io.kovertx.sql.migrator.common.antlr
import io.kovertx.sql.migrator.grammar.PostgreSQLLexer
import io.kovertx.sql.migrator.grammar.PostgreSQLParser
import io.kovertx.sql.migrator.grammar.PostgreSQLParser.StmtContext

var postgresParser = SqlVariantParser {
    antlr(::PostgreSQLLexer, ::PostgreSQLParser, { it.root() }, setOf(StmtContext::class.java))
}