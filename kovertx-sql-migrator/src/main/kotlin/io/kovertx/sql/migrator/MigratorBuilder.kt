package io.kovertx.sql.migrator

import io.kovertx.core.Koverticle
import io.vertx.core.Vertx
import io.vertx.sqlclient.Pool


/**
 * define and apply migrator as a warmup step for a Koverticle
 */
fun Koverticle.migrate(vertx: Vertx, pool: Pool, fn: MigratorOptions.() -> Unit) = warmup { ->
    Migrator.build(vertx, fn).migrate(pool).mapEmpty()
}