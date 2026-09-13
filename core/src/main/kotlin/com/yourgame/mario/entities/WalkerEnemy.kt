package com.yourgame.mario.entities

import com.yourgame.mario.physics.CollisionHandler
import com.yourgame.mario.physics.Physics

/**
 * Simple patrol enemy: walks in one direction, turns around at walls and at
 * ledges. Killed by a stomp from above; damages the player on side contact
 * (handled by PlayScreen, which owns collision resolution between entities).
 */
class WalkerEnemy(startX: Float, startY: Float) : Entity(startX, startY, 28f, 28f) {

    companion object {
        const val SPEED = 60f
    }

    private var direction = -1f

    fun updatePhysics(delta: Float, collision: CollisionHandler) {
        if (!alive) return

        velocityX = SPEED * direction
        velocityY += Physics.GRAVITY * delta
        velocityY = velocityY.coerceAtLeast(Physics.TERMINAL_VELOCITY)

        bounds.x += velocityX * delta
        val resolvedVx = collision.resolveX(bounds, velocityX)
        if (resolvedVx == 0f && velocityX != 0f) {
            direction *= -1f // hit a wall
        }
        velocityX = resolvedVx

        bounds.y += velocityY * delta
        val (resolvedVy, grounded) = collision.resolveY(bounds, velocityY)
        velocityY = resolvedVy

        if (grounded) {
            val aheadX = if (direction > 0f) bounds.x + bounds.width + 1f else bounds.x - 1f
            val footY = bounds.y - 1f
            if (!collision.hasSolidAt(aheadX, footY)) {
                direction *= -1f // about to walk off a ledge
            }
        }
    }
}
