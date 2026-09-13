package com.yourgame.mario.physics

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
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
 *
 * resolveX/resolveY take the entity's velocity Vector2 and zero out the
 * relevant axis in place on collision, rather than returning a new value —
 * so there's nothing to allocate or box per call, and no wrapper result type
 * needed for resolveY's (velocity, grounded) pair.
 */
class CollisionHandler(solids: List<Rectangle>, private val tileSize: Float) {

    private val tileGrid: Map<Pair<Int, Int>, Rectangle> =
        solids.associateBy { tileCoord(it.x, it.y) }

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

    /** Zeroes `velocity.x` in place (and snaps `bounds` flush against the tile) on collision. */
    fun resolveX(bounds: Rectangle, velocity: Vector2) {
        val vx = velocity.x
        if (vx == 0f) return

        if (vx > 0f) {
            var nearestRight = Float.POSITIVE_INFINITY
            forEachNearbyTile(bounds) { tile ->
                if (bounds.overlaps(tile) && tile.x < nearestRight) nearestRight = tile.x
            }
            if (nearestRight != Float.POSITIVE_INFINITY) {
                bounds.x = nearestRight - bounds.width
                velocity.x = 0f
            }
        } else {
            var nearestLeft = Float.NEGATIVE_INFINITY
            forEachNearbyTile(bounds) { tile ->
                if (bounds.overlaps(tile) && tile.x + tile.width > nearestLeft) nearestLeft = tile.x + tile.width
            }
            if (nearestLeft != Float.NEGATIVE_INFINITY) {
                bounds.x = nearestLeft
                velocity.x = 0f
            }
        }
    }

    /** Zeroes `velocity.y` in place on collision; returns whether that collision means "standing on ground". */
    fun resolveY(bounds: Rectangle, velocity: Vector2): Boolean {
        val vy = velocity.y
        if (vy == 0f) return false

        if (vy < 0f) {
            var highestTop = Float.NEGATIVE_INFINITY
            forEachNearbyTile(bounds) { tile ->
                if (bounds.overlaps(tile) && tile.y + tile.height > highestTop) highestTop = tile.y + tile.height
            }
            if (highestTop == Float.NEGATIVE_INFINITY) return false
            bounds.y = highestTop
            velocity.y = 0f
            return true
        }

        var lowestBottom = Float.POSITIVE_INFINITY
        forEachNearbyTile(bounds) { tile ->
            if (bounds.overlaps(tile) && tile.y < lowestBottom) lowestBottom = tile.y
        }
        if (lowestBottom != Float.POSITIVE_INFINITY) {
            bounds.y = lowestBottom - bounds.height
            velocity.y = 0f
        }
        return false
    }

    /** A tile is at most `tileSize` wide, so the point can only ever land in one grid cell. */
    fun hasSolidAt(x: Float, y: Float): Boolean = tileGrid[tileCoord(x, y)]?.contains(x, y) ?: false
}
