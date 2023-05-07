package models

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonManagedReference
import io.vertx.ext.web.handler.sockjs.SockJSSocket
import java.util.UUID

data class Player(
    var id: UUID = UUID.randomUUID(),
) {

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
        val player: Player,
        var x: Float,
        var y: Float,
        var speed: Float,
        val height: Int = 100,
        val width: Int = 20
    ) {
        val direction: PlayerDirection
            get() = when {
                player.upKeyPressed && !player.downKeyPressed -> PlayerDirection.Up
                !player.upKeyPressed && player.downKeyPressed -> PlayerDirection.Down
                else -> PlayerDirection.Stop
            }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Player

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}


