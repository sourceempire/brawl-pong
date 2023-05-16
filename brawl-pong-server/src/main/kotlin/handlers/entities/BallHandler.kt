package io.sourceempire.brawlpong.handlers.entities

import io.sourceempire.brawlpong.models.Match
import io.sourceempire.brawlpong.models.PaddleSide
import io.sourceempire.brawlpong.models.Player

// To adjust the max angle when hitting the paddle's top or bottom
const val PADDLE_COLLISION_OFFSET = 55.0

fun updateBallPosition(match: Match, onScore: (match: Match) -> Unit) {
    val gameState = match.gameState

    // Update the ball's position
    val ball = gameState.ball
    ball.x += ball.dx
    ball.y += ball.dy

    val ballDiameter = ball.radius * 2

    fun handleScore(player: Player) {
        player.score += 1
        gameState.resetBall()

        val winningPlayer = match.players.values.find { it.score == match.scoreToWin }

        if (winningPlayer != null) {
            match.gameState.paused = true
            match.winner = winningPlayer.id
        }

        onScore(match)
    }

    // Check if the ball goes out of bounds on the left or right side
    if (ball.x + ball.radius - ballDiameter > gameState.field.width) {
        // Left paddle player scores
        handleScore(match.players.values.find { it.paddleSide == PaddleSide.Left }!!)
    } else if (ball.x - ball.radius < -ballDiameter) {
        // Right paddle player scores
        handleScore(match.players.values.find { it.paddleSide == PaddleSide.Right }!!)
    }
}

