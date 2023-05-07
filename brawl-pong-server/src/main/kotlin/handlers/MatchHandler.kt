package handlers

import exceptions.MatchNotFoundException
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.ext.web.handler.sockjs.SockJSSocket
import listeners.ScoreUpdateListener
import models.CreateMatchEvent
import models.GameState
import models.Match
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
    fun registerScoreUpdateListener(listener: ScoreUpdateListener)
}

class MatchHandlerImpl(private val vertx: Vertx) : MatchHandler {
    private val matches = mutableMapOf<UUID, Match>()
    private val scoreUpdateListeners = mutableListOf<ScoreUpdateListener>()

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
        val unauthorizedMatches = getMatchesByAuthorizationStatus(requiresAuthorization = false)
        return unauthorizedMatches[matchId] ?: throw MatchNotFoundException()
    }

    override fun getAuthorizedMatch(matchId: UUID): Match {
        val authorizedMatches = getMatchesByAuthorizationStatus(requiresAuthorization = true)
        return authorizedMatches[matchId] ?: throw MatchNotFoundException()
    }

    override fun getMatchBySocket(sockJSSocket: SockJSSocket): Match {
        return matches.values.find {
            it.gameState.player1.connection == sockJSSocket || it.gameState.player2.connection == sockJSSocket
        } ?: throw MatchNotFoundException()
    }

    override fun getMatchByPlayer2Socket(sockJSSocket: SockJSSocket): Match {
        val unauthorizedMatches = getMatchesByAuthorizationStatus(requiresAuthorization = false)
        return unauthorizedMatches.values.find { !it.gameState.player2.connected } ?: throw MatchNotFoundException()
    }

    override fun removeMatch(matchId: UUID): Match {
        return matches.remove(matchId) ?: throw MatchNotFoundException()
    }

    override fun countdownAndStartMatch(matchId: UUID, duration: Long) {
        val match = getMatchById(matchId)?: return

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

    override fun registerScoreUpdateListener(listener: ScoreUpdateListener) {
        scoreUpdateListeners.add(listener)
    }

    private  fun startMatch(matchId: UUID) {
        val match = getMatchById(matchId)?: return

        // Start the game by enabling movement and updating game state
        match.gameState.startMovement()
        match.dispatchGameState()

        vertx.setPeriodic(1000L / match.tickRate) {
            updateGameState(match)
        }
    }

    private fun notifyScoreUpdate(match: Match, player: UUID) {
        if (match.requiresAuthorization) {
            scoreUpdateListeners.forEach { it.onPlayerScoreUpdate(match, player) }
        }
    }

    private fun updateGameState(match: Match) {
        updatePlayerPositions(match)
        updateBallPosition(match, ::notifyScoreUpdate)
        handlePaddleCollisions(match)
        handleWallCollisions(match)
    }

    private fun getMatchesByAuthorizationStatus(requiresAuthorization: Boolean): Map<UUID, Match> {
        return matches.filter { it.value.requiresAuthorization == requiresAuthorization }
    }


}