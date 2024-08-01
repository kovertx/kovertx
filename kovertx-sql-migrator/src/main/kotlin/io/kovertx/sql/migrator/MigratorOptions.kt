package io.kovertx.sql.migrator

import io.vertx.core.Future
import java.nio.charset.Charset

class MigratorOptions {
    var driverFactory: MigrationDriverFactory? = null
    var logOnly: Boolean = false
    var skipPlanExecution: Boolean
        get() = logOnly
        set(value) { logOnly = value }
    val sources = mutableListOf<MigrationSource>()
}

fun MigratorOptions.sql(title: String, content: String) = this.apply {
    sources.add { Future.succeededFuture(listOf(MigrationStepPlan(title, content))) }
}

fun MigratorOptions.sql(content: String) = this.apply {
    sources.add { Future.succeededFuture(MigrationParser.splitIntoSteps(content)) }
}

fun MigratorOptions.file(title: String, path: String, enc: Charset = Charsets.UTF_8) = this.apply {
    sources.add { vertx -> vertx.fileSystem()
        .readFile(path)
        .map { listOf(MigrationStepPlan(title, it.toString(enc))) }
    }
}

fun MigratorOptions.file(path: String, enc: Charset = Charsets.UTF_8) = this.apply {
    sources.add { vertx -> vertx.fileSystem()
        .readFile(path)
        .map { MigrationParser.splitIntoSteps(it.toString(enc)) }
    }
}