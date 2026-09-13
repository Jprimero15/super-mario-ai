package com.yourgame.mario.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.Viewport

/** High-response polling controller with independent touch hit zones. */
class TouchInputController(private val viewport: Viewport) : InputController {
    // Visual bounds are intentionally smaller than their touch targets.
    val leftButton = Rectangle(20f, 14f, 50f, 50f)
    val rightButton = Rectangle(88f, 14f, 50f, 50f)
    val jumpButton = Rectangle(718f, 10f, 62f, 62f)
    val pauseButton = Rectangle(748f, 424f, 32f, 32f)
    val restartButton = Rectangle(275f, 150f, 110f, 50f)
    val menuButton = Rectangle(415f, 150f, 110f, 50f)

    private val leftHit = Rectangle(8f, 4f, 70f, 76f)
    private val rightHit = Rectangle(82f, 4f, 70f, 76f)
    private val jumpHit = Rectangle(700f, 2f, 98f, 80f)
    private val pauseHit = Rectangle(736f, 412f, 44f, 48f)
    private val restartHit = Rectangle(262f, 140f, 136f, 72f)
    private val menuHit = Rectangle(402f, 140f, 136f, 72f)

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

    /** Poll every available pointer every frame; direction + jump can be simultaneous. */
    fun poll() {
        leftDown = false; rightDown = false; jumpDown = false
        pauseDown = false; restartDown = false; menuDown = false
        val maxPointers = 20
        for (pointer in 0 until maxPointers) {
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
