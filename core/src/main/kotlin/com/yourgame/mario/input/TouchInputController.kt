package com.yourgame.mario.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.Viewport

/** Responsive multi-touch controller with mutually exclusive direction zones. */
class TouchInputController(private val viewport: Viewport) : InputController {
    val leftButton = Rectangle(18f, 14f, 54f, 54f)
    val rightButton = Rectangle(92f, 14f, 54f, 54f)
    val jumpButton = Rectangle(718f, 10f, 64f, 64f)
    val pauseButton = Rectangle(748f, 424f, 32f, 32f)
    val restartButton = Rectangle(275f, 150f, 110f, 50f)
    val menuButton = Rectangle(415f, 150f, 110f, 50f)

    private val leftHit = Rectangle(0f, 0f, 78f, 86f)
    private val rightHit = Rectangle(88f, 0f, 78f, 86f)
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
    private var appliedLeftHanded = false
    private val touchPoint = Vector2()
    private val prefs = Gdx.app.getPreferences("maryou_settings")

    init { applyHandedness(false) }

    private fun applyHandedness(force: Boolean) {
        val leftHanded = prefs.getBoolean("leftHanded", false)
        if (!force && leftHanded == appliedLeftHanded) return
        appliedLeftHanded = leftHanded
        if (leftHanded) {
            jumpButton.set(18f, 10f, 64f, 64f)
            leftButton.set(736f, 14f, 54f, 54f)
            rightButton.set(662f, 14f, 54f, 54f)
        } else {
            leftButton.set(18f, 14f, 54f, 54f)
            rightButton.set(92f, 14f, 54f, 54f)
            jumpButton.set(718f, 10f, 64f, 64f)
        }
    }

    fun poll() {
        applyHandedness(false)
        leftDown = false; rightDown = false; jumpDown = false
        pauseDown = false; restartDown = false; menuDown = false
        for (pointer in 0 until 20) {
            if (!Gdx.input.isTouched(pointer)) continue
            touchPoint.set(Gdx.input.getX(pointer).toFloat(), Gdx.input.getY(pointer).toFloat())
            viewport.unproject(touchPoint)
            val leftZone = if (appliedLeftHanded) Rectangle(654f, 0f, 78f, 86f) else leftHit
            val rightZone = if (appliedLeftHanded) Rectangle(742f, 0f, 58f, 86f) else rightHit
            if (leftZone.contains(touchPoint)) leftDown = true
            if (rightZone.contains(touchPoint)) rightDown = true
            val jumpZone = if (appliedLeftHanded) Rectangle(0f, 0f, 110f, 88f) else jumpHit
            if (jumpZone.contains(touchPoint)) jumpDown = true
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
