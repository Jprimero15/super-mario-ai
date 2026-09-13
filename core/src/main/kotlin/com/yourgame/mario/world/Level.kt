package com.yourgame.mario.world

import com.badlogic.gdx.math.Rectangle

/**
 * A small hand-authored, string-grid level format:
 *   '#' = solid ground/platform tile
 *   'C' = coin spawn
 *   'E' = enemy spawn
 *   'P' = player start
 *   '.' = empty space
 *
 * This keeps the vertical slice self-contained with no external map files to
 * ship or load. Swap this out for real TiledMap (.tmx) loading later, per
 * step 3 of the master prompt, once you're adding real art/tilesets.
 */
class Level(layout: List<String>, val tileSize: Float = 32f) {

    val widthInTiles = layout.maxOf { it.length }
    val heightInTiles = layout.size

    val solidTiles = mutableListOf<Rectangle>()
    val coinSpawns = mutableListOf<Rectangle>()
    val enemySpawns = mutableListOf<Rectangle>()
    var playerStart = Rectangle(0f, 0f, tileSize, tileSize)
        private set

    val widthInPixels get() = widthInTiles * tileSize
    val heightInPixels get() = heightInTiles * tileSize

    init {
        // layout[0] is the TOP row visually, so flip Y when converting to
        // world coordinates (LibGDX's Y axis points up, origin bottom-left).
        for (row in layout.indices) {
            val line = layout[row]
            val worldY = (heightInTiles - 1 - row) * tileSize
            for (col in line.indices) {
                val worldX = col * tileSize
                when (line[col]) {
                    '#' -> solidTiles.add(Rectangle(worldX, worldY, tileSize, tileSize))
                    'C' -> coinSpawns.add(
                        Rectangle(worldX + tileSize / 4f, worldY + tileSize / 4f, tileSize / 2f, tileSize / 2f)
                    )
                    'E' -> enemySpawns.add(Rectangle(worldX, worldY, tileSize, tileSize))
                    'P' -> playerStart = Rectangle(worldX, worldY, tileSize, tileSize)
                }
            }
        }
    }

    companion object {
        fun level1(): Level {
            val layout = listOf(
                "..........................................",
                "..........................................",
                "..................C.......................",
                "..............####........................",
                "..........................................",
                "P.......E...........C..C..C........E......",
                "##########..###############...#############"
            )
            return Level(layout)
        }
    }
}
