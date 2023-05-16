package io.sourceempire.brawlpong.handlers

import io.sourceempire.brawlpong.exceptions.MatchAlreadyFinishedException
import io.sourceempire.brawlpong.exceptions.MatchFullException
import io.sourceempire.brawlpong.exceptions.MatchNotFoundException
import io.sourceempire.brawlpong.exceptions.PlayerNotInMatchException
import io.sourceempire.brawlpong.handlers.entities.handlePaddleCollisions
import io.sourceempire.brawlpong.handlers.entities.handleWallCollisions
import io.sourceempire.brawlpong.handlers.entities.updateBallPosition
import io.sourceempire.brawlpong.handlers.entities.updatePaddlePositions
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.ext.web.handler.sockjs.SockJSSocket
import io.sourceempire.brawlpong.listeners.MatchEventListener
import io.sourceempire.brawlpong.models.CreateMatchRequest
import io.sourceempire.brawlpong.models.entities.GameState
import io.sourceempire.brawlpong.models.Match
import io.sourceempire.brawlpong.models.PaddleSide
import io.sourceempire.brawlpong.models.Player
import io.sourceempire.brawlpong.models.ClientAction
import io.sourceempire.brawlpong.models.KeyDownAction
import io.sourceempire.brawlpong.models.KeyUpAction
import io.sourceempire.brawlpong.models.ReadyAction
import io.sourceempire.brawlpong.models.entities.Paddle
import io.sourceempire.brawlpong.repos.MatchStatsRepo
import io.sourceempire.brawlpong.utils.TICK_RATE
import io.vertx.core.CompositeFuture
import java.util.*

interface MatchHandler {
    companion object {
        fun create(vertx: Vertx, matchStatsRepo: MatchStatsRepo) = MatchHandlerImpl(vertx, matchStatsRepo)
    }

    fun createMatch(): Match
    fun createMatch(createMatchRequest: CreateMatchRequest): Future<Unit>
    fun getMatchById(matchId: UUID): Match
    fun getUnauthorizedMatch(matchId: UUID): Match
    fun getAuthorizedMatch(matchId: UUID): Match
    fun getMatchBySocket(sockJSSocket: SockJSSocket): Match
    fun getMatchWithEmptySlot(): Match
    fun removeMatch(matchId: UUID): Match
    fun countdownAndStartMatch(matchId: UUID, duration: Long)
    fun registerMatchEventListener(listener: MatchEventListener)

    fun addPlayer(matchId: UUID, player: Player): Future<Unit>
    fun addPlayerConnection(matchId: UUID, playerId: UUID, sockJSSocket: SockJSSocket): Future<Unit>

    fun handleAction(action: ClientAction, sockJSSocket: SockJSSocket)
}

class MatchHandlerImpl(private val vertx: Vertx, private val matchStatsRepo: MatchStatsRepo) : MatchHandler {
    private val matches = mutableMapOf<UUID, Match>()
    private val authorizedMatches get() = matches.filter { it.value.requiresAuthorization }.toMap()
    private val unauthorizedMatches get() = matches.filter { !it.value.requiresAuthorization }.toMap()
    private val matchEventListeners = mutableListOf<MatchEventListener>()

    override fun createMatch(): Match {
        val matchId = UUID.randomUUID()
        val initialState = GameState.createInitialState()
        matches[matchId] = Match(id = matchId, gameState = initialState)
        return matches[matchId]!!
    }

    override fun createMatch(createMatchRequest: CreateMatchRequest): Future<Unit> {
        val gameState = GameState.createInitialState()
        val player1 = Player(PaddleSide.Left, createMatchRequest.player1Id)
        val player2 = Player(PaddleSide.Right, createMatchRequest.player2Id)

        val match = Match(createMatchRequest.matchId, gameState, requiresAuthorization = true)
        matches[match.id] = match
        updateMatchStatsIfAuthorized(match)

        return CompositeFuture.all(addPlayer(match.id, player1), addPlayer(match.id, player2))
            .compose{
                Future.succeededFuture()
            }
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
        return matches.values.find { match ->
            match.players.values.any { it.connection?.hashCode() == sockJSSocket.hashCode() }
        }?: throw MatchNotFoundException()
    }

    override fun getMatchWithEmptySlot(): Match {
        return unauthorizedMatches.values.find { it.players.size == 1 } ?: throw MatchNotFoundException()
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

    override fun addPlayer(matchId: UUID, player: Player): Future<Unit> {
        val match = matches[matchId]?: return Future.failedFuture(MatchNotFoundException())

        if (match.players.size > 2) {
            return Future.failedFuture(MatchFullException())
        }

        match.players[player.id] = player
        match.dispatchGameState()
        match.dispatchPlayerInfo()

        return Future.succeededFuture()
    }

    override fun addPlayerConnection(matchId: UUID, playerId: UUID, sockJSSocket: SockJSSocket): Future<Unit> {
        val match = matches[matchId]?: return Future.failedFuture(MatchNotFoundException())
        val player = match.players[playerId]?: return Future.failedFuture(PlayerNotInMatchException())
        player.connection = sockJSSocket
        match.dispatchGameState()
        match.dispatchPlayerInfo()
        return Future.succeededFuture()
    }

    override fun handleAction(action: ClientAction, sockJSSocket: SockJSSocket) {
        val match = getMatchBySocket(sockJSSocket)

        when (action) {
            is ReadyAction -> handlePlayerReadyEvent(match, sockJSSocket)
            is KeyDownAction -> handleKeyDownEvent(match, sockJSSocket, action)
            is KeyUpAction -> handleKeyUpEvent(match, sockJSSocket, action)
        }

        match.dispatchGameState()
    }

    private fun startMatch(matchId: UUID) {
        val match = getMatchById(matchId)

        if (match.winner != null) {
            throw MatchAlreadyFinishedException()
        }

        // Set the paused state to false
        match.gameState.paused = false
        match.dispatchGameState()
        vertx.setPeriodic(0,1000L / TICK_RATE) { timerId ->
            if (match.gameState.paused) {
                // Cancel the timer when the game is paused
                match.dispatchGameState()
                vertx.cancelTimer(timerId)
            } else {
                updateGameState(match)
            }
        }
    }

    private fun onScore(match: Match) {
        match.dispatchGameState()
        match.dispatchStats()

        updateMatchStatsIfAuthorized(match).onSuccess {
            invokeEventListenersIfAuthorized(match) { listener ->
                val winner = match.winner

                if (winner == null) {
                    listener.onStateChanged(match.id)
                } else {
                    listener.onMatchEnd(match.id, winner)
                }
            }
        }
    }

    private fun onCollision(match: Match) {
        match.dispatchGameState()
    }

    private fun updateGameState(match: Match) {
        updatePaddlePositions(match)
        updateBallPosition(match, ::onScore)
        handlePaddleCollisions(match, ::onCollision)
        handleWallCollisions(match, ::onCollision)
    }

    private fun invokeEventListenersIfAuthorized(match: Match, event: (MatchEventListener) -> Unit) {
        if (match.requiresAuthorization) {
            matchEventListeners.forEach(event)
        }
    }

    private fun handlePlayerReadyEvent(match: Match, sockJSSocket: SockJSSocket) {
        val player = match.players.values.find { it.connection == sockJSSocket }?: throw PlayerNotInMatchException()

        if (match.allPlayersReady()) {
            return
        }

        player.ready = true
        match.dispatchGameState()

        if (match.allPlayersReady()) {
            countdownAndStartMatch(match.id, 5)
        }
    }

    private fun handleKeyDownEvent(match: Match, sockJSSocket: SockJSSocket, action: KeyDownAction) {
        val player = getPaddleBySocket(match, sockJSSocket)

        when (action.key) {
            "ArrowUp" -> player.upKeyPressed = true
            "ArrowDown" -> player.downKeyPressed = true
        }
    }

    private fun handleKeyUpEvent(match: Match, sockJSSocket: SockJSSocket, action: KeyUpAction) {
        val player = getPaddleBySocket(match, sockJSSocket)

        when (action.key) {
            "ArrowUp" -> player.upKeyPressed = false
            "ArrowDown" -> player.downKeyPressed = false
        }
    }

    private fun getPaddleBySocket(match: Match, sockJSSocket: SockJSSocket): Paddle {
        val player = match.players.values.find {
            it.connection.hashCode() == sockJSSocket.hashCode()
        }?: throw PlayerNotInMatchException()

        return when (player.paddleSide) {
            PaddleSide.Left -> match.gameState.leftPaddle
            PaddleSide.Right -> match.gameState.rightPaddle
        }
    }

    private fun updateMatchStatsIfAuthorized(match: Match): Future<Unit> {
        return if (match.requiresAuthorization) {
            matchStatsRepo.updateMatchStats(match.getMatchStats())
                .recover {
                    it.printStackTrace()
                    Future.failedFuture(it)
                }
        } else  {
            Future.succeededFuture()
        }
    }
}