package handlers

import io.vertx.core.json.JsonObject
import io.vertx.ext.web.handler.sockjs.SockJSHandler
import io.vertx.ext.web.handler.sockjs.SockJSSocket

import auth.Auth
import exceptions.MatchNotFoundException
import io.netty.handler.codec.http.QueryStringDecoder
import io.vertx.core.Future
import io.vertx.core.buffer.Buffer
import models.*

interface ClientConnectionHandler {
    companion object {
        fun create(
            sockJSHandler: SockJSHandler,
            matchHandler: MatchHandler,
            auth: Auth
        ): ClientConnectionHandler = ClientConnectionHandlerImpl(sockJSHandler, matchHandler, auth)
    }
}

private class ClientConnectionHandlerImpl(
    sockJSHandler: SockJSHandler,
    private val matchHandler: MatchHandler,
    private val auth: Auth
): ClientConnectionHandler {

    init {
        sockJSHandler.socketHandler { sockJSSocket ->
            handleClientConnection(sockJSSocket)

            sockJSSocket.handler { buffer ->
                handleMessage(sockJSSocket, buffer)
            }

            sockJSSocket.endHandler {
                handleClientDisconnect(sockJSSocket)
            }
        }
    }

    private fun handleClientConnection(sockJSSocket: SockJSSocket) {
        val token = getToken(sockJSSocket)

        if (token != null) {
            handleAuthorizedConnection(sockJSSocket, token)
        } else {
            handleUnauthorizedConnection(sockJSSocket)
        }.onSuccess {
            matchHandler.getMatchBySocket(sockJSSocket).dispatchPlayerInfo()
        }
    }

    private fun handleAuthorizedConnection(sockJSSocket: SockJSSocket, token: String): Future<Unit> {
        return auth.validateBrawlToken(token)
            .compose { authInfo ->
                try {
                    val match = matchHandler.getAuthorizedMatch(authInfo.matchId)
                    match.getPlayerById(authInfo.playerId).connection = sockJSSocket
                    match.dispatchGameState()
                    Future.succeededFuture<Unit>()
                } catch (error: Throwable) {
                    Future.failedFuture(error)
                }
            }.onFailure {
                it.printStackTrace()
            }
    }

    private fun handleUnauthorizedConnection(sockJSSocket: SockJSSocket): Future<Unit> {
        // Check if there's an available GameSession with an empty player2 slot
        return try {
            val availableMatch = matchHandler.getMatchByPlayer2Socket(sockJSSocket)

            // If an available GameSession is found, set the connecting client as player2
            availableMatch.gameState.player2.connection = sockJSSocket
            availableMatch.dispatchGameState()

            Future.succeededFuture()
        } catch (error: Throwable) {
            if (error is MatchNotFoundException) {
                // If there's no available GameSession, create a new one and set the connecting client as player1
                val match = matchHandler.createMatch()
                match.gameState.player1.connection = sockJSSocket
                match.dispatchGameState()

                Future.succeededFuture()
            } else {
                Future.failedFuture(error)
            }
        }
    }

    private fun handleMessage(sockJSSocket: SockJSSocket, buffer: Buffer) {
        val match = matchHandler.getMatchBySocket(sockJSSocket)

        val jsonMessage = JsonObject(buffer.toString())

        try {
            when (val event = ClientEvent.fromJson(jsonMessage)) {
                is ReadyEvent -> handlePlayerReadyEvent(match, sockJSSocket, matchHandler)
                is KeyDownEvent -> handleKeyDownEvent(match, sockJSSocket, event)
                is KeyUpEvent -> handleKeyUpEvent(match, sockJSSocket, event)
            }
        } catch (error: IllegalArgumentException) {
            error.printStackTrace()
        }

        match.dispatchGameState()
    }

    private fun handleClientDisconnect(sockJSSocket: SockJSSocket) {
        // Find and remove the game session this player was in if a match exists
        val match = matchHandler.getMatchBySocket(sockJSSocket)?: return

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