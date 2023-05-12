package io.sourceempire.brawlpong.handlers

import io.sourceempire.brawlpong.models.PlayerDirection
import io.sourceempire.brawlpong.models.Match


fun updatePaddlePositions(match: Match) {
    val gameState = match.gameState
    // Update player 1's position
    when (gameState.player1.renderData.direction) {
        PlayerDirection.Up -> {
            gameState.player1.renderData.y = maxOf(gameState.player1.renderData.y - gameState.player1.renderData.speed, 0f)
        }
        PlayerDirection.Down -> {
            gameState.player1.renderData.y = minOf(gameState.player1.renderData.y + gameState.player1.renderData.speed, gameState.field.height - gameState.player1.renderData.height)
        }
        PlayerDirection.Stop -> {
            // Do nothing
        }
    }

    // Update player 2's position
    when (gameState.player2.renderData.direction) {
        PlayerDirection.Up -> {
            gameState.player2.renderData.y = maxOf(gameState.player2.renderData.y - gameState.player2.renderData.speed, 0f)
        }
        PlayerDirection.Down -> {
            gameState.player2.renderData.y = minOf(gameState.player2.renderData.y + gameState.player2.renderData.speed, gameState.field.height - gameState.player2.renderData.height)
        }
        PlayerDirection.Stop -> {
            // Do nothing
        }
    }
}