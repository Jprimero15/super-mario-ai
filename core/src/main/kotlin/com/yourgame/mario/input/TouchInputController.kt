package com.yourgame.mario.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.Viewport

/** Responsive multi-touch controller. Direction and jump can be held together. */
class TouchInputController(private val viewport: Viewport) : InputController {
    val leftButton = Rectangle(18f, 14f, 54f, 54f)
    val rightButton = Rectangle(92f, 14f, 54f, 54f)
    val jumpButton = Rectangle(718f, 10f, 64f, 64f)
    val pauseButton = Rectangle(748f, 424f, 32f, 32f)
    val restartButton = Rectangle(275f, 150f, 110f, 50f)
    val menuButton = Rectangle(415f, 150f, 110f, 50f)

    // Generous touch zones; map through the HUD viewport so they stay correct on all aspect ratios.
    private val leftHit = Rectangle(0f, 0f, 86f, 86f)
    private val rightHit = Rectangle(80f, 0f, 86f, 86f)
    private val jumpHit = Rectangle(690f, 0f, 110f, 88f)
    private val pauseHit = Rectangle(730f, 405f, 70f, 75f)
    private val restartHit = Rectangle(250f, 130f, 160f, 90f)
    private val menuHit = Rectangle(390f, 130f, 160f, 90f)

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
        leftDown = false; rightDown = false; jumpDown = false
        pauseDown = false; restartDown = false; menuDown = false
        for (pointer in 0 until 20) {
            if (!Gdx.input.isTouched(pointer)) continue
            touchPoint.set(Gdx.input.getX(pointer).toFloat(), Gdx.input.getY(pointer).toFloat())
            viewport.unproject(touchPoint)
            if (leftHit.contains(touchPoint)) leftDown = true
            if (rightHit.contains(touchPoint)) rightDown = true
            if (jumpHit.contains(touchPoint)) jumpDown = true
            if (pauseHit.contains(touchPoint)) pauseDown = true
            if (restartHit.contains(touchPoint)) restartDown = true
            if (menuHit.contains(touchPoint)) menuDown = true
        }
    }

    fun endFrame() {
        jumpWasDown = jumpDown
        pauseWasDown = pauseDown
        restartWasDown = restartDown
        menuWasDown = menuDown
    }

    override fun isLeftPressed() = leftDown
    override fun isRightPressed() = rightDown
    override fun isJumpPressed() = jumpDown
    override fun isJumpJustPressed() = jumpDown && !jumpWasDown
    override fun isPauseJustPressed() = pauseDown && !pauseWasDown
    fun isRestartJustPressed() = restartDown && !restartWasDown
    fun isMenuJustPressed() = menuDown && !menuWasDown
}
