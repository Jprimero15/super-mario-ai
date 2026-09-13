package com.yourgame.mario.physics

import com.badlogic.gdx.math.Rectangle

/** Resolves an entity against static solid rectangles, one axis at a time. */
class CollisionHandler(private val solids: List<Rectangle>) {

    fun resolveX(bounds: Rectangle, velocityX: Float): Float {
        if (velocityX == 0f) return 0f

        var corrected = velocityX
        if (velocityX > 0f) {
            var nearestRight = Float.POSITIVE_INFINITY
            for (tile in solids) {
                if (!bounds.overlaps(tile)) continue
                if (tile.x < nearestRight) nearestRight = tile.x
            }
            if (nearestRight != Float.POSITIVE_INFINITY) {
                bounds.x = nearestRight - bounds.width
                corrected = 0f
            }
        } else {
            var nearestLeft = Float.NEGATIVE_INFINITY
            for (tile in solids) {
                if (!bounds.overlaps(tile)) continue
                if (tile.x + tile.width > nearestLeft) nearestLeft = tile.x + tile.width
            }
            if (nearestLeft != Float.NEGATIVE_INFINITY) {
                bounds.x = nearestLeft
                corrected = 0f
            }
        }
        return corrected
    }

    fun resolveY(bounds: Rectangle, velocityY: Float): Pair<Float, Boolean> {
        if (velocityY == 0f) return 0f to false

        if (velocityY < 0f) {
            var highestTop = Float.NEGATIVE_INFINITY
            for (tile in solids) {
                if (!bounds.overlaps(tile)) continue
                if (tile.y + tile.height > highestTop) highestTop = tile.y + tile.height
            }
            return if (highestTop != Float.NEGATIVE_INFINITY) {
                bounds.y = highestTop
                0f to true
            } else {
                velocityY to false
            }
        }

        var lowestBottom = Float.POSITIVE_INFINITY
        for (tile in solids) {
            if (!bounds.overlaps(tile)) continue
            if (tile.y < lowestBottom) lowestBottom = tile.y
        }
        return if (lowestBottom != Float.POSITIVE_INFINITY) {
            bounds.y = lowestBottom - bounds.height
            0f to false
        } else {
            velocityY to false
        }
    }

    fun hasSolidAt(x: Float, y: Float): Boolean = solids.any { it.contains(x, y) }
}
