package handlers

import models.Match
import models.Player
import java.util.UUID

// To adjust the max angle when hitting the paddle's top or bottom
const val PADDLE_COLLISION_OFFSET = 55.0

fun updateBallPosition(match: Match, onScore: (match: Match, player: UUID) -> Unit) {
    val gameState = match.gameState

    // Update the ball's position
    val ball = gameState.ball
    ball.x += ball.dx
    ball.y += ball.dy

    val ballDiameter = ball.radius * 2

    fun handleScore(player: Player) {
        player.score += 1
        gameState.resetBall()
        match.dispatchGameState()

        player.id?.let { playerId ->
            onScore(match, playerId)
        }
    }

    // Check if the ball goes out of bounds on the left or right side
    if (ball.x + ball.radius - ballDiameter > gameState.field.width) {
        // Player 1 scores
        handleScore(gameState.player2)
    } else if (ball.x - ball.radius < -ballDiameter) {
        // Player 2 scores
        handleScore(gameState.player1)
    }
}