package com.yourgame.mario.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.yourgame.mario.MarioGame
import com.yourgame.mario.entities.Coin
import com.yourgame.mario.entities.Player
import com.yourgame.mario.entities.WalkerEnemy
import com.yourgame.mario.input.TouchInputController
import com.yourgame.mario.physics.CollisionHandler
import com.yourgame.mario.physics.Physics
import com.yourgame.mario.ui.HUD
import com.yourgame.mario.world.Level

/**
 * NOTE ON VISUALS: entities are drawn as flat-colored rectangles via
 * ShapeRenderer rather than sprites, so this project runs with zero external
 * asset files. Swap in a TextureAtlas + SpriteBatch per the master prompt's
 * step 4 once you have real art.
 */
class PlayScreen(private val game: MarioGame) : Screen {

    companion object {
        const val VIEWPORT_WIDTH = 800f
        const val VIEWPORT_HEIGHT = 480f
        const val LEVEL_TIME_LIMIT = 200
    }

    private val level = Level.level1()
    private val collision = CollisionHandler(level.solidTiles, level.tileSize)

    private val camera = OrthographicCamera()
    private val viewport: Viewport = FitViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, camera)

    private val hudCamera = OrthographicCamera()
    private val hudViewport: Viewport = FitViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, hudCamera)

    // Touch buttons are hit-tested in the HUD viewport's screen-space units,
    // so it needs a reference to that viewport to unproject touch coordinates.
    private val input = TouchInputController(hudViewport)

    private val shapeRenderer = ShapeRenderer()
    private val batch = SpriteBatch()
    private val font = BitmapFont().apply { data.setScale(1.4f) }
    private val hud = HUD(font)

    private val player = Player(level.playerStart.x, level.playerStart.y)
    private val enemies = level.enemySpawns.map { WalkerEnemy(it.x, it.y) }.toMutableList()
    private val coins = level.coinSpawns.map { Coin(it.x, it.y, it.width, it.height) }.toMutableList()

    private var paused = false
    private var elapsedTime = 0f
    private var levelComplete = false
    private var flickerClock = 0f

    override fun show() {
        hudCamera.position.set(hudViewport.worldWidth / 2f, hudViewport.worldHeight / 2f, 0f)
    }

    override fun render(delta: Float) {
        val frameDelta = delta.coerceIn(0f, 1f / 30f)
        input.poll()
        if (input.isPauseJustPressed()) paused = !paused
        if (!paused && !levelComplete) update(frameDelta)
        draw()
        input.endFrame()
    }

    private fun update(delta: Float) {
        elapsedTime += delta
        flickerClock += delta

        val timeRemaining = LEVEL_TIME_LIMIT - elapsedTime.toInt()
        if (timeRemaining <= 0 && !player.isDead) {
            player.killInstantly()
        }

        player.updatePhysics(delta, input, collision)
        for (enemy in enemies) {
            if (enemy.alive) enemy.updatePhysics(delta, collision)
        }

        handlePlayerEnemyCollisions()
        handleCoinCollisions()

        if (player.bounds.y < -200f && !player.isDead) {
            player.killInstantly()
        }

        if (player.isDead) {
            player.velocityY += Physics.GRAVITY * delta
            player.bounds.y += player.velocityY * delta
            if (player.bounds.y < -400f) respawnOrGameOver()
        }

        // Reaching the far right edge of the level clears it.
        if (player.bounds.x + player.bounds.width >= level.widthInPixels - level.tileSize) {
            levelComplete = true
        }

        updateCamera()
    }

    private fun handlePlayerEnemyCollisions() {
        for (enemy in enemies) {
            if (!enemy.alive || player.isDead) continue
            if (!player.bounds.overlaps(enemy.bounds)) continue

            val stompedFromAbove = player.velocityY < 0f &&
                player.bounds.y >= (enemy.bounds.y + enemy.bounds.height - 10f)

            if (stompedFromAbove) {
                enemy.alive = false
                player.velocityY = Player.JUMP_VELOCITY * 0.5f
                player.score += 100
            } else if (!player.isInvincible) {
                val fatal = player.shrinkOrDie()
                if (fatal) {
                    player.killInstantly()
                } else {
                    player.velocityX = if (player.bounds.x < enemy.bounds.x) -200f else 200f
                }
            }
        }
        enemies.removeAll { !it.alive }
    }

    private fun handleCoinCollisions() {
        val iterator = coins.iterator()
        while (iterator.hasNext()) {
            val coin = iterator.next()
            if (player.bounds.overlaps(coin.bounds)) {
                player.score += 10
                iterator.remove()
            }
        }
    }

    private fun respawnOrGameOver() {
        player.lives -= 1
        if (player.lives <= 0) {
            game.screen = GameOverScreen(game, player.score)
            dispose()
            return
        }
        player.resetTo(level.playerStart.x, level.playerStart.y)
    }

    private fun updateCamera() {
        val halfWidth = viewport.worldWidth / 2f
        camera.position.x = if (level.widthInPixels > viewport.worldWidth) {
            (player.bounds.x + player.bounds.width / 2f).coerceIn(halfWidth, level.widthInPixels - halfWidth)
        } else {
            halfWidth
        }
        camera.position.y = viewport.worldHeight / 2f
        camera.update()
    }

    private fun draw() {
        Gdx.gl.glClearColor(0.45f, 0.7f, 0.95f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        shapeRenderer.projectionMatrix = camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        shapeRenderer.color = Color.FOREST
        for (tile in level.solidTiles) shapeRenderer.rect(tile.x, tile.y, tile.width, tile.height)

        shapeRenderer.color = Color.GOLD
        for (coin in coins) shapeRenderer.rect(coin.bounds.x, coin.bounds.y, coin.bounds.width, coin.bounds.height)

        shapeRenderer.color = Color.MAROON
        for (enemy in enemies) shapeRenderer.rect(enemy.bounds.x, enemy.bounds.y, enemy.bounds.width, enemy.bounds.height)

        // Flicker the player sprite while invincible; a lower-fidelity but
        // dependency-free stand-in for an alpha-blended flash.
        val visibleThisFrame = !player.isInvincible || (flickerClock % 0.2f) < 0.1f
        if (visibleThisFrame) {
            shapeRenderer.color = if (player.isDead) Color.GRAY else Color.RED
            shapeRenderer.rect(player.bounds.x, player.bounds.y, player.bounds.width, player.bounds.height)
        }

        shapeRenderer.end()

        // Touch buttons live in HUD screen-space so they stay fixed on screen
        // regardless of camera scroll, and line up with what poll() hit-tests.
        shapeRenderer.projectionMatrix = hudCamera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        drawButton(input.leftButton, input.isLeftPressed())
        drawButton(input.rightButton, input.isRightPressed())
        drawButton(input.jumpButton, input.isJumpPressed())
        drawButton(input.pauseButton, paused)
        shapeRenderer.end()

        batch.projectionMatrix = hudCamera.combined
        batch.begin()
        val timeRemaining = (LEVEL_TIME_LIMIT - elapsedTime.toInt()).coerceAtLeast(0)
        hud.render(batch, player, timeRemaining)
        font.draw(batch, "<", input.leftButton.x + 34f, input.leftButton.y + 60f)
        font.draw(batch, ">", input.rightButton.x + 34f, input.rightButton.y + 60f)
        font.draw(batch, "JUMP", input.jumpButton.x + 18f, input.jumpButton.y + 65f)
        font.draw(batch, "||", input.pauseButton.x + 14f, input.pauseButton.y + 35f)
        if (paused) font.draw(batch, "PAUSED", 360f, 300f)
        if (levelComplete) font.draw(batch, "LEVEL COMPLETE!  Score: ${player.score}", 190f, 300f)
        batch.end()
    }

    private fun drawButton(bounds: Rectangle, pressed: Boolean) {
        shapeRenderer.color = if (pressed) Color.LIGHT_GRAY else Color.DARK_GRAY
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height)
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
        hudViewport.update(width, height, true)
    }

    override fun pause() {}
    override fun resume() {}
    override fun hide() {}

    override fun dispose() {
        shapeRenderer.dispose()
        batch.dispose()
        font.dispose()
    }
}
