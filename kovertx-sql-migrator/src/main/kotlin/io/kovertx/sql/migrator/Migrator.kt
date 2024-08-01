package io.kovertx.sql.migrator

import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.sqlclient.Pool

data class MigrationStatus(
    val validated: Int = 0,
    val applied: Int = 0,
    val finalHash: String = ""
)

class Migrator(private val vertx: Vertx, options: MigratorOptions) {

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(Migrator::class.java)

        fun build(vertx: Vertx, fn: MigratorOptions.() -> Unit): Migrator {
            return Migrator(vertx, MigratorOptions().also(fn))
        }
    }

    private val skipPlanExecution = options.skipPlanExecution

    private val driverFactory = options.driverFactory ?: throw IllegalArgumentException(
        "Migration Driver not specified")

    private val sources = options.sources.toList()

    private fun initAndRetrieveMigrationLogs(driver: MigrationDriver) =
        driver.initLog().compose { driver.getMigrationLogs() }

    private fun resolveMigrationsSteps(): Future<List<MigrationStepPlan>> =
        Future.all(sources.map { it(vertx) }).compose { composite ->
            Future.succeededFuture(composite
                .list<List<MigrationStepPlan>>()
                .flatMap { it })
        }

    private fun validateOrApplyStep(
        driver: MigrationDriver,
        plan: MigrationStepPlan,
        log: MigrationStepLog?,
        prevStatus: MigrationStatus): Future<MigrationStatus> {

        if (log == null) {
            // no corresponding log, we'll try to apply the plan
            logger.debug("Migration step '${plan.title}' not yet applied, applying now...")
            return driver.applyStep(plan, logOnly = skipPlanExecution).map { stepHash -> prevStatus.copy(
                applied = prevStatus.applied + 1,
                finalHash = stepHash
            ) }
        } else {
            logger.debug("Migration step '${plan.title}' already applied, validating")
            return driver.validateStep(plan, log)
                .map { prevStatus.copy(
                    validated = prevStatus.validated + 1,
                    finalHash = log.hash
                ) }
        }
    }

    fun migrate(pool: Pool): Future<MigrationStatus> {
        return pool.withTransaction { conn ->
            logger.debug("Migration transaction started")
            val driver = driverFactory.build(conn)
            return@withTransaction initAndRetrieveMigrationLogs(driver)
                .compose { logs ->
                    return@compose resolveMigrationsSteps().compose { plans ->

                        if (logs.size > plans.size) {
                            return@compose Future.failedFuture(
                                "Found ${logs.size} migration logs, but only know about " +
                                    "${plans.size} plans")
                        } else {
                            logger.info("Found ${logs.size} existing migration logs")
                        }

                        val root = Future.succeededFuture(MigrationStatus())
                        plans.foldIndexed(root) { idx, prev, plan ->
                            prev.compose { prevStatus ->
                                val log = logs.getOrNull(idx)
                                validateOrApplyStep(driver, plan, log, prevStatus)
                            }
                        }
                    }.onSuccess { status ->
                        logger.info("""
                            Migration Complete
                            - Validated: ${status.validated}
                            - Applied: ${status.applied}
                            - Final step hash: ${status.finalHash}
                        """.trimIndent())
                    }
                }
        }
    }
}