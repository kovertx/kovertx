package io.kovertx.config.impl.spi

import io.vertx.config.spi.ConfigStore
import io.vertx.config.spi.ConfigStoreFactory
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject

class KovertxFileConfigStoreFactory : ConfigStoreFactory {
    override fun name() = "kovertx-file"

    override fun create(vertx: Vertx, configuration: JsonObject): ConfigStore =
        SilentFileConfigStore(vertx, configuration)
}

/**
 * An alternative store based on FileConfigStore, that silences errors retrieving the file (e.g.
 * file not found)
 *
 * @see io.vertx.config.impl.spi.FileConfigStore
 */
class SilentFileConfigStore(private val vertx: Vertx, configuration: JsonObject) : ConfigStore {

    private val path =
        configuration.getString("path")
            ?: throw IllegalArgumentException("The `path` configuration is required.")

    override fun get() =
        vertx.fileSystem().readFile(path).recover { Future.succeededFuture(Buffer.buffer("{}")) }
}
