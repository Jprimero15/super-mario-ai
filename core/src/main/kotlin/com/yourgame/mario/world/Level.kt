package com.yourgame.mario.world

import com.badlogic.gdx.math.Rectangle
import kotlin.math.max
import kotlin.random.Random

/**
 * Procedural endless world. Chunks are deterministic per seed, so the run is
 * random but reproducible for a given seed and never reaches a fixed endpoint.
 */
class Level(val tileSize: Float = 32f, private val seed: Long = 20260914L) {
    val heightInTiles = 12
    val widthInTiles = Int.MAX_VALUE / 4
    val widthInPixels get() = Float.MAX_VALUE / 8f
    val heightInPixels get() = heightInTiles * tileSize
    val solidTiles = mutableListOf<Rectangle>()
    val coinSpawns = mutableListOf<Rectangle>()
    val pipeSpawns = mutableListOf<Rectangle>()
    var playerStart = Rectangle(64f, tileSize, tileSize, tileSize)
        private set

    private val chunkWidth = 32
    private var generatedThroughChunk = -1

    init { ensureGeneratedThrough(1600f) }

    fun ensureGeneratedThrough(worldX: Float) {
        val targetChunk = max(0, (worldX / (chunkWidth * tileSize)).toInt())
        while (generatedThroughChunk < targetChunk) generateChunk(++generatedThroughChunk)
    }

    private fun generateChunk(chunk: Int) {
        val random = Random(seed + chunk * 7919L)
        val start = chunk * chunkWidth
        val end = start + chunkWidth
        val groundY = 0f

        for (x in start until end) {
            val gap = x > 8 && random.nextFloat() < 0.075f
            if (!gap) solidTiles += Rectangle(x * tileSize, groundY, tileSize, tileSize)
            if (!gap && random.nextFloat() < 0.20f) {
                val coinY = tileSize * (1 + random.nextInt(1, 4))
                coinSpawns += Rectangle(x * tileSize + tileSize * .25f, coinY + tileSize * .25f, tileSize * .5f, tileSize * .5f)
            }
        }

        val pipeCount = random.nextInt(0, 3)
        repeat(pipeCount) {
            val xTile = start + 7 + random.nextInt(chunkWidth - 14)
            val x = xTile * tileSize
            val heightTiles = random.nextInt(2, 4)
            // Pipes always have a solid ground tile beneath them, even if the procedural pass made a gap here.
            solidTiles += Rectangle(x, groundY, tileSize, tileSize)
            val pipe = Rectangle(x, groundY + tileSize, tileSize, heightTiles * tileSize)
            pipeSpawns += pipe
            for (row in 0 until heightTiles) solidTiles += Rectangle(x, groundY + tileSize + row * tileSize, tileSize, tileSize)
            coinSpawns += Rectangle(x + tileSize * .25f, groundY + pipe.height + tileSize * .35f, tileSize * .5f, tileSize * .5f)
        }

        val arcBase = start + 12
        for (i in 0 until 3) {
            val x = (arcBase + i * 3) * tileSize
            val y = tileSize * (3 + if (i == 1) 1 else 0)
            coinSpawns += Rectangle(x + tileSize * .25f, y + tileSize * .25f, tileSize * .5f, tileSize * .5f)
        }
    }

    companion object { fun level1(): Level = Level() }
}
