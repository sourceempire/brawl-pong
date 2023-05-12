package io.sourceempire.brawlpong.handlers

import io.sourceempire.brawlpong.exceptions.MatchAlreadyFinishedException
import io.sourceempire.brawlpong.exceptions.MatchNotFoundException
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.ext.web.handler.sockjs.SockJSSocket
import io.sourceempire.brawlpong.listeners.MatchEventListener
import io.sourceempire.brawlpong.models.CreateMatchEvent
import io.sourceempire.brawlpong.models.GameState
import io.sourceempire.brawlpong.models.Match
import java.util.*

interface MatchHandler {
    companion object {
        fun create(vertx: Vertx) = MatchHandlerImpl(vertx)
    }

    fun createMatch(): Match
    fun createMatch(createMatchEvent: CreateMatchEvent): Future<Unit>
    fun getMatchById(matchId: UUID): Match
    fun getUnauthorizedMatch(matchId: UUID): Match
    fun getAuthorizedMatch(matchId: UUID): Match
    fun getMatchBySocket(sockJSSocket: SockJSSocket): Match
    fun getMatchByPlayer2Socket(sockJSSocket: SockJSSocket): Match
    fun removeMatch(matchId: UUID): Match
    fun countdownAndStartMatch(matchId: UUID, duration: Long)
    fun registerMatchEventListener(listener: MatchEventListener)

}

class MatchHandlerImpl(private val vertx: Vertx) : MatchHandler {
    private val matches = mutableMapOf<UUID, Match>()
    private val authorizedMatches get() = matches.filter { it.value.requiresAuthorization }
    private val unauthorizedMatches get() = matches.filter { !it.value.requiresAuthorization }

    private val matchEventListeners = mutableListOf<MatchEventListener>()

    override fun createMatch(): Match {
        val matchId = UUID.randomUUID()
        val initialState = GameState.createInitialState()
        matches[matchId] = Match(id = matchId, gameState = initialState)
        return matches[matchId]!!
    }

    override fun createMatch(createMatchEvent: CreateMatchEvent): Future<Unit> {
        val gameState = GameState.createInitialState()
        gameState.player1.id = createMatchEvent.player1Id
        gameState.player2.id = createMatchEvent.player2Id
        val match = Match(createMatchEvent.matchId, gameState, true)

        matches[match.id] = match
        return Future.succeededFuture()
    }

    override fun getMatchById(matchId: UUID): Match {
        return matches[matchId] ?: throw MatchNotFoundException()
    }

    override fun getUnauthorizedMatch(matchId: UUID): Match {
        return unauthorizedMatches[matchId] ?: throw MatchNotFoundException()
    }

    override fun getAuthorizedMatch(matchId: UUID): Match {
        return authorizedMatches[matchId] ?: throw MatchNotFoundException()
    }

    override fun getMatchBySocket(sockJSSocket: SockJSSocket): Match {
        return matches.values.find {
            it.gameState.player1.connection == sockJSSocket || it.gameState.player2.connection == sockJSSocket
        } ?: throw MatchNotFoundException()
    }

    override fun getMatchByPlayer2Socket(sockJSSocket: SockJSSocket): Match {
        return unauthorizedMatches.values.find { !it.gameState.player2.connected } ?: throw MatchNotFoundException()
    }

    override fun removeMatch(matchId: UUID): Match {
        return matches.remove(matchId) ?: throw MatchNotFoundException()
    }

    override fun countdownAndStartMatch(matchId: UUID, duration: Long) {
        val match = getMatchById(matchId)

        var remainingTime = duration

        match.dispatchMatchCountdown(remainingTime)

        vertx.setPeriodic(1000) { timerId ->
            if (remainingTime > 0) {
                remainingTime -= 1
                match.dispatchMatchCountdown(remainingTime)
            } else {
                vertx.cancelTimer(timerId)
                startMatch(matchId)
            }
        }
    }

    override fun registerMatchEventListener(listener: MatchEventListener) {
        matchEventListeners.add(listener)
    }

    private fun startMatch(matchId: UUID) {
        val match = getMatchById(matchId)

        if (match.gameState.winner != null) {
            throw MatchAlreadyFinishedException()
        }

        // Set the paused state to false
        match.gameState.paused = false

        // Start the game by enabling movement and updating game state
        match.dispatchGameState()

        vertx.setPeriodic(1000L / match.tickRate) { timerId ->
            if (match.gameState.paused) {
                // Cancel the timer when the game is paused
                match.dispatchGameState()
                vertx.cancelTimer(timerId)
            } else {
                updateGameState(match)
            }
        }
    }

    private fun unpauseMatch(matchId: UUID) {
        startMatch(matchId)
    }

    private fun pauseMatch(matchId: UUID) {
        val match = getMatchById(matchId)
        match.gameState.paused = true
        match.dispatchGameState()
    }

    private fun onScore(match: Match, player: UUID) {
        invokeEventListenersIfAuthorized(match) {
            it.onPlayerScored(match, player)
        }

        match.checkForWinner {
            invokeEventListenersIfAuthorized(match) {
                it.onMatchEnded(match)
            }
        }
    }


    private fun updateGameState(match: Match) {
        updatePaddlePositions(match)
        updateBallPosition(match, ::onScore)
        handlePaddleCollisions(match)
        handleWallCollisions(match)
    }

    private fun invokeEventListenersIfAuthorized(match: Match, event: (MatchEventListener) -> Unit) {
        if (match.requiresAuthorization) {
            matchEventListeners.forEach(event)
        }
    }
}