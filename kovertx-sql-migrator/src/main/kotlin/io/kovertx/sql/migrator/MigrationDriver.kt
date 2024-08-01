package io.kovertx.sql.migrator

import io.vertx.core.Future
import io.vertx.sqlclient.SqlConnection

/**
 * Defines how to construct a MigrationDriver
 */
interface MigrationDriverFactory {
    fun build(conn: SqlConnection): MigrationDriver
}

/**
 * Defines all the behavior necessary to initialize, validate, and apply migrations to a connection.
 */
interface MigrationDriver {
    fun initLog(): Future<Void>
    fun getMigrationLogs(): Future<List<MigrationStepLog>>
    fun validateStep(plan: MigrationStepPlan, log: MigrationStepLog): Future<Void>
    fun applyStep(plan: MigrationStepPlan, logOnly: Boolean = false): Future<String>
}
