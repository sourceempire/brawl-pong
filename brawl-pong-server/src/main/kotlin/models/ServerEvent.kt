package models

import io.vertx.core.json.JsonObject
import java.util.*

enum class ServerEventType {
    CreateMatch;

    companion object {
        fun fromString(serverMessage: String): ServerEventType {
            return when (serverMessage) {
                "create-match" -> CreateMatch
                else -> throw IllegalArgumentException("Unsupported server message: $serverMessage")
            }
        }
    }
}

interface ServerEvent {
    val type: ServerEventType

    companion object {
        fun fromJson(json: JsonObject): ServerEvent {
            return when (ServerEventType.fromString(json.getString("type"))) {
                ServerEventType.CreateMatch -> CreateMatchEvent(json)
            }
        }
    }
}

data class CreateMatchEvent(val matchId: UUID, val player1Id: UUID, val player2Id: UUID): ServerEvent {
    override val type = ServerEventType.CreateMatch

    constructor(json: JsonObject): this(
        UUID.fromString(json.getString("matchId")),
        UUID.fromString(json.getString("player1Id")),
        UUID.fromString(json.getString("player2Id"))
    )
}