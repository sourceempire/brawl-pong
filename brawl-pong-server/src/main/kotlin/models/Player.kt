package io.sourceempire.brawlpong.models

import com.fasterxml.jackson.annotation.JsonIgnore
import io.vertx.ext.web.handler.sockjs.SockJSSocket
import java.util.UUID

enum class PaddleSide {
    Left,
    Right
}

data class Player(
    val paddleSide: PaddleSide,
    val id: UUID = UUID.randomUUID(),
    var score: Int = 0
) {

    // TODO -> handle disconnect (https://chat.openai.com/c/e59dac15-920f-4233-9d6e-97e424f028b2)
    val connected: Boolean get() = connection != null
    var ready: Boolean = false

    @JsonIgnore
    var connection: SockJSSocket? = null

    fun getPlayerStats(): PlayerStats {
        return PlayerStats(id, score)
    }
}