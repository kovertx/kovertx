package io.kovertx.sql.migrator

import io.vertx.core.Future
import io.vertx.core.Vertx
import java.time.OffsetDateTime

data class MigrationStepPlan(
    val title: String,
    val content: String,
)

data class MigrationStepLog(
    val logId: Long,
    val title: String,
    val hash: String,
    val applied: OffsetDateTime,
)

typealias MigrationSource = (Vertx) -> Future<List<MigrationStepPlan>>
