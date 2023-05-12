package io.sourceempire.brawlpong

import io.sourceempire.brawlpong.auth.Auth
import io.sourceempire.brawlpong.handlers.ClientConnectionHandler
import io.sourceempire.brawlpong.handlers.MatchHandler
import io.sourceempire.brawlpong.handlers.ServerConnectionHandler
import io.sourceempire.brawlpong.utils.getClusterManager
import io.sourceempire.brawlpong.utils.getEnvProperty
import io.sourceempire.brawlpong.utils.loadEnv
import io.vertx.core.*
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.kotlin.core.deploymentOptionsOf
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager


fun main() {
    loadEnv()

    if (getEnvProperty("RUN_CLUSTERED") == "true") {
        startClusteredVertx()
    } else {
        startSingleVertx()
    }
}


class Main: AbstractVerticle() {

    override fun start(startPromise: Promise<Void>) {
        loadEnv()

        val clusterManager = vertx.getClusterManager()
        clusterManager?.curatorFramework?.apply {
            println("Is clustered")
        }

        val server = vertx.createHttpServer(HttpServerOptions().setHost(getEnvProperty("THIS_SERVER_URL")).setPort(getEnvProperty("HTTP_PORT").toInt()))
        val router = Router.router(vertx)
        val auth = Auth(vertx)
        val matchHandler = MatchHandler.create(vertx)

        val serverConnectionHandler = ServerConnectionHandler.create(vertx, matchHandler)
        matchHandler.registerMatchEventListener(serverConnectionHandler)

        ClientConnectionHandler(vertx, router, matchHandler, auth)

        router.route("/match/*").handler(StaticHandler.create("static").setIndexPage("index.html"))

        server.requestHandler(router).listen()

        startPromise.complete()
    }

    override fun stop(stopPromise: Promise<Void>) {
        // Here you can clean up resources, if necessary

        // Remember to complete the stop promise when you're done cleaning up
        stopPromise.complete()
    }
}

fun startSingleVertx() {
    val vertxOptions = VertxOptions().setBlockedThreadCheckInterval(999888777666L)

    val vertx = Vertx.vertx(vertxOptions)
    vertx.deployVerticle("io.sourceempire.brawlpong.Main")
}

fun startClusteredVertx() {
    val vertxOptions = VertxOptions().setBlockedThreadCheckInterval(999888777666L)

    val manager = ZookeeperClusterManager()
    vertxOptions.clusterManager = manager
    Vertx.clusteredVertx(vertxOptions) { res: AsyncResult<Vertx> ->
        if (res.succeeded()) {
            val vertx = res.result()
            vertx.deployVerticle("io.sourceempire.brawlpong.Main", deploymentOptionsOf(instances = 1))
        } else {
            println("Could not start vertx: ${res.cause().message}")
        }
    }
}