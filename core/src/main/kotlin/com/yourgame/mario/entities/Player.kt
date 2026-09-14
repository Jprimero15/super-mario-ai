package com.yourgame.mario.entities

import com.yourgame.mario.input.InputController
import com.yourgame.mario.physics.CollisionHandler
import com.yourgame.mario.physics.Physics

// Player size is used by damage progression without requiring sprite assets.
enum class PlayerSize(val width: Float, val height: Float) {
    SMALL(28f, 28f),
    BIG(28f, 56f)
}

class Player(startX: Float, startY: Float) :
    Entity(startX, startY, PlayerSize.SMALL.width, PlayerSize.SMALL.height) {

    companion object {
        const val BASE_RUN_SPEED = 250f
        const val SPEED_PER_100_STEPS = 16f
        const val MAX_RUN_SPEED = 430f
        const val JUMP_VELOCITY = 500f
        const val MIN_JUMP_VELOCITY = 190f
        const val COYOTE_TIME = 0.12f
        const val JUMP_BUFFER = 0.14f
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
    var coinsCollected = 0
    var lives = 3

    private var invincibleTimer = 0f
    private var coyoteTimer = 0f
    private var jumpBufferTimer = 0f
    private var jumpHeld = false

    fun currentRunSpeed(steps: Int): Float =
        (BASE_RUN_SPEED + (steps / 100) * SPEED_PER_100_STEPS).coerceAtMost(MAX_RUN_SPEED)

    fun grow() {
        if (size == PlayerSize.SMALL) {
            size = PlayerSize.BIG
            bounds.height = size.height
            bounds.y -= PlayerSize.BIG.height - PlayerSize.SMALL.height
        }
    }

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

    /** Marks the run as dead and gives a small, predictable fall animation. */
    fun killInstantly() {
        if (isDead) return
        isDead = true
        jumpHeld = false
        coyoteTimer = 0f
        jumpBufferTimer = 0f
        velocity.x = 0f
        velocity.y = -240f
    }

    fun resetTo(x: Float, y: Float) {
        isDead = false
        bounds.x = x
        bounds.y = y
        velocity.setZero()
        onGround = false
        jumpHeld = false
        coyoteTimer = 0f
        jumpBufferTimer = 0f
        startInvincibility(2f)
    }

    fun updatePhysics(delta: Float, input: InputController, collision: CollisionHandler, steps: Int) {
        if (isDead) return

        val steer = when {
            input.isLeftPressed() -> -1f
            input.isRightPressed() -> 1f
            else -> 0f
        }
        val targetSpeed = if (steer < 0f) -currentRunSpeed(steps) else currentRunSpeed(steps)
        facingRight = targetSpeed >= 0f
        velocity.x += (targetSpeed - velocity.x) * (1f - kotlin.math.exp(-12f * delta))
        velocity.x = velocity.x.coerceIn(-MAX_RUN_SPEED, MAX_RUN_SPEED)

        coyoteTimer = if (onGround) COYOTE_TIME else (coyoteTimer - delta).coerceAtLeast(0f)
        jumpBufferTimer = if (input.isJumpJustPressed()) JUMP_BUFFER else (jumpBufferTimer - delta).coerceAtLeast(0f)

        if (jumpBufferTimer > 0f && coyoteTimer > 0f) {
            velocity.y = JUMP_VELOCITY
            jumpHeld = true
            coyoteTimer = 0f
            jumpBufferTimer = 0f
            onGround = false
        }

        if (jumpHeld && !input.isJumpPressed() && velocity.y > MIN_JUMP_VELOCITY) velocity.y = MIN_JUMP_VELOCITY
        if (!input.isJumpPressed()) jumpHeld = false

        velocity.y += Physics.GRAVITY * delta
        velocity.y = velocity.y.coerceAtLeast(Physics.TERMINAL_VELOCITY)

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
