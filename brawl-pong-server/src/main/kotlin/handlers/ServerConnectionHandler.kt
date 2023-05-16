package io.sourceempire.brawlpong.handlers

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.sourceempire.brawlpong.listeners.MatchEventListener
import io.sourceempire.brawlpong.models.CreateMatchRequest
import io.sourceempire.brawlpong.repos.MatchStatsRepo
import io.sourceempire.brawlpong.utils.getEnvProperty
import io.vertx.core.eventbus.Message
import io.vertx.core.json.Json
import io.vertx.kotlin.core.json.jsonObjectOf
import java.util.*

interface ServerConnectionHandler: MatchEventListener {
    companion object {
        fun create(
            vertx: Vertx,
            matchHandler: MatchHandler,
            matchStatsRepo: MatchStatsRepo,
        ): ServerConnectionHandler = ServerConnectionHandlerImpl(vertx, matchHandler, matchStatsRepo)
    }
}

class ServerConnectionHandlerImpl(
    vertx: Vertx,
    private val matchHandler: MatchHandler,
    private val matchStatsRepo: MatchStatsRepo,
): ServerConnectionHandler {
    private val eventBus = vertx.eventBus()

    init {
        setupCreateMatchRequest()
        setupMatchStatsRequest()
    }

    override fun onStateChanged(matchId: UUID) {
        val message = jsonObjectOf("matchId" to matchId)
        eventBus.send("brawl-server.pong.match.state-change", message)
    }

    override fun onMatchEnd(matchId: UUID, winner: UUID) {
        val message = jsonObjectOf("matchId" to matchId, "winner" to winner)
        eventBus.send("brawl-server.pong.match.end", message)
    }

    private fun setupCreateMatchRequest() {
        eventBus.consumer<JsonObject>("brawl-pong.create-match").handler { message ->
            val request = CreateMatchRequest(message.body())
            matchHandler.createMatch(request).onSuccess {
                message.reply(jsonObjectOf("serverAddress" to "${getEnvProperty("THIS_SERVER_URL")}:${getEnvProperty("HTTP_PORT")}/match/"))
            }.onFailure {
                it.printStackTrace()
                message.fail(500, it.message)
            }
        }
    }

    private fun setupMatchStatsRequest() {
        eventBus.consumer<JsonObject>("brawl-pong.match-stats").handler(::handleMatchStatsRequest)
    }

    private fun handleMatchStatsRequest(message: Message<JsonObject>) {
        val matchId = getMatchIdFromMessage(message)

        if (matchId != null) {
            fetchAndReplyMatchStats(matchId, message)
        } else {
            message.fail(400, "no match id provided")
        }
    }

    private fun getMatchIdFromMessage(message: Message<JsonObject>): UUID? {
        return try {
            UUID.fromString(message.body().getString("matchId"))
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    private fun fetchAndReplyMatchStats(matchId: UUID, message: Message<JsonObject>) {
        matchStatsRepo.getMatchStats(matchId).onSuccess { matchStats ->
            message.reply(Json.encode(matchStats))
        }.onFailure {
            it.printStackTrace()
            message.fail(500, it.message)
        }
    }
}