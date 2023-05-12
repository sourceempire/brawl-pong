package io.sourceempire.brawlpong.models

import com.fasterxml.jackson.annotation.JsonIgnore
import io.sourceempire.brawlpong.exceptions.PlayerNotInMatchException
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.Json
import java.util.UUID

data class Match(
    val id: UUID,
    val gameState: GameState,
    @JsonIgnore
    val requiresAuthorization: Boolean = false,
    val scoreToWin: Int = 2,

) {
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

        gameState.player1.connection?.write(createGameStateMessage())
        gameState.player2.connection?.write(createGameStateMessage())
    }

    fun dispatchPlayerInfo() {
        fun createPlayerInfoMessage(sessionPlayerId: UUID): Buffer {
            val data = mapOf(
                "player1" to mapOf(
                    "id" to gameState.player1.id,
                    "isSessionPlayer" to (gameState.player1.id == sessionPlayerId)
                ),
                "player2" to mapOf(
                    "id" to gameState.player2.id,
                    "isSessionPlayer" to (gameState.player2.id == sessionPlayerId)
                ),
            )

            val message = mapOf(
                "type" to "player-info",
                "data" to data
            )

            return Buffer.buffer(Json.encode(message))
        }

        // Send the game state to player 1 with the player id connected to the session
        gameState.player1.connection?.write(createPlayerInfoMessage(gameState.player1.id))

        // Send the game state to player 2 with the player id connected to the session
        gameState.player2.connection?.write(createPlayerInfoMessage(gameState.player2.id))
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
        gameState.player1.connection?.write(createCountdownMessage())
        gameState.player2.connection?.write(createCountdownMessage())
    }

    fun getPlayerById(playerId: UUID): Player {
        return listOf(gameState.player1, gameState.player2).find { it.id == playerId }
            ?: throw PlayerNotInMatchException()
    }

    fun updateWinnerIfExists(): Boolean {
        val player1 = gameState.player1
        val player2 = gameState.player2

        when {
            player1.score == scoreToWin -> {
                gameState.winner = player1.id
                gameState.paused = true
                return true
            }
            player2.score == scoreToWin -> {
                gameState.winner = player2.id
                gameState.paused = true
                return true
            }
        }

        return false
    }
}