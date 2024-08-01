# Kovertx SQL Migrator

Migrator provides a simple mechanism for applying SQL migrations using Vertx Sql clients.

## Example

````kotlin

fun demo() {
    buildMigrator {
        sqliteDriver()
        
        sql("""
            CREATE TABLE demo_table (a TEXT, b INT);
        """.trimIndent())
    }
}

````

## Drivers

Drivers define how to apply

## Migration Step Plans

Migrations are broken into sequential steps, where each must be applied in sequence.

Steps are applied in transactions, so we ensure a step is either completely applied or
not applied at all. Most functions for adding steps have two variations - one with a
title and one without.

If a title is provided, the loaded content is assumed to be a complete step. In no-title
variants, we look for header blocks with the regex `^--+\h+migration:\h*(.*)$` and include
content between them.

````kotlin
val migrator = buildMigrator(vertx) {
    // single step with a single statement
    sql("Create a table", "CREATE TABLE foo (bar TEXT);")
    
    // single step with a multiple statements
    sql("Create two tables", """
        CREATE TABLE aard (vark TEXT);
        CREATE TABLE bam (baz TEXT);
    """.trimIndent())

    // single string with mutliple steps
    sql("""
        -- migration: delete 'aard'
        DROP TABLE aard;
        -- migration: delete 'vark'
        DROP TABLE vark;
    """.trimIndent())
    
    // The contents of this file will be loaded as a single migration step
    file("some changes", "migration-001.sql")
    
    // The contents of this file will be split into steps by looking for headers
    file("multi-migration-log.sql")
}
````

The content of migration steps should be plain dialect-dependent SQL. Drivers define how
to parse, normalize, and split this content. Drivers should make a best effort to normalize
content before hashing, so that things like comments and formatting changes don't cause
hash validation failures.

## Migration Step Logs

Logs of applied migrations are generally stored in a table created by the driver being
used. As an example, the Sqlite driver creates a table like:

````sqlite
CREATE TABLE IF NOT EXISTS kovertx_migrations (
    log_index INTEGER PRIMARY KEY AUTOINCREMENT,
    hash VARCHAR(128) NOT NULL,
    applied TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)
````

...the exact details of this table depend on the driver in question. Drivers may have
their own configuration to control how things are laid out.

When a `Migrator` is applied, it will use the driver to query for migrations that have
already been applied.

- For logged (already applied) migrations, it will verify that the stored hash matches the next 
  expected step's hash. If existing 
- For unlogged (not yet applied) migrations, it will attempt to apply the migration step
  and add a log entry.
- If validation or application of migration steps fails, it will back out of the
  transaction leaving the database unchanged.