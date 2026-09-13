package com.yourgame.mario.input

/**
 * Abstraction between gameplay code and the actual input hardware. On
 * Android there's no keyboard, so the concrete implementation is
 * TouchInputController — gameplay code (Player) never knows the difference.
 */
interface InputController {
    fun isLeftPressed(): Boolean
    fun isRightPressed(): Boolean
    fun isJumpPressed(): Boolean
    fun isJumpJustPressed(): Boolean
    fun isPauseJustPressed(): Boolean
}
