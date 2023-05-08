import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.sockjs.SockJSHandler

import auth.Auth
import handlers.*
import io.vertx.ext.web.handler.StaticHandler

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