package io.sourceempire.brawlpong.models

import io.vertx.core.json.JsonObject
import java.util.*

data class CreateMatchRequest(val matchId: UUID, val player1Id: UUID, val player2Id: UUID) {
    constructor(json: JsonObject): this(
        UUID.fromString(json.getString("matchId")),
        UUID.fromString(json.getString("player1Id")),
        UUID.fromString(json.getString("player2Id"))
    )
}