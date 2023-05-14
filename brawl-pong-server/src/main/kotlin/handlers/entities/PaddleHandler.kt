package io.sourceempire.brawlpong.handlers.entities

import io.sourceempire.brawlpong.models.Match
import io.sourceempire.brawlpong.models.entities.Paddle


fun updatePaddlePositions(match: Match) {
    val gameState = match.gameState
    // Update player 1's position
    when (gameState.paddle1.renderData.direction) {
        Paddle.Direction.Up -> {
            gameState.paddle1.renderData.y = maxOf(gameState.paddle1.renderData.y - gameState.paddle1.renderData.speed, 0f)
        }
        Paddle.Direction.Down -> {
            gameState.paddle1.renderData.y = minOf(gameState.paddle1.renderData.y + gameState.paddle1.renderData.speed, gameState.field.height - gameState.paddle1.renderData.height)
        }
        Paddle.Direction.Stop -> {
            // Do nothing
        }
    }

    // Update player 2's position
    when (gameState.paddle2.renderData.direction) {
        Paddle.Direction.Up -> {
            gameState.paddle2.renderData.y = maxOf(gameState.paddle2.renderData.y - gameState.paddle2.renderData.speed, 0f)
        }
        Paddle.Direction.Down -> {
            gameState.paddle2.renderData.y = minOf(gameState.paddle2.renderData.y + gameState.paddle2.renderData.speed, gameState.field.height - gameState.paddle2.renderData.height)
        }
        Paddle.Direction.Stop -> {
            // Do nothing
        }
    }
}