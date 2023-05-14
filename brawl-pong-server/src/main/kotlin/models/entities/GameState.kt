package io.sourceempire.brawlpong.models.entities

import java.util.*

// TODO -> Should only include paddles, ball, field, and paused.


data class GameState(
    val paddle1: Paddle,
    val paddle2: Paddle,
    val ball: Ball,
    val field: Field,
    var paused: Boolean = true,
    var winner: UUID? = null,
) {
    companion object {
        fun createInitialState(): GameState {
            val fieldWidth = 800f
            val fieldHeight = 600f

            // Set the x position of player1 to be 20 pixels from the left side of the field
            val player1X = 20f

            // Set the x position of player2 to be 20 pixels from the right side of the field,
            // accounting for the paddle width (20 pixels)
            val player2X = fieldWidth - 40f

            // Set the initial y position of both players to be in the middle of the field,
            // accounting for half of the paddle height (50 pixels)
            val playerY = fieldHeight / 2 - 50f

            // Create the player1 object with its x and y positions
            val paddle1 = Paddle(
                x = player1X,
                y = playerY,
                speed = 5f,
            )

            // Create the player2 object with its x and y positions
            val paddle2 = Paddle(
                x = player2X,
                y = playerY,
                speed = 5f
            )

            // Set the ball's initial x and y position to be in the middle of the field
            val ballX = fieldWidth / 2f
            val ballY = fieldHeight / 2f

            // Create the ball object with its x and y positions and initial velocities (dx, dy)
            val ball = Ball(
                x = ballX,
                y = ballY,
                speed = 3f,
            )

            // Create the field object with its width and height
            val field = Field(
                width = fieldWidth,
                height = fieldHeight
            )

            // Return the GameState object with the created player1, player2, ball, and field objects
            return GameState(paddle1, paddle2, ball, field)
        }
    }


    fun resetBall() {
        ball.x = field.width / 2f
        ball.y = field.height / 2f
        ball.setRandomDirection()
        ball.setSpeed(3f)
        ball.touchedPaddle = false
    }
}
