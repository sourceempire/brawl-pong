package io.sourceempire.brawlpong.models

import io.vertx.core.json.JsonObject
import java.util.*

data class PlayerStats(val playerId: UUID, val score: Int)
data class MatchStats(val matchId: UUID, val players: Map<UUID, PlayerStats>, val winner: UUID?) {
    fun toJson(): JsonObject = JsonObject.mapFrom(this)
}