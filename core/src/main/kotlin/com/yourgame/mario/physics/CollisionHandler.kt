package com.yourgame.mario.physics

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import kotlin.math.floor

/** Grid-indexed collision handler for the currently retained procedural world window. */
class CollisionHandler(solids: List<Rectangle>, private val tileSize: Float) {
    private val tileGrid = HashMap<Pair<Int, Int>, Rectangle>(solids.size * 2)

    init { replaceSolids(solids) }

    fun replaceSolids(solids: List<Rectangle>) {
        tileGrid.clear()
        for (solid in solids) tileGrid[tileCoord(solid.x, solid.y)] = solid
    }

    fun addSolids(solids: List<Rectangle>) {
        for (solid in solids) tileGrid[tileCoord(solid.x, solid.y)] = solid
    }

    fun pruneBefore(worldX: Float) {
        val iterator = tileGrid.entries.iterator()
        while (iterator.hasNext()) {
            val solid = iterator.next().value
            if (solid.x + solid.width < worldX) iterator.remove()
        }
    }

    private fun tileCoord(x: Float, y: Float) = floor(x / tileSize).toInt() to floor(y / tileSize).toInt()

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

    fun hasSolidAt(x: Float, y: Float): Boolean = tileGrid[tileCoord(x, y)]?.contains(x, y) ?: false
}
