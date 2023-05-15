package io.sourceempire.brawlpong.models

import com.fasterxml.jackson.annotation.JsonIgnore
import io.sourceempire.brawlpong.exceptions.PlayerPaddleSideNotSet
import io.sourceempire.brawlpong.exceptions.PlayerNotInMatchException
import io.sourceempire.brawlpong.models.entities.GameState
import io.sourceempire.brawlpong.utils.TICK_RATE
import io.vertx.core.Future
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.Json
import java.util.UUID

data class Match(
    val id: UUID,
    val gameState: GameState,
    val scoreToWin: Int = 2,
    @JsonIgnore
    val requiresAuthorization: Boolean = false
) {
    val players: MutableMap<UUID, Player> = HashMap()

    var winner: UUID? = null

    fun dispatchGameState() {
        fun createGameStateMessage(): Buffer {
            val data = mapOf(
                "tickRate" to TICK_RATE,
                "gameState" to gameState,
                "winner" to winner
            )

            val message = mapOf(
                "type" to "game-state",
                "data" to data
            )
            return Buffer.buffer(Json.encode(message))
        }

        players.values.forEach { it.connection?.write(createGameStateMessage()) }
    }

    fun dispatchStats() {
        fun createGameStatsMessage(): Buffer {
            return Buffer.buffer(
                Json.encode(
                    mapOf(
                        "type" to "stats",
                        "data" to mapOf(
                            "winner" to winner,
                            "score" to mapOf(
                                "leftPaddle" to players.values.find { it.paddleSide == PaddleSide.Left }!!.score,
                                "rightPaddle" to players.values.find { it.paddleSide == PaddleSide.Right }!!.score
                            ),
                        )
                    )
                )
            )
        }

        players.values.forEach { it.connection!!.write(createGameStatsMessage()) }
    }

    fun dispatchPlayerInfo() {
        fun createPlayerInfoMessage(player: Player): Buffer {
            val leftPaddlePlayer = players.values.find { it.paddleSide == PaddleSide.Left }!!
            val rightPaddlePlayer = players.values.find { it.paddleSide == PaddleSide.Right }

            val data = mapOf(
                "leftPaddle" to mapOf(
                    "id" to leftPaddlePlayer.id,
                    "ready" to leftPaddlePlayer.ready,
                    "connected" to leftPaddlePlayer.connected,
                    "isSessionPlayer" to (player.paddleSide == PaddleSide.Left)
                ),
                "rightPaddle" to if (rightPaddlePlayer != null) mapOf(
                    "id" to rightPaddlePlayer.id,
                    "ready" to rightPaddlePlayer.ready,
                    "connected" to rightPaddlePlayer.connected,
                    "isSessionPlayer" to (player.paddleSide == PaddleSide.Right)
                ) else null,
            )

            val message = mapOf(
                "type" to "player-info",
                "data" to data
            )

            return Buffer.buffer(Json.encode(message))
        }

        val leftPaddlePlayer = players.values.find { it.paddleSide == PaddleSide.Left }
        val rightPaddlePlayer = players.values.find { it.paddleSide == PaddleSide.Right }

        // Send the game state to player 1 with the player id connected to the session
        leftPaddlePlayer?.connection?.write(createPlayerInfoMessage(leftPaddlePlayer))
        // Send the game state to player 2 with the player id connected to the session
        rightPaddlePlayer?.connection?.write(createPlayerInfoMessage(rightPaddlePlayer))
    }

    fun dispatchMatchCountdown(countdown: Long) {
        fun createCountdownMessage(): Buffer {
            val message = mapOf(
                "type" to "match-countdown",
                "data" to mapOf("countdown" to countdown)
            )
            return Buffer.buffer(Json.encode(message))
        }

        players.values.forEach { it.connection?.write(createCountdownMessage()) }
    }

    fun getPlayerById(playerId: UUID): Player {
        return players[playerId]?: throw PlayerNotInMatchException()
    }

    fun updateWinnerIfExists(): Future<UUID?> {
        val leftPlayer = players.values.find { it.paddleSide == PaddleSide.Left }?: throw PlayerPaddleSideNotSet(PaddleSide.Left)
        val rightPlayer = players.values.find { it.paddleSide == PaddleSide.Right }?: throw PlayerPaddleSideNotSet(PaddleSide.Right)

        return when {
            leftPlayer.score == scoreToWin -> {
                winner = leftPlayer.id
                dispatchStats()
                gameState.paused = true
                dispatchGameState()
                Future.succeededFuture(leftPlayer.id)
            }
            rightPlayer.score == scoreToWin -> {
                winner = rightPlayer.id
                dispatchStats()
                gameState.paused = true
                dispatchGameState()
                Future.succeededFuture(rightPlayer.id)
            }
            else -> {
                Future.succeededFuture(null)
            }
        }
    }
}