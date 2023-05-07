package models

import exceptions.PlayerNotInMatchException
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.Json
import java.util.UUID

data class Match(
    val id: UUID,
    val gameState: GameState,
    val requiresAuthorization: Boolean = false,
    val scoreToWin: Int = 21,
    val winner: UUID? = null,
) {
    val tickRate = 60

    fun dispatchGameState() {
        fun createMessageForPlayer(clientPlayerNumber: Int): Buffer {
            val data = mapOf(
                "tickRate" to tickRate,
                "gameState" to gameState,
                "clientPlayerNumber" to clientPlayerNumber
            )
            val message = mapOf(
                "type" to "game-state",
                "data" to data
            )
            return Buffer.buffer(Json.encode(message))
        }

        // Send the game state to player 1 with clientPlayerNumber set to 1
        gameState.player1.connection?.write(createMessageForPlayer(1))

        // Send the game state to player 2 with clientPlayerNumber set to 2
        gameState.player2.connection?.write(createMessageForPlayer(2))
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
}