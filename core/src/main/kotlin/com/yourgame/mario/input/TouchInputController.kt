package com.yourgame.mario.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.Viewport

/**
 * On-screen touch buttons for Android (no keyboard available). Hit-tests
 * active touch pointers against button rectangles defined in the same
 * screen-space virtual units as the HUD viewport, so the buttons drawn by
 * PlayScreen line up exactly with what's tested here.
 *
 * Usage per frame: call poll() once before reading any is*Pressed() methods,
 * then call endFrame() once after — that pair is what makes isJumpJustPressed()
 * / isPauseJustPressed() correctly detect a fresh press instead of a held one.
 */
class TouchInputController(private val viewport: Viewport) : InputController {

    val leftButton = Rectangle(24f, 24f, 90f, 90f)
    val rightButton = Rectangle(130f, 24f, 90f, 90f)
    val jumpButton = Rectangle(660f, 24f, 110f, 110f)
    val pauseButton = Rectangle(730f, 420f, 50f, 50f)

    private var leftDown = false
    private var rightDown = false
    private var jumpDown = false
    private var pauseDown = false

    private var jumpWasDown = false
    private var pauseWasDown = false

    private val touchPoint = Vector2()

    /** Re-reads all active touch pointers. Call once at the top of each frame. */
    fun poll() {
        leftDown = false
        rightDown = false
        jumpDown = false
        pauseDown = false

        for (pointer in 0 until 4) {
            if (!Gdx.input.isTouched(pointer)) continue
            touchPoint.set(Gdx.input.getX(pointer).toFloat(), Gdx.input.getY(pointer).toFloat())
            viewport.unproject(touchPoint)

            if (leftButton.contains(touchPoint)) leftDown = true
            if (rightButton.contains(touchPoint)) rightDown = true
            if (jumpButton.contains(touchPoint)) jumpDown = true
            if (pauseButton.contains(touchPoint)) pauseDown = true
        }
    }

    /** Call once at the end of each frame, after gameplay code has read this frame's input. */
    fun endFrame() {
        jumpWasDown = jumpDown
        pauseWasDown = pauseDown
    }

    override fun isLeftPressed() = leftDown
    override fun isRightPressed() = rightDown
    override fun isJumpPressed() = jumpDown
    override fun isJumpJustPressed() = jumpDown && !jumpWasDown
    override fun isPauseJustPressed() = pauseDown && !pauseWasDown
}
