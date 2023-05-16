package io.sourceempire.brawlpong.models

import io.vertx.core.json.JsonObject

interface ClientAction {
    companion object {
        fun fromJson(json: JsonObject): ClientAction {
            return when (val actionType = json.getString("type")) {
                "ready" -> ReadyAction()
                "key-down" -> KeyDownAction(json)
                "key-up" -> KeyUpAction(json)
                else -> throw IllegalArgumentException("Unsupported client action: $actionType")
            }
        }
    }
}

private interface KeyAction: ClientAction {
    val key: String
}

class ReadyAction: ClientAction

data class KeyDownAction(override val key: String): KeyAction {
    constructor(json: JsonObject): this(json.getString("key"))
}

data class KeyUpAction(override val key: String): KeyAction {
    constructor(json: JsonObject): this(json.getString("key"))
}
