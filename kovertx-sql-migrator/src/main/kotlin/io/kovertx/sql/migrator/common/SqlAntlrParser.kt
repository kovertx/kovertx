package io.kovertx.sql.migrator.common

import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.ErrorNode
import org.antlr.v4.runtime.tree.ParseTreeListener
import org.antlr.v4.runtime.tree.TerminalNode
import java.security.MessageDigest

/**
 * Standard utility class for parsing SQL dialects using an ANTLR 4 based lexer/parser/listener.
 *
 * This has a few purposes
 * - Splitting migration code into individual statements (most drivers don't handle multiple
 *   statements in a single query)
 * - Normalizing SQL into a canonical form. Things like comments and whitespace variations should be
 *   ignored when computing the hash of a migration step, so that things like formatting changes
 *   won't break consistency.
 */
open class SqlAntlrParser<TParser : Parser>(
    private val lexerFactory: (CharStream) -> Lexer,
    private val parserFactory: (TokenStream) -> TParser,
    private val parseRoot: (TParser) -> Unit,
    private val statementContextClasses: Set<Class<*>>,
    private val hashSalt: String = ""
) : SqlParser {
    /**
     * Parses content, collecting a list of individual normalized SQL statements.
     */
    override fun split(content: String): List<String> {
        val lexer = lexerFactory(CharStreams.fromString(content))
        val parser = parserFactory(CommonTokenStream(lexer))
        val listener = SqlStatementListener(statementContextClasses)
        parser.addParseListener(listener)
        parseRoot(parser)
        return listener.statements
    }

    /**
     * Hash SQL statements for consistency
     */
    @OptIn(ExperimentalStdlibApi::class)
    override fun hash(content: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        split(content).forEach { stmt -> digest.update(stmt.encodeToByteArray()) }
        digest.update(hashSalt.toByteArray())
        return digest.digest().toHexString()
    }
}

fun <TParser : Parser> SqlVariantParserBuilder.antlr(
    lexerFactory: (CharStream) -> Lexer,
    parserFactory: (TokenStream) -> TParser,
    parseRoot: (TParser) -> Unit,
    statementContextClasses: Set<Class<*>>,
    hashSalt: String = "") {
    variant(SqlAntlrParser(lexerFactory, parserFactory, parseRoot, statementContextClasses, hashSalt))
}

/**
 * Very simplistic ParseTreeListener that collects individual SQL statements. The basic assumption
 * is that an ANTLR grammar will have or more rules that correspond to top-level statements in the
 * SQL dialect - we keep track of when we enter/exit a top-level statement and buffer all the
 * terminal nodes within.
 *
 * Assumptions:
 * - Whitespace and comments are sent HIDDEN channel during lexing and won't be visited during parse
 * - It is safe to inject a single space character (0x20) between terminals in the grammar
 * - The statement rule(s) are not semicolon-terminated (and a semicolon can be safely appended)
 */
class SqlStatementListener(
    private val statementContextClasses: Set<Class<*>>
) : ParseTreeListener {

    private var statementDepth = 0
    private val statementBuffer = StringBuilder()
    val statements = mutableListOf<String>()

    override fun visitTerminal(node: TerminalNode) {
        if (statementDepth < 1) return
        statementBuffer.append(node.text)
        statementBuffer.append(" ")
    }

    override fun visitErrorNode(node: ErrorNode) {}

    override fun enterEveryRule(ctx: ParserRuleContext) {
        if (statementContextClasses.contains(ctx.javaClass)) {
            statementDepth += 1
        }
    }

    override fun exitEveryRule(ctx: ParserRuleContext) {
        if (statementContextClasses.contains(ctx.javaClass)) {
            statementDepth -= 1
            if (statementDepth == 0) {
                statementBuffer.append(";")
                statements.add(statementBuffer.toString())
                statementBuffer.clear()
            }
        }
    }

}