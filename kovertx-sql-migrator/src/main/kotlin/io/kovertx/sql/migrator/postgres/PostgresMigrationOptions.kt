package io.kovertx.sql.migrator.postgres

data class PostgresMigrationOptions(
    var migrationsTable: String = "kovertx_migrations"
)