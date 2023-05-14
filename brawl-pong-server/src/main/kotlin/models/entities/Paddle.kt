package io.sourceempire.brawlpong.models.entities

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonManagedReference
import io.vertx.ext.web.handler.sockjs.SockJSSocket
import java.util.UUID

data class Paddle(
    var playerId: UUID = UUID.randomUUID(),
) {
    enum class Direction {
        Stop,
        Up,
        Down
    }

    @JsonManagedReference
    lateinit var renderData: PlayerRenderData

    constructor(
        x: Float,
        y: Float,
        speed: Float,
        height: Int = 100,
        width: Int = 20
    ) : this() {
        renderData = PlayerRenderData(this, x, y, speed, height, width)
    }

    @JsonIgnore
    var upKeyPressed: Boolean = false

    @JsonIgnore
    var downKeyPressed: Boolean = false

    @JsonIgnore
    var connection: SockJSSocket? = null

    var score = 0

    var ready: Boolean = false

    val connected: Boolean
        get() = connection != null


    data class PlayerRenderData(
        @JsonBackReference
        val paddle: Paddle,
        var x: Float,
        var y: Float,
        var speed: Float,
        val height: Int = 100,
        val width: Int = 20
    ) {
        val direction: Direction
            get() = when {
                paddle.upKeyPressed && !paddle.downKeyPressed -> Direction.Up
                !paddle.upKeyPressed && paddle.downKeyPressed -> Direction.Down
                else -> Direction.Stop
            }
    }
}


