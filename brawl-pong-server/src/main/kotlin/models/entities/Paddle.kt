package io.sourceempire.brawlpong.models.entities

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonManagedReference
import io.vertx.ext.web.handler.sockjs.SockJSSocket
import java.util.UUID

class Paddle(val x: Float,
             var y: Float,
             val speed: Float,
             val height: Int = 100,
             val width: Int = 20) {
    enum class Direction {
        Stop,
        Up,
        Down
    }

    @JsonIgnore
    var upKeyPressed: Boolean = false

    @JsonIgnore
    var downKeyPressed: Boolean = false

    val direction: Direction
        get() = when {
            upKeyPressed && !downKeyPressed -> Direction.Up
            !upKeyPressed && downKeyPressed -> Direction.Down
            else -> Direction.Stop
        }
}


