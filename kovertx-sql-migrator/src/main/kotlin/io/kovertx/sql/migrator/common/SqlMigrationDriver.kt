package io.kovertx.sql.migrator.common

import io.kovertx.sql.migrator.MigrationDriver
import io.kovertx.sql.migrator.MigrationStepLog
import io.kovertx.sql.migrator.MigrationStepPlan
import io.vertx.core.Future
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.SqlConnection
import io.vertx.sqlclient.Tuple

abstract class SqlMigrationDriver(
    private val conn: SqlConnection,
    private val parser: SqlVariantParser
) : MigrationDriver {

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(SqlMigrationDriver::class.java)
    }

    protected abstract val initLogSql: String
    protected abstract val selectLogsSql: String
    protected abstract fun mapLogRow(row: Row): MigrationStepLog
    protected abstract val insertLogSql: String
    protected abstract val updateLogHashSql: String

    override fun initLog(): Future<Void> = conn
        .preparedQuery(initLogSql)
        .execute()
        .mapEmpty()

    override fun getMigrationLogs(): Future<List<MigrationStepLog>> = conn
        .preparedQuery(selectLogsSql)
        .mapping(this::mapLogRow)
        .execute()
        .map { it.toList() }

    override fun validateStep(plan: MigrationStepPlan, log: MigrationStepLog): Future<Void> {
        val (expectedHash, preferredHash) = parser.hash(plan.content, log.hash)
        if (expectedHash != log.hash) {
            return Future.failedFuture("Migration step hash mismatch. " +
                "${log.hash} (actual) != ${expectedHash} (expected)")
        }

        if (expectedHash != preferredHash) {
            // Hash is valid, but there's a newer hash variant - upgrade to it
            logger.debug("Step '${plan.title}' has outdated hash variant, upgrading hash " +
                "${expectedHash} -> ${preferredHash}")
            return conn.preparedQuery(updateLogHashSql)
                .execute(mapLogUpdateData(log, preferredHash))
                .mapEmpty()
        } else {
            return Future.succeededFuture()
        }
    }

    protected open fun mapLogUpdateData(oldLog: MigrationStepLog, preferredHash: String) =
        Tuple.of(oldLog.logId, preferredHash)

    override fun applyStep(plan: MigrationStepPlan, logOnly: Boolean): Future<String> {
        val statements = parser.split(plan.content)
        val hashResult = parser.hash(plan.content)

        logger.debug("Logging step '${plan.title}'")
        val insertLog = conn.preparedQuery(insertLogSql)
            .execute(mapLogInsertData(plan, hashResult.preferHash))

        if (logOnly) return insertLog.map(hashResult.preferHash)

        return statements.foldIndexed(insertLog) { idx, prev, stmt ->
            prev.compose {
                logger.debug("Applying step statement #${idx}")
                conn.preparedQuery(stmt).execute()
            }
        }.map(hashResult.preferHash)
    }

    protected open fun mapLogInsertData(plan: MigrationStepPlan, preferredHash: String) =
        Tuple.of(plan.title, preferredHash)
}