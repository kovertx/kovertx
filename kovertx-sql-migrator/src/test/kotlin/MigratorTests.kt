import io.kovertx.sql.migrator.Migrator
import io.kovertx.sql.migrator.sql
import io.kovertx.sql.migrator.sqlite.sqliteDriver
import io.vertx.core.Vertx
import io.vertx.jdbcclient.JDBCConnectOptions
import io.vertx.jdbcclient.JDBCPool
import io.vertx.junit5.VertxTestContext
import io.vertx.sqlclient.PoolOptions
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit


fun createSqlitePool(vertx: Vertx) = JDBCPool.pool(
    vertx,
    JDBCConnectOptions().setJdbcUrl("jdbc:sqlite::memory:"),
    PoolOptions().setMaxSize(1))


class MigratorTests {
    val vertx = Vertx.vertx()

    @Test
    fun testEmptyMigration() {
        val testContext = VertxTestContext()

        val pool = createSqlitePool(vertx)
        val migrator = Migrator.build(vertx) {
            sqliteDriver()
        }

        migrator.migrate(pool).onComplete(testContext.succeeding { status ->
            testContext.verify {
                assert(status.applied == 0)
                assert(status.validated == 0)
                testContext.completeNow()
            }
        })

        assert(testContext.awaitCompletion(5, TimeUnit.SECONDS))
        if (testContext.failed()) throw testContext.causeOfFailure()
    }

    @Test
    fun testSingleMigration() {
        val testContext = VertxTestContext()

        val pool = createSqlitePool(vertx)
        val migrator = Migrator.build(vertx) {
            sqliteDriver()
            sql("CREATE TABLE test (foo TEXT);")
        }

        migrator.migrate(pool).onComplete(testContext.succeeding { status ->
            testContext.verify {
                assert(status.applied == 1)
                assert(status.validated == 0)
                testContext.completeNow()
            }
        })

        assert(testContext.awaitCompletion(5, TimeUnit.SECONDS))
        if (testContext.failed()) throw testContext.causeOfFailure()
    }

    @Test
    fun testMultiMigration() {
        val testContext = VertxTestContext()

        val pool = createSqlitePool(vertx)
        val migrator = Migrator.build(vertx) {
            sqliteDriver()
            sql("""
                -- migration: create test table
                CREATE TABLE test (foo TEXT);
                -- migration: create test2 table
                CREATE TABLE test2 (bar INT);
            """.trimIndent())
        }

        migrator.migrate(pool).onComplete(testContext.succeeding { status ->
            testContext.verify {
                assert(status.applied == 2)
                assert(status.validated == 0)
                testContext.completeNow()
            }
        })

        assert(testContext.awaitCompletion(5, TimeUnit.SECONDS))
        if (testContext.failed()) throw testContext.causeOfFailure()
    }

    @Test
    fun testDuplicateMigration() {
        val testContext = VertxTestContext()

        val pool = createSqlitePool(vertx)
        val migrator = Migrator.build(vertx) {
            sqliteDriver()
            sql("CREATE TABLE test (foo TEXT);")
        }

        migrator.migrate(pool).onComplete(testContext.succeeding { status ->
            testContext.verify {
                assert(status.applied == 1)
                assert(status.validated == 0)
            }
            migrator.migrate(pool).onComplete(testContext.succeeding { status2 ->
                testContext.verify {
                    assert(status2.applied == 0)
                    assert(status2.validated == 1)
                    testContext.completeNow()
                }
            })
        })

        assert(testContext.awaitCompletion(5, TimeUnit.SECONDS))
        if (testContext.failed()) throw testContext.causeOfFailure()
    }

    @Test
    fun testMigrationFailed() {
        val testContext = VertxTestContext()

        val pool = createSqlitePool(vertx)
        val migrator = Migrator.build(vertx) {
            sqliteDriver()
            sql("CREATE TABLE test (foo TEXT);")
        }

        val migrator2 = Migrator.build(vertx) {
            sqliteDriver()
            sql("CREATE TABLE test2 (foo TEXT);")
        }

        migrator.migrate(pool).onComplete(testContext.succeeding { status ->
            testContext.verify {
                assert(status.applied == 1)
                assert(status.validated == 0)
            }
            migrator2.migrate(pool).onComplete(testContext.failing { t ->
                t.printStackTrace()
                testContext.completeNow()
            })
        })

        assert(testContext.awaitCompletion(5, TimeUnit.SECONDS))
        if (testContext.failed()) throw testContext.causeOfFailure()
    }
}