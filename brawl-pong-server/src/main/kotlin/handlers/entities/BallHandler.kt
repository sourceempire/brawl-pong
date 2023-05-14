package io.sourceempire.brawlpong.handlers.entities

import io.sourceempire.brawlpong.models.Match
import io.sourceempire.brawlpong.models.entities.Paddle

// To adjust the max angle when hitting the paddle's top or bottom
const val PADDLE_COLLISION_OFFSET = 55.0

fun updateBallPosition(match: Match, onScore: (match: Match) -> Unit) {
    val gameState = match.gameState

    // Update the ball's position
    val ball = gameState.ball
    ball.x += ball.dx
    ball.y += ball.dy

    val ballDiameter = ball.radius * 2

    fun handleScore(paddle: Paddle) {
        paddle.score += 1
        gameState.resetBall()
        match.dispatchGameState()
        onScore(match)
    }

    // Check if the ball goes out of bounds on the left or right side
    if (ball.x + ball.radius - ballDiameter > gameState.field.width) {
        // Player 1 scores
        handleScore(gameState.paddle1)
    } else if (ball.x - ball.radius < -ballDiameter) {
        // Player 2 scores
        handleScore(gameState.paddle2)
    }
}