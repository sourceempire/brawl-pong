package io.sourceempire.brawlpong.handlers.actions

import io.sourceempire.brawlpong.handlers.MatchHandler
import io.vertx.ext.web.handler.sockjs.SockJSSocket
import io.sourceempire.brawlpong.models.*
import io.sourceempire.brawlpong.models.actions.KeyDownAction
import io.sourceempire.brawlpong.models.actions.KeyUpAction
import io.sourceempire.brawlpong.models.entities.Paddle
import java.util.*

fun handlePlayerReadyEvent(match: Match, sockJSSocket: SockJSSocket, matchHandler: MatchHandler) {
    val player = getPlayerBySocket(match, sockJSSocket)

    if (player == null || areBothPlayersReady(match)) {
        return
    }
    markPlayerAsReady(player)
    match.dispatchGameState()

    if (areBothPlayersReady(match)) {
        startMatchCountdown(matchHandler, match.id)
    }
}

fun handleKeyDownEvent(match: Match, sockJSSocket: SockJSSocket, event: KeyDownAction) {
    val player = getPlayerBySocket(match, sockJSSocket)

    when (event.key) {
        "ArrowUp" -> player?.upKeyPressed = true
        "ArrowDown" -> player?.downKeyPressed = true
    }
}

fun handleKeyUpEvent(match: Match, sockJSSocket: SockJSSocket, event: KeyUpAction) {
    val player = getPlayerBySocket(match, sockJSSocket)

    when (event.key) {
        "ArrowUp" -> player?.upKeyPressed = false
        "ArrowDown" -> player?.downKeyPressed = false
    }
}

private fun getPlayerBySocket(match: Match, sockJSSocket: SockJSSocket): Paddle? {
    return when (sockJSSocket) {
        match.gameState.paddle1.connection -> match.gameState.paddle1
        match.gameState.paddle2.connection -> match.gameState.paddle2
        else -> null
    }
}

private fun areBothPlayersReady(match: Match): Boolean {
    return match.gameState.paddle1.ready && match.gameState.paddle2.ready
}

private fun markPlayerAsReady(paddle: Paddle) {
    paddle.ready = true
}

private fun startMatchCountdown(matchHandler: MatchHandler, matchId: UUID) {
    matchHandler.countdownAndStartMatch(matchId, 5)
}