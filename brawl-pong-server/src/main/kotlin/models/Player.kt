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
    val connected: Boolean get() = connection != null
    var ready: Boolean = false

    @JsonIgnore
    var connection: SockJSSocket? = null
}