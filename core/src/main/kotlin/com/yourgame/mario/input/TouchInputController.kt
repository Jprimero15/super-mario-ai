package com.yourgame.mario.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.Viewport

/** Responsive multi-touch controller for the Android game HUD. */
class TouchInputController(private val viewport: Viewport) : InputController {
    val leftButton = Rectangle(22f, 22f, 74f, 74f)
    val rightButton = Rectangle(106f, 22f, 74f, 74f)
    val jumpButton = Rectangle(680f, 22f, 96f, 96f)
    val pauseButton = Rectangle(726f, 420f, 50f, 50f)
    val restartButton = Rectangle(250f, 165f, 135f, 54f)
    val menuButton = Rectangle(415f, 165f, 135f, 54f)

    private var leftDown = false
    private var rightDown = false
    private var jumpDown = false
    private var pauseDown = false
    private var restartDown = false
    private var menuDown = false

    private var jumpWasDown = false
    private var pauseWasDown = false
    private var restartWasDown = false
    private var menuWasDown = false

    private val touchPoint = Vector2()

    fun poll() {
        leftDown = false; rightDown = false; jumpDown = false; pauseDown = false
        restartDown = false; menuDown = false

        for (pointer in 0 until 10) {
            if (!Gdx.input.isTouched(pointer)) continue
            touchPoint.set(Gdx.input.getX(pointer).toFloat(), Gdx.input.getY(pointer).toFloat())
            viewport.unproject(touchPoint)
            if (leftButton.contains(touchPoint)) leftDown = true
            if (rightButton.contains(touchPoint)) rightDown = true
            if (jumpButton.contains(touchPoint)) jumpDown = true
            if (pauseButton.contains(touchPoint)) pauseDown = true
            if (restartButton.contains(touchPoint)) restartDown = true
            if (menuButton.contains(touchPoint)) menuDown = true
        }
    }

    fun endFrame() {
        jumpWasDown = jumpDown; pauseWasDown = pauseDown
        restartWasDown = restartDown; menuWasDown = menuDown
    }

    override fun isLeftPressed() = leftDown
    override fun isRightPressed() = rightDown
    override fun isJumpPressed() = jumpDown
    override fun isJumpJustPressed() = jumpDown && !jumpWasDown
    override fun isPauseJustPressed() = pauseDown && !pauseWasDown
    fun isRestartJustPressed() = restartDown && !restartWasDown
    fun isMenuJustPressed() = menuDown && !menuWasDown
}
