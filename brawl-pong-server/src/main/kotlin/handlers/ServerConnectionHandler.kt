package io.sourceempire.brawlpong.handlers

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.sourceempire.brawlpong.listeners.MatchEventListener
import io.sourceempire.brawlpong.models.CreateMatchRequest
import io.sourceempire.brawlpong.utils.getEnvProperty
import io.vertx.kotlin.core.json.jsonObjectOf
import java.util.*

interface ServerConnectionHandler: MatchEventListener {
    companion object {
        fun create(
            vertx: Vertx,
            matchHandler: MatchHandler,
        ): ServerConnectionHandler = ServerConnectionHandlerImpl(vertx, matchHandler)
    }
}

class ServerConnectionHandlerImpl(
    vertx: Vertx,
    private val matchHandler: MatchHandler,
): ServerConnectionHandler {
    private val eventBus = vertx.eventBus()

    init {
        eventBus.consumer<JsonObject>("brawl-pong.create-match").handler { message ->
            val request = CreateMatchRequest(message.body())

            matchHandler.createMatch(request).onSuccess {
                message.reply(jsonObjectOf("serverAddress" to "${getEnvProperty("THIS_SERVER_URL")}:${getEnvProperty("HTTP_PORT")}/match/"))
            }.onFailure {
                message.fail(500, it.message)
            }
        }
    }

    override fun onStateChanged(matchId: UUID) {
        val message = jsonObjectOf("matchId" to matchId)
        eventBus.send("brawl-server.pong.match.state-change", message)
    }

    override fun onMatchEnd(matchId: UUID, winner: UUID) {
        val message = jsonObjectOf("matchId" to matchId, "winner" to winner)
        eventBus.send("brawl-server.pong.match.end", message)
    }
}