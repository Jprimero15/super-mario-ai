package com.yourgame.mario.entities

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2

abstract class Entity(x: Float, y: Float, width: Float, height: Float) {
    val bounds = Rectangle(x, y, width, height)
    val velocity = Vector2()
    var alive = true
}
