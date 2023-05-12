package io.sourceempire.brawlpong.handlers

import io.sourceempire.brawlpong.models.Match
import kotlin.math.cos
import kotlin.math.sin

fun handlePaddleCollisions(match: Match) {
    val gameState = match.gameState
    val ball = gameState.ball

    // Check for collisions with the paddles
    listOf(gameState.player1, gameState.player2).forEach { paddle ->
        if (ball.x + ball.radius >= paddle.renderData.x && ball.x - ball.radius <= paddle.renderData.x + paddle.renderData.width
            && ball.y + ball.radius >= paddle.renderData.y && ball.y - ball.radius <= paddle.renderData.y + paddle.renderData.height
        ) {
            // If the ball hasn't touched a paddle yet, give it a new speed
            if (!ball.touchedPaddle) {
                ball.setSpeed(10f)
                ball.touchedPaddle = true
            }

            // Calculate the relative hit position on the paddle (0: top, 1: bottom)
            val relativeHitPosition = ((ball.y - paddle.renderData.y) / paddle.renderData.height).coerceIn(0f, 1f)

            // Calculate the bounce angle based on the relative hit position
            // Ensure the angle is between -paddleCollisionOffset and paddleCollisionOffset
            val bounceAngle = Math.toRadians(((2 * relativeHitPosition - 1) * PADDLE_COLLISION_OFFSET))

            // To preserve the speed when changing direction since speed is calculated from dx and dy
            val previousSpeed = ball.speed

            // Set the ball's new direction
            ball.dy = ball.speed * sin(bounceAngle).toFloat()
            ball.dx = if (paddle == gameState.player1) {
                ball.speed * cos(bounceAngle).toFloat()
            } else {
                -ball.speed * cos(bounceAngle).toFloat()
            }

            ball.setSpeed(previousSpeed)
            match.dispatchGameState()
        }
    }
}

fun handleWallCollisions(match: Match) {
    val gameState = match.gameState
    val ball = gameState.ball
    // Check for collisions with the top and bottom of the field
    if (ball.y - ball.radius <= 0) {
        ball.dy = -ball.dy
        ball.y = ball.radius // Make sure the ball doesn't go past the top wall
        match.dispatchGameState()
    } else if (ball.y + ball.radius >= gameState.field.height) {
        ball.dy = -ball.dy
        ball.y = gameState.field.height - ball.radius // Make sure the ball doesn't go past the bottom wall
        match.dispatchGameState()
    }
}