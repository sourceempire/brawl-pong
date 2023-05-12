package io.sourceempire.brawlpong.handlers

import io.vertx.ext.web.handler.sockjs.SockJSSocket
import io.sourceempire.brawlpong.models.*
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

fun handleKeyDownEvent(match: Match, sockJSSocket: SockJSSocket, event: KeyDownEvent) {
    val player = getPlayerBySocket(match, sockJSSocket)

    when (event.key) {
        "ArrowUp" -> player?.upKeyPressed = true
        "ArrowDown" -> player?.downKeyPressed = true
    }
}

fun handleKeyUpEvent(match: Match, sockJSSocket: SockJSSocket, event: KeyUpEvent) {
    val player = getPlayerBySocket(match, sockJSSocket)

    when (event.key) {
        "ArrowUp" -> player?.upKeyPressed = false
        "ArrowDown" -> player?.downKeyPressed = false
    }
}

private fun getPlayerBySocket(match: Match, sockJSSocket: SockJSSocket): Player? {
    return when (sockJSSocket) {
        match.gameState.player1.connection -> match.gameState.player1
        match.gameState.player2.connection -> match.gameState.player2
        else -> null
    }
}

private fun areBothPlayersReady(match: Match): Boolean {
    return match.gameState.player1.ready && match.gameState.player2.ready
}

private fun markPlayerAsReady(player: Player) {
    player.ready = true
}

private fun startMatchCountdown(matchHandler: MatchHandler, matchId: UUID) {
    matchHandler.countdownAndStartMatch(matchId, 5)
}