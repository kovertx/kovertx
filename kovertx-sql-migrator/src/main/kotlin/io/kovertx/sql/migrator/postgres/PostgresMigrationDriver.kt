package io.kovertx.sql.migrator.postgres

import io.kovertx.sql.migrator.MigrationDriverFactory
import io.kovertx.sql.migrator.MigrationStepLog
import io.kovertx.sql.migrator.MigratorOptions
import io.kovertx.sql.migrator.common.SqlMigrationDriver
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.SqlConnection


class PostgresMigrationDriver(
    conn: SqlConnection,
    options: PostgresMigrationOptions
) : SqlMigrationDriver(conn, postgresParser) {

    override val initLogSql = """
        CREATE TABLE IF NOT EXISTS ${options.migrationsTable} (
        log_id BIGSERIAL PRIMARY KEY,
        title TEXT NOT NULL,
        hash TEXT NOT NULL,
        applied TIMESTAMPTZ DEFAULT now()
    );
    """.trimIndent()

    override val selectLogsSql = """
        SELECT log_id, title, hash, applied FROM ${options.migrationsTable}
        ORDER BY log_id
    """.trimIndent()

    override fun mapLogRow(row: Row) = MigrationStepLog(
        logId = row.getLong(0),
        title = row.getString(1),
        hash = row.getString(2),
        applied = row.getOffsetDateTime(3))

    override val insertLogSql = """
        INSERT INTO ${options.migrationsTable}
        (title, hash)
        VALUES ($1, $2);
    """.trimIndent()

    override val updateLogHashSql = """
        UPDATE ${options.migrationsTable}
        SET hash = $2
        WHERE log_id = $1;
    """.trimIndent()
}

class PostgresMigrationDriverFactory : MigrationDriverFactory {
    override fun build(conn: SqlConnection) = PostgresMigrationDriver(conn, PostgresMigrationOptions())
}

fun MigratorOptions.postgresDriver() {
    driverFactory = PostgresMigrationDriverFactory()
}

fun MigratorOptions.postgresDriver(fn: PostgresMigrationDriverFactory.() -> Unit) {
    driverFactory = PostgresMigrationDriverFactory().also(fn)
}
