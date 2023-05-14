package io.sourceempire.brawlpong.models

import com.fasterxml.jackson.annotation.JsonIgnore
import io.sourceempire.brawlpong.exceptions.PlayerNotInMatchException
import io.sourceempire.brawlpong.models.entities.GameState
import io.sourceempire.brawlpong.models.entities.Paddle
import io.vertx.core.Future
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.Json
import java.util.UUID


data class Match(
    val id: UUID,
    val gameState: GameState,
    val scoreToWin: Int = 2,
    @JsonIgnore
    val requiresAuthorization: Boolean = false,
    ) {

    var winner: UUID? = null

    // TODO -> Move out to some constants file
    val tickRate = 60

    fun dispatchGameState() {
        fun createGameStateMessage(): Buffer {
            val data = mapOf(
                "tickRate" to tickRate,
                "gameState" to gameState,
            )
            val message = mapOf(
                "type" to "game-state",
                "data" to data
            )
            return Buffer.buffer(Json.encode(message))
        }

        gameState.paddle1.connection?.write(createGameStateMessage())
        gameState.paddle2.connection?.write(createGameStateMessage())
    }

    fun dispatchPlayerInfo() {
        fun createPlayerInfoMessage(sessionPlayerId: UUID): Buffer {
            val data = mapOf(
                "player1" to mapOf(
                    "id" to gameState.paddle1.playerId,
                    "isSessionPlayer" to (gameState.paddle1.playerId == sessionPlayerId)
                ),
                "player2" to mapOf(
                    "id" to gameState.paddle2.playerId,
                    "isSessionPlayer" to (gameState.paddle2.playerId == sessionPlayerId)
                ),
            )

            val message = mapOf(
                "type" to "player-info",
                "data" to data
            )

            return Buffer.buffer(Json.encode(message))
        }

        // Send the game state to player 1 with the player id connected to the session
        gameState.paddle1.connection?.write(createPlayerInfoMessage(gameState.paddle1.playerId))

        // Send the game state to player 2 with the player id connected to the session
        gameState.paddle2.connection?.write(createPlayerInfoMessage(gameState.paddle2.playerId))
    }

    fun dispatchMatchCountdown(countdown: Long) {
        fun createCountdownMessage(): Buffer {
            val message = mapOf(
                "type" to "match-countdown",
                "data" to mapOf("countdown" to countdown)
            )
            return Buffer.buffer(Json.encode(message))
        }

        // Send the countdown message to player 1 and player 2
        gameState.paddle1.connection?.write(createCountdownMessage())
        gameState.paddle2.connection?.write(createCountdownMessage())
    }

    fun getPlayerById(playerId: UUID): Paddle {
        return listOf(gameState.paddle1, gameState.paddle2).find { it.playerId == playerId }
            ?: throw PlayerNotInMatchException()
    }

    fun updateWinnerIfExists(): Future<UUID?> {
        val paddle1 = gameState.paddle1
        val paddle2 = gameState.paddle2

        return when {
            paddle1.score == scoreToWin -> {
                gameState.winner = paddle1.playerId
                gameState.paused = true
                Future.succeededFuture(paddle1.playerId)
            }
            paddle2.score == scoreToWin -> {
                gameState.winner = paddle2.playerId
                gameState.paused = true
                Future.succeededFuture(paddle2.playerId)
            }
            else -> {
                Future.succeededFuture(null)
            }
        }
    }
}