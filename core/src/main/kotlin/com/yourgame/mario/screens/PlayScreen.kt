package com.yourgame.mario.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.yourgame.mario.MaryouGame
import com.yourgame.mario.audio.SoundManager
import com.yourgame.mario.entities.Coin
import com.yourgame.mario.entities.Player
import com.yourgame.mario.entities.ShieldOrb
import com.yourgame.mario.entities.WalkerEnemy
import com.yourgame.mario.input.TouchInputController
import com.yourgame.mario.physics.CollisionHandler
import com.yourgame.mario.physics.Physics
import com.yourgame.mario.ui.FontFactory
import com.yourgame.mario.ui.HUD
import com.yourgame.mario.ui.VectorArt
import com.yourgame.mario.world.HoleSpawn
import com.yourgame.mario.world.Level
import com.yourgame.mario.world.MonsterSpawn
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class PlayScreen(private val game: MaryouGame) : Screen {
    companion object {
        const val VIEWPORT_WIDTH = 800f
        const val WORLD_HEIGHT = 400f
        const val UI_HEIGHT = 480f
        private const val RETAIN_BEHIND = 1200f
        private const val GENERATE_AHEAD = 2200f
    }

    private data class Particle(var x: Float, var y: Float, var vx: Float, var vy: Float, var life: Float, val maxLife: Float, val radius: Float)

    private val level = Level.level1()
    private val collision = CollisionHandler(level.solidTiles, level.tileSize)
    private val camera = OrthographicCamera()
    private val viewport: Viewport = FitViewport(VIEWPORT_WIDTH, WORLD_HEIGHT, camera)
    private val hudCamera = OrthographicCamera()
    private val hudViewport: Viewport = FitViewport(VIEWPORT_WIDTH, UI_HEIGHT, hudCamera)
    private val input = TouchInputController(hudViewport)
    private val shapeRenderer = ShapeRenderer()
    private val batch = SpriteBatch()
    private val font: BitmapFont = FontFactory.create(22)
    private val hud = HUD(font)
    private val textLayout = GlyphLayout()
    private val sound = SoundManager()
    private val player = Player(level.playerStart.x, level.playerStart.y)
    private val coins = mutableListOf<Coin>()
    private val monsters = mutableListOf<WalkerEnemy>()
    private val powerUps = mutableListOf<ShieldOrb>()
    private val particles = mutableListOf<Particle>()

    private val sky = Color(.035f, .055f, .085f, 1f)
    private val worldPanel = Color(.28f, .55f, .82f, 1f)
    private val cloud = Color(.47f, .72f, .88f, .22f)
    private val groundGlow = Color(.17f, .42f, .34f, .92f)
    private val hudPanel = Color(.035f, .055f, .085f, .92f)
    private val hudLine = Color(1f, 1f, 1f, .055f)
    private val buttonColor = Color(.08f, .14f, .21f, 1f)
    private val jumpButtonColor = Color(.86f, .22f, .12f, 1f)
    private val overlay = Color(.02f, .035f, .06f, .80f)
    private val card = Color(.065f, .10f, .15f, .99f)

    private var worldRevision = level.revision
    private var paused = false
    private var animTime = 0f
    private var checkpointX = player.bounds.x
    private var previousPlayerX = player.bounds.x
    private var previousPlayerBottom = player.bounds.y + player.bounds.height
    private var lastStepMilestone = 0
    private var cameraX = VIEWPORT_WIDTH / 2f
    private var cameraY = WORLD_HEIGHT / 2f
    private var screenShake = 0f
    private var squashTimer = 0f
    private var stretchTimer = 0f
    private var comboTimer = 0f
    private var combo = 0
    private var showTutorial = !Gdx.app.getPreferences("maryou_run_records").getBoolean("tutorialSeen", false)
    private var transitionAlpha = 1f

    override fun show() {
        hudCamera.position.set(hudViewport.worldWidth / 2f, hudViewport.worldHeight / 2f, 0f)
        hudCamera.update()
        syncWorld()
    }

    override fun render(delta: Float) {
        val d = delta.coerceIn(0f, 1f / 30f)
        animTime += d
        transitionAlpha = (transitionAlpha - d * 3f).coerceAtLeast(0f)

        input.poll()
        if (showTutorial && Gdx.input.justTouched()) dismissTutorial()
        if (input.isPauseJustPressed()) paused = !paused

        if (paused) {
            if (input.isRestartJustPressed()) { restart(); return }
            if (input.isMenuJustPressed()) { game.screen = MainMenuScreen(game); dispose(); return }
        } else {
            update(d)
        }

        draw()
        input.endFrame()
    }

    private fun dismissTutorial() {
        showTutorial = false
        Gdx.app.getPreferences("maryou_run_records").putBoolean("tutorialSeen", true).flush()
    }

    private fun restart() {
        game.screen = PlayScreen(game)
        dispose()
    }

    private fun update(delta: Float) {
        previousPlayerX = player.bounds.x
        previousPlayerBottom = player.bounds.y + player.bounds.height

        level.ensureGeneratedThrough(player.bounds.x + GENERATE_AHEAD)
        level.pruneBefore(player.bounds.x - RETAIN_BEHIND)
        syncWorld()

        val steps = currentSteps()
        val wasGrounded = player.onGround
        val jumpPressed = input.isJumpJustPressed()
        val coinsBefore = player.coinsCollected

        player.updatePhysics(delta, input, collision, steps)

        if (jumpPressed && wasGrounded) {
            sound.play("jump", .55f)
            stretchTimer = .10f
        }
        if (!wasGrounded && player.onGround) squashTimer = .12f

        for (monster in monsters) {
            if (monster.alive && monster.bounds.x + monster.bounds.width > player.bounds.x - 900f && monster.bounds.x < player.bounds.x + 1200f) {
                monster.updatePhysics(delta, collision)
            }
        }

        handlePipeHits()
        handleMonsterCollisions()
        handleCoinCollisions()
        handlePowerUps()

        if (player.coinsCollected > coinsBefore) {
            sound.play("coin", .55f)
        }

        if (!player.isDead && player.bounds.y + player.bounds.height < -8f && isOverHole()) {
            player.killInstantly()
            screenShake = .22f
            burst(player.bounds.x + player.bounds.width / 2f, 8f, 8)
        }

        if (player.isDead) {
            player.velocity.y += Physics.GRAVITY * delta
            player.bounds.y += player.velocity.y * delta
            if (player.bounds.y < -360f) respawn()
        }

        comboTimer -= delta
        if (comboTimer <= 0f) combo = 0

        val newSteps = currentSteps()
        if (newSteps / 100 > lastStepMilestone) {
            lastStepMilestone = newSteps / 100
            sound.play("milestone", .65f)
        }
        if (player.onGround && player.velocity.x > 40f && animTime % .24f < delta) sound.play("step", .22f)

        val nextCheckpoint = (player.bounds.x / 256f).toInt() * 256f + 64f
        if (nextCheckpoint > checkpointX && !isHoleAt(nextCheckpoint)) checkpointX = nextCheckpoint

        particles.removeAll {
            it.life -= delta
            it.x += it.vx * delta
            it.y += it.vy * delta
            it.vy -= 420f * delta
            it.life <= 0f
        }
        squashTimer = (squashTimer - delta).coerceAtLeast(0f)
        stretchTimer = (stretchTimer - delta).coerceAtLeast(0f)
        screenShake = (screenShake - delta).coerceAtLeast(0f)

        coins.removeAll { it.bounds.x + it.bounds.width < player.bounds.x - RETAIN_BEHIND }
        monsters.removeAll { !it.alive || it.bounds.x + it.bounds.width < player.bounds.x - RETAIN_BEHIND }
        powerUps.removeAll { it.bounds.x + it.bounds.width < player.bounds.x - RETAIN_BEHIND }

        updateCamera(delta)
    }

    private fun currentSteps(): Int = max(0, (player.bounds.x / level.tileSize).toInt())

    private fun isHoleAt(worldX: Float): Boolean = level.holeSpawns.any { worldX >= it.bounds.x && worldX <= it.bounds.x + it.bounds.width }

    private fun isOverHole(): Boolean {
        val footCenter = player.bounds.x + player.bounds.width * .50f
        return level.holeSpawns.any { footCenter > it.bounds.x + 2f && footCenter < it.bounds.x + it.bounds.width - 2f }
    }

    private fun syncWorld() {
        if (worldRevision != level.revision) {
            collision.replaceSolids(level.solidTiles)
            worldRevision = level.revision
        }
        for (s in level.drainCoinSpawns()) coins += Coin(s.x, s.y, s.width, s.height)
        for (s in level.drainMonsterSpawns()) monsters += WalkerEnemy(s.bounds.x, s.bounds.y, s.tier)
        for (s in level.drainPowerUpSpawns()) powerUps += ShieldOrb(s.bounds.x, s.bounds.y, s.bounds.width, s.bounds.height)
    }

    private fun handleCoinCollisions() {
        val iterator = coins.iterator()
        while (iterator.hasNext()) {
            val coin = iterator.next()
            if (player.bounds.overlaps(coin.bounds)) {
                player.coinsCollected++
                player.score += 10
                burst(coin.bounds.x + coin.bounds.width / 2f, coin.bounds.y + coin.bounds.height / 2f, 6)
                iterator.remove()
            }
        }
    }

    private fun handlePowerUps() {
        val iterator = powerUps.iterator()
        while (iterator.hasNext()) {
            val orb = iterator.next()
            if (player.bounds.overlaps(orb.bounds)) {
                player.startInvincibility(4f)
                player.score += 25
                sound.play("milestone", .5f)
                burst(orb.bounds.x + orb.bounds.width / 2f, orb.bounds.y + orb.bounds.height / 2f, 10)
                iterator.remove()
            }
        }
    }

    private fun handlePipeHits() {
        if (player.isInvincible || player.isDead) return
        for (pipe in level.pipeSpawns) {
            if (pipe.x > player.bounds.x + 180f || pipe.x + pipe.width < player.bounds.x - 80f) continue
            val pipeTop = pipe.y + pipe.height
            val currentBottom = player.bounds.y + player.bounds.height
            val previousRight = previousPlayerX + player.bounds.width
            val currentRight = player.bounds.x + player.bounds.width
            val landedOnTop = currentBottom >= pipeTop - 2f && currentBottom <= pipeTop + 8f && previousPlayerBottom <= pipeTop + 10f && player.onGround
            val verticalContact = player.bounds.y < pipeTop - 3f && currentBottom > pipe.y + 3f
            val crossedFromLeft = previousRight <= pipe.x + 4f && currentRight >= pipe.x - 1f
            val crossedFromRight = previousPlayerX >= pipe.x + pipe.width - 4f && player.bounds.x <= pipe.x + pipe.width + 1f
            if (verticalContact && (crossedFromLeft || crossedFromRight) && !landedOnTop) {
                damagePlayer()
                return
            }
        }
    }

    private fun handleMonsterCollisions() {
        if (player.isDead) return
        for (monster in monsters) {
            if (!monster.alive || !player.bounds.overlaps(monster.bounds)) continue
            val monsterTop = monster.bounds.y + monster.bounds.height
            val stomp = player.velocity.y < 0f && previousPlayerBottom >= monsterTop - 3f && player.bounds.y + player.bounds.height <= monsterTop + 12f
            if (stomp) {
                monster.alive = false
                combo = min(5, if (comboTimer > 0f) combo + 1 else 1)
                comboTimer = 1.8f
                player.score += 50 * combo
                player.velocity.y = Player.JUMP_VELOCITY * .62f
                sound.play("stomp", .65f)
                burst(monster.bounds.x + monster.bounds.width / 2f, monsterTop, 9)
            } else if (!player.isInvincible) {
                damagePlayer()
                return
            }
        }
    }

    private fun damagePlayer() {
        sound.play("hit", .65f)
        screenShake = .20f
        burst(player.bounds.x + player.bounds.width / 2f, player.bounds.y + player.bounds.height / 2f, 8)
        if (player.shrinkOrDie()) player.killInstantly()
    }

    private fun burst(x: Float, y: Float, count: Int) {
        repeat(count) { i ->
            val angle = i.toFloat() / count.coerceAtLeast(1) * 6.28318f
            val speed = 60f + (i % 3) * 28f
            particles += Particle(x, y, kotlin.math.cos(angle) * speed, kotlin.math.sin(angle) * speed + 40f, .42f, .42f, 2.5f + (i % 2))
        }
    }

    private fun respawn() {
        player.lives -= 1
        if (player.lives <= 0) {
            sound.play("gameover", .8f)
            recordRun()
            game.screen = GameOverScreen(game, player.score, player.coinsCollected, currentSteps())
            dispose()
            return
        }
        player.resetTo(checkpointX, level.tileSize)
        combo = 0
        comboTimer = 0f
    }

    private fun recordRun() {
        val prefs = Gdx.app.getPreferences("maryou_run_records")
        val bestScore = prefs.getInteger("bestScore", 0)
        val bestCoins = prefs.getInteger("bestCoins", 0)
        val bestSteps = prefs.getInteger("bestSteps", 0)
        prefs.putInteger("bestScore", max(bestScore, player.score))
        prefs.putInteger("bestCoins", max(bestCoins, player.coinsCollected))
        prefs.putInteger("bestSteps", max(bestSteps, currentSteps()))
        prefs.flush()
    }

    private fun updateCamera(delta: Float) {
        val half = viewport.worldWidth / 2f
        val targetX = max(half, player.bounds.x + player.bounds.width / 2f + 90f)
        cameraX += (targetX - cameraX) * (1f - exp(-7f * delta))
        cameraY += (WORLD_HEIGHT / 2f - cameraY) * (1f - exp(-5f * delta))
        val shakeX = if (screenShake > 0f) sin(animTime * 75f) * screenShake * 45f else 0f
        val shakeY = if (screenShake > 0f) sin(animTime * 91f) * screenShake * 20f else 0f
        camera.position.x = cameraX + shakeX
        camera.position.y = cameraY + shakeY
        camera.update()
    }

    private fun draw() {
        Gdx.gl.glClearColor(sky.r, sky.g, sky.b, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        viewport.apply()
        shapeRenderer.projectionMatrix = camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = worldPanel
        shapeRenderer.rect(camera.position.x - 420f, 0f, 840f, WORLD_HEIGHT)
        shapeRenderer.color = cloud
        val cloudOffset = sin(animTime * .12f) * 18f
        shapeRenderer.circle(camera.position.x - 230f + cloudOffset, 305f, 110f)
        shapeRenderer.circle(camera.position.x + 190f + cloudOffset * .6f, 285f, 135f)
        shapeRenderer.color = groundGlow
        shapeRenderer.circle(camera.position.x - 180f, 80f, 100f)
        shapeRenderer.circle(camera.position.x + 220f, 70f, 120f)

        for (hole in level.holeSpawns) if (visible(hole.bounds)) VectorArt.hole(shapeRenderer, hole.bounds)
        for (tile in level.solidTiles) if (visible(tile)) VectorArt.tile(shapeRenderer, tile, tile.y == 0f)
        for (pipe in level.pipeSpawns) if (visible(pipe)) VectorArt.pipe(shapeRenderer, pipe)
        for (monster in monsters) if (monster.alive && visible(monster.bounds)) VectorArt.monster(shapeRenderer, monster.bounds, monster.tier)
        for (coin in coins) if (visible(coin.bounds)) VectorArt.coin(shapeRenderer, coin.bounds, animTime * 5f)
        for (orb in powerUps) if (visible(orb.bounds)) VectorArt.powerUp(shapeRenderer, orb.bounds, animTime)
        for (p in particles) if (visible(Rectangle(p.x - p.radius, p.y - p.radius, p.radius * 2f, p.radius * 2f))) {
            shapeRenderer.color = Color(1f, 1f, 1f, (p.life / p.maxLife).coerceIn(0f, 1f))
            shapeRenderer.circle(p.x, p.y, p.radius)
        }

        val squash = if (squashTimer > 0f) 1.12f else if (stretchTimer > 0f) .88f else 1f
        val stretch = if (squashTimer > 0f) .88f else if (stretchTimer > 0f) 1.12f else 1f
        if (!player.isInvincible || (animTime % .2f) < .1f) {
            VectorArt.player(shapeRenderer, player.bounds, player.facingRight, player.size.name == "BIG", player.isDead, squash, stretch)
        }
        shapeRenderer.end()

        hudViewport.apply()
        shapeRenderer.projectionMatrix = hudCamera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = hudPanel
        shapeRenderer.rect(0f, 0f, hudViewport.worldWidth, 84f)
        shapeRenderer.color = hudLine
        shapeRenderer.rect(0f, 82f, hudViewport.worldWidth, 2f)
        shapeRenderer.color = Color(0f, 0f, 0f, .16f)
        shapeRenderer.rect(12f, 418f, hudViewport.worldWidth - 24f, 55f)

        val controlsEnabled = !paused && !showTutorial
        VectorArt.button(shapeRenderer, input.leftButton, controlsEnabled && input.isLeftPressed(), buttonColor, .30f)
        VectorArt.button(shapeRenderer, input.rightButton, controlsEnabled && input.isRightPressed(), buttonColor, .30f)
        VectorArt.button(shapeRenderer, input.jumpButton, controlsEnabled && input.isJumpPressed(), jumpButtonColor, .38f)
        VectorArt.pause(shapeRenderer, input.pauseButton, input.isPauseJustPressed())
        VectorArt.leftIcon(shapeRenderer, Rectangle(input.leftButton.x + 10f, input.leftButton.y + 8f, 30f, 30f))
        VectorArt.rightIcon(shapeRenderer, Rectangle(input.rightButton.x + 10f, input.rightButton.y + 8f, 30f, 30f))
        VectorArt.jumpIcon(shapeRenderer, Rectangle(input.jumpButton.x + 11f, input.jumpButton.y + 7f, 40f, 40f))

        if (paused || showTutorial) {
            shapeRenderer.color = overlay
            shapeRenderer.rect(0f, 0f, hudViewport.worldWidth, hudViewport.worldHeight)
            shapeRenderer.color = card
            shapeRenderer.rect(165f, 90f, 470f, 310f)
            shapeRenderer.color = Color(1f, 1f, 1f, .06f)
            shapeRenderer.rect(180f, 105f, 440f, 280f)
            if (paused) {
                VectorArt.button(shapeRenderer, input.restartButton, false, buttonColor, .88f)
                VectorArt.button(shapeRenderer, input.menuButton, false, buttonColor, .88f)
                VectorArt.restartIcon(shapeRenderer, Rectangle(267f, 161f, 42f, 42f))
                VectorArt.homeIcon(shapeRenderer, Rectangle(432f, 161f, 42f, 42f))
            }
        }
        shapeRenderer.end()

        batch.projectionMatrix = hudCamera.combined
        batch.begin()
        val steps = currentSteps()
        hud.render(batch, player, steps, combo, level.monsterTierForSteps(steps).name, level.stepsUntilNextTier(steps))
        font.data.setScale(.72f)
        font.color = Color(.72f, .82f, .92f, 1f)
        val progress = "AUTO-RUN  •  STEP $steps  •  SPEED ${player.currentRunSpeed(steps).toInt()}"
        textLayout.setText(font, progress)
        font.draw(batch, progress, 400f - textLayout.width / 2f, 40f)

        if (paused) {
            drawCentered("PAUSED", 334f, 44f, Color.WHITE)
            drawCentered("Take a breather — your run is safe.", 302f, 23f, Color(.72f, .82f, .92f, 1f))
            drawCenteredAt("RESTART", 326f, 184f, 23f)
            drawCenteredAt("MENU", 470f, 184f, 23f)
        } else if (showTutorial) {
            drawCentered("HOW TO RUN", 330f, 42f, Color.WHITE)
            drawCentered("AUTO-RUN", 292f, 24f, Color(.98f, .72f, .25f, 1f))
            drawCentered("Steer left / right • tap jump to clear hazards", 260f, 20f, Color(.78f, .88f, .95f, 1f))
            drawCentered("Tap anywhere to dismiss", 212f, 18f, Color(.60f, .72f, .82f, 1f))
        }

        if (transitionAlpha > 0f) {
            shapeRenderer.projectionMatrix = hudCamera.combined
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            shapeRenderer.color = Color(0f, 0f, 0f, transitionAlpha)
            shapeRenderer.rect(0f, 0f, hudViewport.worldWidth, hudViewport.worldHeight)
            shapeRenderer.end()
        }
        batch.end()
    }

    private fun visible(rect: Rectangle): Boolean = rect.x + rect.width >= camera.position.x - 560f && rect.x <= camera.position.x + 560f

    private fun drawCentered(text: String, baselineY: Float, size: Float, color: Color) {
        font.data.setScale(size / 24f)
        font.color = color
        textLayout.setText(font, text)
        font.draw(batch, text, 400f - textLayout.width / 2f, baselineY)
    }

    private fun drawCenteredAt(text: String, centerX: Float, baselineY: Float, size: Float) {
        font.data.setScale(size / 24f)
        font.color = Color.WHITE
        textLayout.setText(font, text)
        font.draw(batch, text, centerX - textLayout.width / 2f, baselineY)
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, false)
        val controlHeight = (height * .17f).toInt().coerceIn(78, height / 3)
        viewport.setScreenBounds(0, controlHeight, width, (height - controlHeight).coerceAtLeast(1))
        hudViewport.update(width, height, true)
    }

    override fun pause() { paused = true }
    override fun resume() { paused = true }
    override fun hide() {}
    override fun dispose() {
        shapeRenderer.dispose()
        batch.dispose()
        font.dispose()
        sound.dispose()
    }
}
