package io.sourceempire.brawlpong.handlers.actions

import io.sourceempire.brawlpong.exceptions.PlayerNotInMatchException
import io.sourceempire.brawlpong.handlers.MatchHandler
import io.vertx.ext.web.handler.sockjs.SockJSSocket
import io.sourceempire.brawlpong.models.*
import io.sourceempire.brawlpong.models.actions.KeyDownAction
import io.sourceempire.brawlpong.models.actions.KeyUpAction
import io.sourceempire.brawlpong.models.entities.Paddle
import java.util.*

fun handlePlayerReadyEvent(match: Match, sockJSSocket: SockJSSocket, matchHandler: MatchHandler) {
    val player = match.players.values.find { it.connection == sockJSSocket }

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
    val player = getPaddleBySocket(match, sockJSSocket)

    when (event.key) {
        "ArrowUp" -> player?.upKeyPressed = true
        "ArrowDown" -> player?.downKeyPressed = true
    }
}

fun handleKeyUpEvent(match: Match, sockJSSocket: SockJSSocket, event: KeyUpAction) {
    val player = getPaddleBySocket(match, sockJSSocket)

    when (event.key) {
        "ArrowUp" -> player?.upKeyPressed = false
        "ArrowDown" -> player?.downKeyPressed = false
    }
}

private fun getPaddleBySocket(match: Match, sockJSSocket: SockJSSocket): Paddle? {
    val player = match.players.values.find {
        it.connection.hashCode() == sockJSSocket.hashCode()
    }?: throw PlayerNotInMatchException()

    return when (player.paddleSide) {
        PaddleSide.Left -> match.gameState.leftPaddle
        PaddleSide.Right -> match.gameState.rightPaddle
    }
}

private fun areBothPlayersReady(match: Match): Boolean {
    return match.players.values.all { it.ready }
}

private fun markPlayerAsReady(player: Player) {
    player.ready = true
}

private fun startMatchCountdown(matchHandler: MatchHandler, matchId: UUID) {
    matchHandler.countdownAndStartMatch(matchId, 5)
}