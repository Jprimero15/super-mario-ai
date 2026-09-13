package com.yourgame.mario.entities

import com.badlogic.gdx.math.Rectangle

abstract class Entity(x: Float, y: Float, width: Float, height: Float) {
    val bounds = Rectangle(x, y, width, height)
    var velocityX = 0f
    var velocityY = 0f
    var alive = true
}
