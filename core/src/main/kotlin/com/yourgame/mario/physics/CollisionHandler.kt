package com.yourgame.mario.physics

import com.badlogic.gdx.math.Rectangle

/**
 * Resolves an entity's movement against a list of solid tiles, one axis at a
 * time. Moving and resolving X before Y (rather than both at once) is what
 * avoids tunneling through thin geometry and getting caught on tile corners.
 */
class CollisionHandler(private val solids: List<Rectangle>) {

    /** Call after moving `bounds` horizontally. Returns the corrected X velocity. */
    fun resolveX(bounds: Rectangle, velocityX: Float): Float {
        var vx = velocityX
        for (tile in solids) {
            if (bounds.overlaps(tile)) {
                if (vx > 0f) bounds.x = tile.x - bounds.width
                else if (vx < 0f) bounds.x = tile.x + tile.width
                vx = 0f
            }
        }
        return vx
    }

    /** Call after moving `bounds` vertically. Returns (correctedVelocityY, isOnGround). */
    fun resolveY(bounds: Rectangle, velocityY: Float): Pair<Float, Boolean> {
        var vy = velocityY
        var onGround = false
        for (tile in solids) {
            if (bounds.overlaps(tile)) {
                if (vy > 0f) {
                    bounds.y = tile.y - bounds.height
                } else if (vy < 0f) {
                    bounds.y = tile.y + tile.height
                    onGround = true
                }
                vy = 0f
            }
        }
        return Pair(vy, onGround)
    }

    /** Used for ledge detection: is there solid ground at this exact point? */
    fun hasSolidAt(x: Float, y: Float): Boolean = solids.any { it.contains(x, y) }
}
