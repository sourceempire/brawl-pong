package models

import io.vertx.core.json.JsonObject

enum class ClientEventType {
    KeyUp,
    KeyDown,
    Ready;

    companion object {
        fun fromString(clientMessage: String): ClientEventType {
            return when (clientMessage) {
                "key-up" -> KeyUp
                "key-down" -> KeyDown
                "ready" -> Ready
                else -> throw IllegalArgumentException("Unsupported client message: $clientMessage")
            }
        }
    }
}

interface ClientEvent {
    val type: ClientEventType

    companion object {
        fun fromJson(json: JsonObject): ClientEvent {
            return when (ClientEventType.fromString(json.getString("type"))) {
                ClientEventType.Ready -> ReadyEvent()
                ClientEventType.KeyDown -> KeyDownEvent(json)
                ClientEventType.KeyUp -> KeyUpEvent(json)
            }
        }
    }
}

private interface KeyEvent: ClientEvent {
    val key: String
}

class ReadyEvent: ClientEvent {
    override val type: ClientEventType = ClientEventType.Ready
}


data class KeyDownEvent(override val key: String) : KeyEvent {
    override val type: ClientEventType = ClientEventType.KeyDown

    constructor(json: JsonObject): this(json.getString("key"))
}

data class KeyUpEvent(override val key: String) : KeyEvent {
    override val type: ClientEventType = ClientEventType.KeyDown

    constructor(json: JsonObject): this(json.getString("key"))
}
