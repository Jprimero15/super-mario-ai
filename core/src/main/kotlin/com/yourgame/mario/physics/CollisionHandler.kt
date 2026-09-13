package com.yourgame.mario.physics

import com.badlogic.gdx.math.Rectangle
import kotlin.math.floor

/**
 * Resolves an entity against static solid rectangles, one axis at a time.
 *
 * Solids are indexed by tile coordinate at construction time, so a query
 * only checks the handful of tiles actually touching the entity's bounds
 * instead of scanning every solid tile in the level (which the previous
 * implementation did twice per entity, every frame). Doesn't matter much at
 * ~70 tiles for the vertical slice's one level, but it stops collision cost
 * from scaling with level size once real levels are added.
 */
class CollisionHandler(solids: List<Rectangle>, private val tileSize: Float) {

    private val tileGrid: Map<Pair<Int, Int>, Rectangle> =
        solids.associateBy { tileCoord(it.x, it.y) }

    // Reused across calls so resolveY doesn't allocate a new boxed result every frame.
    private val yResolution = YResolution(0f, false)

    private fun tileCoord(x: Float, y: Float) =
        floor(x / tileSize).toInt() to floor(y / tileSize).toInt()

    private inline fun forEachNearbyTile(bounds: Rectangle, action: (Rectangle) -> Unit) {
        val minTx = floor(bounds.x / tileSize).toInt() - 1
        val maxTx = floor((bounds.x + bounds.width) / tileSize).toInt() + 1
        val minTy = floor(bounds.y / tileSize).toInt() - 1
        val maxTy = floor((bounds.y + bounds.height) / tileSize).toInt() + 1

        for (tx in minTx..maxTx) {
            for (ty in minTy..maxTy) {
                tileGrid[tx to ty]?.let(action)
            }
        }
    }

    fun resolveX(bounds: Rectangle, velocityX: Float): Float {
        if (velocityX == 0f) return 0f

        var corrected = velocityX
        if (velocityX > 0f) {
            var nearestRight = Float.POSITIVE_INFINITY
            forEachNearbyTile(bounds) { tile ->
                if (bounds.overlaps(tile) && tile.x < nearestRight) nearestRight = tile.x
            }
            if (nearestRight != Float.POSITIVE_INFINITY) {
                bounds.x = nearestRight - bounds.width
                corrected = 0f
            }
        } else {
            var nearestLeft = Float.NEGATIVE_INFINITY
            forEachNearbyTile(bounds) { tile ->
                if (bounds.overlaps(tile) && tile.x + tile.width > nearestLeft) nearestLeft = tile.x + tile.width
            }
            if (nearestLeft != Float.NEGATIVE_INFINITY) {
                bounds.x = nearestLeft
                corrected = 0f
            }
        }
        return corrected
    }

    fun resolveY(bounds: Rectangle, velocityY: Float): YResolution {
        if (velocityY == 0f) return yResolution.set(0f, false)

        if (velocityY < 0f) {
            var highestTop = Float.NEGATIVE_INFINITY
            forEachNearbyTile(bounds) { tile ->
                if (bounds.overlaps(tile) && tile.y + tile.height > highestTop) highestTop = tile.y + tile.height
            }
            return if (highestTop != Float.NEGATIVE_INFINITY) {
                bounds.y = highestTop
                yResolution.set(0f, true)
            } else {
                yResolution.set(velocityY, false)
            }
        }

        var lowestBottom = Float.POSITIVE_INFINITY
        forEachNearbyTile(bounds) { tile ->
            if (bounds.overlaps(tile) && tile.y < lowestBottom) lowestBottom = tile.y
        }
        return if (lowestBottom != Float.POSITIVE_INFINITY) {
            bounds.y = lowestBottom - bounds.height
            yResolution.set(0f, false)
        } else {
            yResolution.set(velocityY, false)
        }
    }

    /** A tile is at most `tileSize` wide, so the point can only ever land in one grid cell. */
    fun hasSolidAt(x: Float, y: Float): Boolean = tileGrid[tileCoord(x, y)]?.contains(x, y) ?: false
}

/**
 * Mutable (velocityY, grounded) pair. A dedicated class rather than
 * Kotlin's `Pair<Float, Boolean>` so CollisionHandler can reuse one instance
 * instead of boxing+allocating a new result every call; supports the same
 * `val (vy, grounded) = ...` destructuring call sites already use.
 */
class YResolution(var velocityY: Float, var grounded: Boolean) {
    internal fun set(velocityY: Float, grounded: Boolean): YResolution {
        this.velocityY = velocityY
        this.grounded = grounded
        return this
    }

    operator fun component1() = velocityY
    operator fun component2() = grounded
}
