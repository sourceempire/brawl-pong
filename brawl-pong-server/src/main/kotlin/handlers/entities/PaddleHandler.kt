package io.sourceempire.brawlpong.handlers.entities

import io.sourceempire.brawlpong.models.Match
import io.sourceempire.brawlpong.models.entities.Paddle


fun updatePaddlePositions(match: Match) {
    val gameState = match.gameState
    // Update player 1's position
    when (gameState.leftPaddle.direction) {
        Paddle.Direction.Up -> {
            gameState.leftPaddle.y = maxOf(gameState.leftPaddle.y - gameState.leftPaddle.speed, 0f)
        }
        Paddle.Direction.Down -> {
            gameState.leftPaddle.y = minOf(gameState.leftPaddle.y + gameState.leftPaddle.speed, gameState.field.height - gameState.leftPaddle.height)
        }
        Paddle.Direction.Stop -> {
            // Do nothing
        }
    }

    // Update player 2's position
    when (gameState.rightPaddle.direction) {
        Paddle.Direction.Up -> {
            gameState.rightPaddle.y = maxOf(gameState.rightPaddle.y - gameState.rightPaddle.speed, 0f)
        }
        Paddle.Direction.Down -> {
            gameState.rightPaddle.y = minOf(gameState.rightPaddle.y + gameState.rightPaddle.speed, gameState.field.height - gameState.rightPaddle.height)
        }
        Paddle.Direction.Stop -> {
            // Do nothing
        }
    }
}