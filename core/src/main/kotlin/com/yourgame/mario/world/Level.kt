package com.yourgame.mario.world

import com.badlogic.gdx.math.Rectangle
import kotlin.math.max
import kotlin.random.Random

enum class MonsterTier(val width: Float, val height: Float, val speed: Float) {
    SMALL(28f, 28f, 62f),
    MEDIUM(36f, 36f, 72f),
    LARGE(48f, 48f, 84f)
}

data class MonsterSpawn(val bounds: Rectangle, val tier: MonsterTier)
data class HoleSpawn(val bounds: Rectangle)

data class PowerUpSpawn(val bounds: Rectangle)

/** Endless deterministic chunks with bounded retention and seam-aware spacing. */
class Level(val tileSize: Float = 32f, private val seed: Long = 20260914L) {
    val heightInTiles = 12
    val heightInPixels get() = heightInTiles * tileSize

    val solidTiles = mutableListOf<Rectangle>()
    val pipeSpawns = mutableListOf<Rectangle>()
    val holeSpawns = mutableListOf<HoleSpawn>()

    private val pendingCoins = mutableListOf<Rectangle>()
    private val pendingMonsters = mutableListOf<MonsterSpawn>()
    private val pendingPowerUps = mutableListOf<PowerUpSpawn>()
    private val recentOccupied = mutableListOf<Rectangle>()

    var playerStart = Rectangle(64f, tileSize, tileSize, tileSize)
        private set

    private val chunkWidth = 32
    private val occupancyOverlapTiles = 20
    private var generatedThroughChunk = -1
    private var revisionValue = 0L

    val revision: Long get() = revisionValue
    val nextTierForSteps: (Int) -> MonsterTier = { steps -> monsterTierForSteps(steps) }

    init {
        ensureGeneratedThrough(1600f)
    }

    fun ensureGeneratedThrough(worldX: Float) {
        val targetChunk = max(0, (worldX / (chunkWidth * tileSize)).toInt())
        while (generatedThroughChunk < targetChunk) {
            generateChunk(++generatedThroughChunk)
        }
    }

    fun monsterTierForSteps(steps: Int): MonsterTier = when {
        steps < 200 -> MonsterTier.SMALL
        steps < 400 -> MonsterTier.MEDIUM
        else -> MonsterTier.LARGE
    }

    fun stepsUntilNextTier(steps: Int): Int = when {
        steps < 200 -> 200 - steps
        steps < 400 -> 400 - steps
        else -> 0
    }

    fun drainCoinSpawns(): List<Rectangle> = pendingCoins.toList().also { pendingCoins.clear() }
    fun drainMonsterSpawns(): List<MonsterSpawn> = pendingMonsters.toList().also { pendingMonsters.clear() }
    fun drainPowerUpSpawns(): List<PowerUpSpawn> = pendingPowerUps.toList().also { pendingPowerUps.clear() }

    /** Keep only a bounded world window behind the player. */
    fun pruneBefore(worldX: Float) {
        var changed = false
        changed = solidTiles.removeAll { it.x + it.width < worldX } || changed
        changed = pipeSpawns.removeAll { it.x + it.width < worldX } || changed
        changed = holeSpawns.removeAll { it.bounds.x + it.bounds.width < worldX } || changed
        recentOccupied.removeAll { it.x + it.width < worldX }
        if (changed) revisionValue++
    }

    private fun generateChunk(chunk: Int) {
        val random = Random(seed + chunk * 7919L)
        val start = chunk * chunkWidth
        val end = start + chunkWidth
        val groundY = 0f
        val occupied = recentOccupied.toMutableList()
        val holes = mutableListOf<Pair<Int, Int>>()

        var cursor = start + 10
        val holeChance = (.08f + chunk * .0004f).coerceAtMost(.18f)
        while (cursor < end - 3) {
            if (random.nextFloat() < holeChance) {
                val width = random.nextInt(1, 3)
                holes += cursor to width
                cursor += width + random.nextInt(5, 8)
            } else {
                cursor += random.nextInt(3, 6)
            }
        }

        fun inHole(tile: Int) = holes.any { tile >= it.first && tile < it.first + it.second }
        fun freeArea(rect: Rectangle, padding: Float = 8f): Boolean {
            val expanded = Rectangle(rect.x - padding, rect.y - padding, rect.width + padding * 2f, rect.height + padding * 2f)
            return occupied.none { it.overlaps(expanded) }
        }

        for (x in start until end) {
            if (!inHole(x)) solidTiles += Rectangle(x * tileSize, groundY, tileSize, tileSize)
        }
        for ((holeX, holeWidth) in holes) {
            holeSpawns += HoleSpawn(Rectangle(holeX * tileSize, groundY, holeWidth * tileSize, tileSize))
        }

        for (x in start + 2 until end - 2) {
            if (!inHole(x) && random.nextFloat() < .18f) {
                val coin = Rectangle(x * tileSize + 9f, tileSize * random.nextInt(1, 4) + 9f, 14f, 14f)
                if (freeArea(coin, 6f)) {
                    pendingCoins += coin
                    occupied += coin
                }
            }
        }

        val pipeCount = random.nextInt(0, if (chunk < 2) 2 else 3)
        repeat(pipeCount) {
            var placed = false
            repeat(8) {
                if (!placed) {
                    val xTile = start + 7 + random.nextInt((chunkWidth - 14).coerceAtLeast(1))
                    val x = xTile * tileSize
                    val heightTiles = random.nextInt(2, 4)
                    val pipe = Rectangle(x, groundY + tileSize, tileSize, heightTiles * tileSize)
                    val base = Rectangle(x, groundY, tileSize, tileSize)
                    if (!inHole(xTile) && freeArea(pipe, 22f)) {
                        solidTiles += base
                        for (row in 0 until heightTiles) {
                            solidTiles += Rectangle(x, groundY + tileSize + row * tileSize, tileSize, tileSize)
                        }
                        pipeSpawns += pipe
                        occupied += pipe
                        placed = true
                    }
                }
            }
        }

        val tier = monsterTierForSteps(start)
        val monsterCount = if (chunk < 2) 0 else random.nextInt(1, 3)
        repeat(monsterCount) {
            var spawned = false
            repeat(8) {
                if (!spawned) {
                    val xTile = start + 8 + random.nextInt((chunkWidth - 12).coerceAtLeast(1))
                    val monster = Rectangle(xTile * tileSize + 2f, tileSize + 1f, tier.width, tier.height)
                    if (!inHole(xTile) && freeArea(monster, 18f)) {
                        pendingMonsters += MonsterSpawn(monster, tier)
                        occupied += monster
                        spawned = true
                    }
                }
            }
        }

        val arcBase = start + 12
        for (i in 0 until 3) {
            val xTile = arcBase + i * 3
            if (!inHole(xTile)) {
                val coin = Rectangle(xTile * tileSize + 9f, tileSize * (3 + if (i == 1) 1 else 0) + 9f, 14f, 14f)
                if (freeArea(coin, 5f)) {
                    pendingCoins += coin
                    occupied += coin
                }
            }
        }

        if (chunk >= 3 && random.nextFloat() < .12f) {
            val powerTile = start + 18 + random.nextInt(7)
            if (!inHole(powerTile)) {
                val powerUp = Rectangle(powerTile * tileSize + 6f, tileSize * 2f + 5f, 22f, 22f)
                if (freeArea(powerUp, 18f)) {
                    pendingPowerUps += PowerUpSpawn(powerUp)
                    occupied += powerUp
                }
            }
        }

        recentOccupied.clear()
        val overlapStartX = (end - occupancyOverlapTiles) * tileSize
        for (rect in occupied) {
            if (rect.x + rect.width >= overlapStartX) recentOccupied += Rectangle(rect)
        }
        revisionValue++
    }

    companion object {
        fun level1(): Level = Level()
    }
}
