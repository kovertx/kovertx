import com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable
import io.kovertx.core.AbstractKoverticle
import io.kovertx.konfig.*
import io.vertx.core.Vertx
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

class SimpleConfig : Konfigurable() {
    val foo by konfig {
        file("/run/secrets/foo")
        env("TEST_FOO")
        default("bar")
    }
}

class FailingConfig1 : Konfigurable() {
    val foo by konfig {
        file("/run/secrets/foo")
        env("TEST_FOO")
    }
}

class FailingConfig2 : Konfigurable() {
    val foo by konfig {
        env("TEST_FOO")
        file("/run/secrets/foo")
    }
}

class ConfigVerticle<T : Konfigurable>(config: T) : AbstractKoverticle() {
    val conf by resolveKonfig<T>(config)
}

class KonfigurableTests {
    val vertx = Vertx.vertx()

    @Test
    fun simpleTest() {
        val testContext = VertxTestContext()

        val conf = SimpleConfig()
        conf.resolve(vertx).onComplete(testContext.succeeding {
            assert(conf.foo == "bar")
            testContext.completeNow()
        })

        assert(testContext.awaitCompletion(5, TimeUnit.SECONDS))
        if (testContext.failed()) throw testContext.causeOfFailure()
    }

    @Test
    fun simpleTestVerticle() {
        val testContext = VertxTestContext()

        val verticle = ConfigVerticle(SimpleConfig())
        vertx.deployVerticle(verticle).onComplete(testContext.succeeding {
            assert(verticle.conf.foo == "bar")
            testContext.completeNow()
        })

        assert(testContext.awaitCompletion(5, TimeUnit.SECONDS))
        if (testContext.failed()) throw testContext.causeOfFailure()
    }

    @Test
    fun testFailing1() {
        val testContext = VertxTestContext()

        val conf = FailingConfig1()
        conf.resolve(vertx).onComplete(testContext.failingThenComplete())

        assert(testContext.awaitCompletion(5, TimeUnit.SECONDS))
        if (testContext.failed()) throw testContext.causeOfFailure()
    }

    @Test
    fun testVerticleFailing1() {
        val testContext = VertxTestContext()
        val verticle = ConfigVerticle(FailingConfig1())
        vertx.deployVerticle(verticle).onComplete(testContext.failingThenComplete())
        assert(testContext.awaitCompletion(5, TimeUnit.SECONDS))
        if (testContext.failed()) throw testContext.causeOfFailure()
    }

    @Test
    fun testFailing2() {
        val testContext = VertxTestContext()

        val conf = FailingConfig2()
        conf.resolve(vertx).onComplete(testContext.failingThenComplete())

        assert(testContext.awaitCompletion(5, TimeUnit.SECONDS))
        if (testContext.failed()) throw testContext.causeOfFailure()
    }

    @Test
    fun testVerticleFailing2() {
        val testContext = VertxTestContext()
        val verticle = ConfigVerticle(FailingConfig2())
        vertx.deployVerticle(verticle).onComplete(testContext.failingThenComplete())
        assert(testContext.awaitCompletion(5, TimeUnit.SECONDS))
        if (testContext.failed()) throw testContext.causeOfFailure()
    }
}