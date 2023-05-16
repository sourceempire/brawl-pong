package io.sourceempire.brawlpong.handlers

import io.vertx.core.json.JsonObject
import io.vertx.ext.web.handler.sockjs.SockJSHandler
import io.vertx.ext.web.handler.sockjs.SockJSSocket

import io.sourceempire.brawlpong.auth.Auth
import io.sourceempire.brawlpong.exceptions.MatchNotFoundException
import io.netty.handler.codec.http.QueryStringDecoder
import io.sourceempire.brawlpong.handlers.actions.handleKeyDownEvent
import io.sourceempire.brawlpong.handlers.actions.handleKeyUpEvent
import io.sourceempire.brawlpong.handlers.actions.handlePlayerReadyEvent
import io.sourceempire.brawlpong.models.PaddleSide
import io.sourceempire.brawlpong.models.Player
import io.sourceempire.brawlpong.models.actions.ClientAction
import io.sourceempire.brawlpong.models.actions.KeyDownAction
import io.sourceempire.brawlpong.models.actions.KeyUpAction
import io.sourceempire.brawlpong.models.actions.ReadyAction
import io.vertx.core.CompositeFuture
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions

class ClientConnectionHandler(
    vertx: Vertx,
    router: Router,
    private val matchHandler: MatchHandler,
    private val auth: Auth
) {
    init {
        val sockJSOptions = SockJSHandlerOptions().setHeartbeatInterval(2000)
        val sockJSHandler = SockJSHandler.create(vertx, sockJSOptions)

        router.route("/game/*").subRouter(sockJSHandler.socketHandler { sockJSSocket ->
            sockJSSocket.handler(sockJSSocket::write)

            handleClientConnection(sockJSSocket)

            sockJSSocket.handler { buffer ->
                handleMessage(sockJSSocket, buffer)
            }

            sockJSSocket.endHandler {
                handleClientDisconnect(sockJSSocket)
            }
        })
    }

    private fun handleClientConnection(sockJSSocket: SockJSSocket) {
        val token = getToken(sockJSSocket)

        if (token != null) {
            handleAuthorizedConnection(sockJSSocket, token)
        } else {
            handleUnauthorizedConnection(sockJSSocket)
        }.onFailure {
            it.printStackTrace()
        }
    }

    private fun handleAuthorizedConnection(sockJSSocket: SockJSSocket, token: String): Future<Unit> {
        return auth.validateBrawlToken(token)
            .compose { authInfo ->
                try {
                    matchHandler.addPlayerConnection(authInfo.matchId, authInfo.playerId, sockJSSocket)
                } catch (error: Throwable) {
                    Future.failedFuture(error)
                }
            }.onFailure {
                it.printStackTrace()
            }
    }

    private fun handleUnauthorizedConnection(sockJSSocket: SockJSSocket): Future<Unit> {
        return try {
            val availableMatch = matchHandler.getMatchWithEmptySlot()
            val player = Player(PaddleSide.Right)

            matchHandler.addPlayer(availableMatch.id, player).compose {
                matchHandler.addPlayerConnection(availableMatch.id, player.id, sockJSSocket)
            }

        } catch (error: Throwable) {
            if (error is MatchNotFoundException) {
                val match = matchHandler.createMatch()
                val player = Player(PaddleSide.Left)

                matchHandler.addPlayer(match.id, player).compose {
                    matchHandler.addPlayerConnection(match.id, player.id, sockJSSocket)
                }
            } else {
                Future.failedFuture(error)
            }
        }
    }

    private fun handleMessage(sockJSSocket: SockJSSocket, buffer: Buffer) {
        val match = matchHandler.getMatchBySocket(sockJSSocket)

        val jsonMessage = JsonObject(buffer.toString())

        try {
            when (val event = ClientAction.fromJson(jsonMessage)) {
                is ReadyAction -> handlePlayerReadyEvent(match, sockJSSocket, matchHandler)
                is KeyDownAction -> handleKeyDownEvent(match, sockJSSocket, event)
                is KeyUpAction -> handleKeyUpEvent(match, sockJSSocket, event)
            }
        } catch (error: IllegalArgumentException) {
            error.printStackTrace()
        }

        match.dispatchGameState()
    }

    private fun handleClientDisconnect(sockJSSocket: SockJSSocket) {
        // Find and remove the game session this player was in if a match exists
        val match = matchHandler.getMatchBySocket(sockJSSocket)

        matchHandler.removeMatch(match.id)

        // TODO -> Handle the match being removed by sending something:
        // val player1Connection = match.gameState.player1.connection
        // val player2Connection = match.gameState.player2.connection
    }

    private fun getToken(sockJSSocket: SockJSSocket): String? {
        val uri = sockJSSocket.uri()
        val queryParams = QueryStringDecoder(uri).parameters()
        return queryParams["token"]?.get(0)
    }
}