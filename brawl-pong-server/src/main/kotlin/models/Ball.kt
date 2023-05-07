package models

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Ball(
    var x: Float,
    var y: Float,
    var dx: Float,
    var dy: Float,
    var radius: Float = 10f
) {
    val speed: Float
        get() = sqrt(dx * dx + dy * dy)

    var touchedPaddle = false

    // These are used to store the direction if ball speed is set to 0
    private var storedDx = dx
    private var storedDy = dy

    constructor(x: Float, y: Float, speed: Float) : this(x, y, 0f, 0f) {
        this.setRandomDirection()
        this.setSpeed(speed)
    }

    private fun setDirection(angle: Float) {
        val tempDx = cos(angle)
        val tempDy = sin(angle)

        if (speed == 0f) {
            storedDx = tempDx
            storedDy = tempDy
        } else {
            dx = tempDx * speed
            dy = tempDy * speed
        }
    }

    fun setRandomDirection() {
        // Generate a random angle in degrees within the specified ranges (315-45 and 135-215)
        val angleDegrees = when {
            Math.random() < 0.5 -> (Math.random() * 90 - 45) // 315 to 45 degrees
            else -> (Math.random() * 80 + 135) // 135 to 215 degrees
        }

        // Convert the angle to radians
        val angleRadians = Math.toRadians(angleDegrees).toFloat()

        setDirection(angleRadians)
    }

    fun setSpeed(newSpeed: Float) {
        if (newSpeed == 0f && storedDx == 0f && storedDy == 0f) {
            throw IllegalStateException("Cannot set speed to 0 when last non-zero direction is not set.")
        }

        val currentSpeed = sqrt(dx * dx + dy * dy)

        if (currentSpeed != 0f) {
            val scaleFactor = newSpeed / currentSpeed
            dx *= scaleFactor
            dy *= scaleFactor
        } else {
            dx = storedDx * newSpeed
            dy = storedDy * newSpeed
        }
    }
}