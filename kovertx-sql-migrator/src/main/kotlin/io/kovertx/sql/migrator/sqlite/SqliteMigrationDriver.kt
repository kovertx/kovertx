package io.kovertx.sql.migrator.sqlite

import io.kovertx.sql.migrator.MigrationDriverFactory
import io.kovertx.sql.migrator.MigrationStepLog
import io.kovertx.sql.migrator.MigratorOptions
import io.kovertx.sql.migrator.common.SqlMigrationDriver
import io.kovertx.sql.migrator.postgres.sqliteParser
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.SqlConnection
import java.time.ZoneOffset

class SqliteMigrationDriver(
    conn: SqlConnection,
    options: SqliteMigrationOptions) : SqlMigrationDriver(conn, sqliteParser) {

    override val initLogSql = """
        CREATE TABLE IF NOT EXISTS ${options.migrationsTable} (
        log_id INTEGER PRIMARY KEY AUTOINCREMENT,
        title TEXT NOT NULL,
        hash TEXT NOT NULL,
        applied TIMESTAMP DEFAULT CURRENT_TIMESTAMP
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
        applied = row.getLocalDateTime(3).atOffset(ZoneOffset.UTC))

    override val insertLogSql = """
        INSERT INTO ${options.migrationsTable}
        (title, hash)
        VALUES (?1, ?2);
    """.trimIndent()

    override val updateLogHashSql = """
        UPDATE ${options.migrationsTable}
        SET hash = ?2
        WHERE log_id = ?1;
    """.trimIndent()
}

class SqliteMigrationDriverFactory(
    val options: SqliteMigrationOptions = SqliteMigrationOptions()
) : MigrationDriverFactory {
    override fun build(conn: SqlConnection) = SqliteMigrationDriver(conn, options)
}

fun MigratorOptions.sqliteDriver() = this.apply {
    driverFactory = SqliteMigrationDriverFactory()
}

fun MigratorOptions.sqliteDriver(fn: SqliteMigrationDriverFactory.() -> Unit) = this.apply {
    driverFactory = SqliteMigrationDriverFactory().also(fn)
}
