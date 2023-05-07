package handlers

import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.core.net.NetSocket
import listeners.ScoreUpdateListener
import models.CreateMatchEvent
import models.Match
import models.ServerEvent
import java.util.*

interface ServerConnectionHandler: ScoreUpdateListener {
    companion object {
        fun create(
            vertx: Vertx,
            matchHandler: MatchHandler,
            gameServerHost: String,
            gameServerPort: Int,
        ): ServerConnectionHandler = ServerConnectionHandlerImpl(vertx, matchHandler, gameServerHost, gameServerPort)
    }
}

class ServerConnectionHandlerImpl(
    vertx: Vertx,
    private val matchHandler: MatchHandler,
    private val gameServerHost: String,
    private val gameServerPort: Int,
): ServerConnectionHandler {

    private lateinit var socket: NetSocket
    private val pongServerPort = 8182

    init {
        val netServer = vertx.createNetServer()

        netServer.connectHandler(::onConnect)

        netServer.listen(pongServerPort, "localhost")
            .onSuccess {
                println("Pong server listening on port $pongServerPort")
            }.onFailure {
                println("Failed to start Pong server: ${it.message}")
            }
    }

    private fun onConnect(socket: NetSocket) {
        handleConnection(socket)

        socket.handler { buffer -> handleMessage(buffer) }
        socket.closeHandler { handleDisconnect() }
        socket.exceptionHandler { throwable -> handleException(throwable) }
    }

    private fun handleConnection(socket: NetSocket) {
        this.socket = socket
        println("Brawl server connected")
    }

    private fun handleMessage(buffer: Buffer) {
        val jsonMessage = JsonObject(buffer.toString())

        when (val event = ServerEvent.fromJson(jsonMessage)) {
            is CreateMatchEvent -> handleCreateMatchEvent(event)
        }
    }

    private fun handleDisconnect() {
        println("Brawl Server disconnected")
    }

    private fun handleException (throwable: Throwable) {
        println("Error in TCP server: ${throwable.message}")
    }

    private fun handleCreateMatchEvent(event: CreateMatchEvent) {
        matchHandler.createMatch(event)
            .onSuccess {
                this.socket.write(Json.encode(mapOf(
                    "type" to "match-created",
                    "serverAddress" to "$gameServerHost:$gameServerPort/match/",
                    "matchId" to event.matchId,
                    "player1Id" to event.player1Id,
                    "player2Id" to event.player2Id
                )))
            }
    }

    override fun onPlayerScoreUpdate(match: Match, player: UUID) {
        println("Match ${match.id} - Player 1: ${match.gameState.player1.score}, Player 2: ${match.gameState.player2.score}")

        this.socket.write(Json.encode(mapOf(
            "type" to "player-scored",
            "matchId" to match.id,
            "player" to player,
            "score" to mapOf(
                match.gameState.player1.id to match.gameState.player1.score,
                match.gameState.player2.id to match.gameState.player2.score
            )
        )))
    }
}