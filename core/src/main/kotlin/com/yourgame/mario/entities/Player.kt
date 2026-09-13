package com.yourgame.mario.entities

import com.yourgame.mario.input.InputController
import com.yourgame.mario.physics.CollisionHandler
import com.yourgame.mario.physics.Physics

enum class PlayerSize(val width: Float, val height: Float) {
    SMALL(28f, 28f),
    BIG(28f, 56f)
}

class Player(startX: Float, startY: Float) :
    Entity(startX, startY, PlayerSize.SMALL.width, PlayerSize.SMALL.height) {

    companion object {
        const val MOVE_SPEED = 220f
        const val ACCEL = 1400f
        const val FRICTION = 1600f
        const val JUMP_VELOCITY = 480f
        const val MIN_JUMP_VELOCITY = 180f
        const val COYOTE_TIME = 0.1f
        const val JUMP_BUFFER = 0.12f
    }

    var size = PlayerSize.SMALL
        private set
    var onGround = false
        private set
    var isInvincible = false
        private set
    var facingRight = true
        private set
    var isDead = false

    var score = 0
    var lives = 3

    private var invincibleTimer = 0f
    private var coyoteTimer = 0f
    private var jumpBufferTimer = 0f
    private var jumpHeld = false

    fun grow() {
        if (size == PlayerSize.SMALL) {
            size = PlayerSize.BIG
            bounds.height = size.height
        }
    }

    /** Returns true if this hit is fatal (player was already Small). */
    fun shrinkOrDie(): Boolean {
        return if (size == PlayerSize.BIG) {
            size = PlayerSize.SMALL
            bounds.height = size.height
            startInvincibility()
            false
        } else {
            true
        }
    }

    fun startInvincibility(duration: Float = 1.5f) {
        isInvincible = true
        invincibleTimer = duration
    }

    fun killInstantly() {
        isDead = true
        velocity.set(0f, JUMP_VELOCITY * 0.6f)
    }

    fun resetTo(x: Float, y: Float) {
        isDead = false
        bounds.x = x
        bounds.y = y
        velocity.setZero()
        startInvincibility(2f)
    }

    fun updatePhysics(delta: Float, input: InputController, collision: CollisionHandler) {
        if (isDead) return

        // --- Horizontal movement (accelerate toward max speed, decelerate via friction) ---
        val moveDir = when {
            input.isLeftPressed() -> -1f
            input.isRightPressed() -> 1f
            else -> 0f
        }

        if (moveDir != 0f) {
            facingRight = moveDir > 0f
            velocity.x += moveDir * ACCEL * delta
            velocity.x = velocity.x.coerceIn(-MOVE_SPEED, MOVE_SPEED)
        } else if (velocity.x > 0f) {
            velocity.x = (velocity.x - FRICTION * delta).coerceAtLeast(0f)
        } else if (velocity.x < 0f) {
            velocity.x = (velocity.x + FRICTION * delta).coerceAtMost(0f)
        }

        // --- Coyote time (grace period after walking off a ledge) & jump buffering ---
        coyoteTimer = if (onGround) COYOTE_TIME else (coyoteTimer - delta).coerceAtLeast(0f)
        jumpBufferTimer = if (input.isJumpJustPressed()) JUMP_BUFFER else (jumpBufferTimer - delta).coerceAtLeast(0f)

        if (jumpBufferTimer > 0f && coyoteTimer > 0f) {
            velocity.y = JUMP_VELOCITY
            jumpHeld = true
            coyoteTimer = 0f
            jumpBufferTimer = 0f
            onGround = false
        }

        // --- Variable jump height: cut the rise short if the button is released early ---
        if (jumpHeld && !input.isJumpPressed() && velocity.y > MIN_JUMP_VELOCITY) {
            velocity.y = MIN_JUMP_VELOCITY
        }
        if (!input.isJumpPressed()) jumpHeld = false

        // --- Gravity ---
        velocity.y += Physics.GRAVITY * delta
        velocity.y = velocity.y.coerceAtLeast(Physics.TERMINAL_VELOCITY)

        // --- Move and resolve X, then Y (prevents tunneling / corner snagging) ---
        bounds.x += velocity.x * delta
        collision.resolveX(bounds, velocity)

        bounds.y += velocity.y * delta
        onGround = collision.resolveY(bounds, velocity)

        if (isInvincible) {
            invincibleTimer -= delta
            if (invincibleTimer <= 0f) isInvincible = false
        }
    }
}
