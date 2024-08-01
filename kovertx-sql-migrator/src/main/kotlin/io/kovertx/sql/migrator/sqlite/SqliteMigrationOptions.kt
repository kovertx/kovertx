package io.kovertx.sql.migrator.sqlite

data class SqliteMigrationOptions(
    var migrationsTable: String = "kovertx_migrations"
)