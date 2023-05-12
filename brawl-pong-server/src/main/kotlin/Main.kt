import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.sockjs.SockJSHandler

import auth.Auth
import handlers.*
import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.DeploymentOptions
import io.vertx.core.Promise
import io.vertx.core.VertxOptions
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.kotlin.core.deploymentOptionsOf
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager

private const val gameServerPort = 8181
private const val gameServerHost = "localhost"

fun main() {
    val vertx = Vertx.vertx()

    val server = vertx.createHttpServer(HttpServerOptions().setHost(gameServerHost).setPort(gameServerPort))
    val router = Router.router(vertx)
    val sockJSHandler = SockJSHandler.create(vertx)

    val auth = Auth(vertx)
    val matchHandler = MatchHandler.create(vertx)

    router.route("/match/*").handler(StaticHandler.create("static").setIndexPage("index.html"))
    router.route("/game/*").handler(sockJSHandler)

    ClientConnectionHandler.create(sockJSHandler, matchHandler, auth)
    val serverConnectionHandler = ServerConnectionHandler.create(vertx, matchHandler, gameServerHost, gameServerPort)

    matchHandler.registerMatchEventListener(serverConnectionHandler)

    server.requestHandler(router).listen()
}

class Main: AbstractVerticle() {

    override fun start(startPromise: Promise<Void>) {
        // Here you can start HTTP server, set up routes, etc.

        // Remember to complete the start promise when you're done setting up
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
            vertx.deployVerticle("io.sourceempire.brawlgaming.Main", deploymentOptionsOf(instances = 1))
        } else {
            println("Could not start vertx: ${res.cause().message}")
        }
    }
}