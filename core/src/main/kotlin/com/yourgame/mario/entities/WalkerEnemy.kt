package com.yourgame.mario.entities

import com.yourgame.mario.physics.CollisionHandler
import com.yourgame.mario.physics.Physics
import com.yourgame.mario.world.MonsterTier

/** Patrol monster with small, medium and large progression tiers. */
class WalkerEnemy(startX: Float, startY: Float, val tier: MonsterTier = MonsterTier.SMALL) :
    Entity(startX, startY, tier.width, tier.height) {

    private var direction = -1f

    fun updatePhysics(delta: Float, collision: CollisionHandler) {
        if (!alive) return
        velocity.x = tier.speed * direction
        velocity.y += Physics.GRAVITY * delta
        velocity.y = velocity.y.coerceAtLeast(Physics.TERMINAL_VELOCITY)

        bounds.x += velocity.x * delta
        val incomingVx = velocity.x
        collision.resolveX(bounds, velocity)
        if (velocity.x == 0f && incomingVx != 0f) direction *= -1f

        bounds.y += velocity.y * delta
        val grounded = collision.resolveY(bounds, velocity)
        if (grounded) {
            val aheadX = if (direction > 0f) bounds.x + bounds.width + 1f else bounds.x - 1f
            val footY = bounds.y - 1f
            if (!collision.hasSolidAt(aheadX, footY)) direction *= -1f
        }
    }
}
