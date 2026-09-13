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
import com.yourgame.mario.MarioGame
import com.yourgame.mario.entities.Coin
import com.yourgame.mario.entities.Player
import com.yourgame.mario.input.TouchInputController
import com.yourgame.mario.physics.CollisionHandler
import com.yourgame.mario.physics.Physics
import com.yourgame.mario.ui.HUD
import com.yourgame.mario.ui.VectorArt
import com.yourgame.mario.world.Level
import kotlin.math.max

class PlayScreen(private val game: MarioGame) : Screen {
    companion object {
        const val VIEWPORT_WIDTH = 800f
        const val WORLD_HEIGHT = 400f
        const val UI_HEIGHT = 480f
    }

    private val level = Level.level1()
    private val collision = CollisionHandler(level.solidTiles, level.tileSize)
    private val camera = OrthographicCamera()
    private val viewport: Viewport = FitViewport(VIEWPORT_WIDTH, WORLD_HEIGHT, camera)
    private val hudCamera = OrthographicCamera()
    private val hudViewport: Viewport = FitViewport(VIEWPORT_WIDTH, UI_HEIGHT, hudCamera)
    private val input = TouchInputController(hudViewport)
    private val shapeRenderer = ShapeRenderer()
    private val batch = SpriteBatch()
    private val font = BitmapFont()
    private val hud = HUD(font)
    private val textLayout = GlyphLayout()
    private val player = Player(level.playerStart.x, level.playerStart.y)
    private val coins = mutableListOf<Coin>()
    private var appliedSolidCount = level.solidTiles.size
    private var spawnedCoinCount = 0
    private var paused = false
    private var elapsedTime = 0f
    private var animTime = 0f
    private var checkpointX = level.playerStart.x

    override fun show() {
        hudCamera.position.set(hudViewport.worldWidth / 2f, hudViewport.worldHeight / 2f, 0f)
        hudCamera.update()
        syncWorld()
    }

    override fun render(delta: Float) {
        val d = delta.coerceIn(0f, 1f / 30f)
        input.poll()
        if (input.isPauseJustPressed()) paused = !paused
        if (paused) {
            if (input.isRestartJustPressed()) { restart(); return }
            if (input.isMenuJustPressed()) { game.screen = MainMenuScreen(game); dispose(); return }
        }
        if (!paused) update(d)
        draw()
        input.endFrame()
    }

    private fun restart() { game.screen = PlayScreen(game); dispose() }

    private fun update(delta: Float) {
        elapsedTime += delta
        animTime += delta

        // Keep generating several screens ahead so the world never reaches an endpoint.
        level.ensureGeneratedThrough(player.bounds.x + 1800f)
        syncWorld()

        // Move the respawn point forward as the player makes meaningful progress.
        val nextCheckpoint = (player.bounds.x / 1024f).toInt() * 1024f + 64f
        if (nextCheckpoint > checkpointX) checkpointX = nextCheckpoint

        player.updatePhysics(delta, input, collision)
        handleCoinCollisions()

        if (player.bounds.y < -220f && !player.isDead) player.killInstantly()
        if (player.isDead) {
            player.velocity.y += Physics.GRAVITY * delta
            player.bounds.y += player.velocity.y * delta
            if (player.bounds.y < -420f) respawn()
        }
        updateCamera()
    }

    private fun syncWorld() {
        if (appliedSolidCount < level.solidTiles.size) {
            collision.addSolids(level.solidTiles.subList(appliedSolidCount, level.solidTiles.size))
            appliedSolidCount = level.solidTiles.size
        }
        while (spawnedCoinCount < level.coinSpawns.size) {
            val spawn = level.coinSpawns[spawnedCoinCount++]
            coins += Coin(spawn.x, spawn.y, spawn.width, spawn.height)
        }
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

    private fun respawn() {
        player.lives -= 1
        if (player.lives <= 0) {
            game.screen = GameOverScreen(game, player.score)
            dispose()
            return
        }
        player.resetTo(checkpointX, level.tileSize)
    }

    private fun updateCamera() {
        val half = viewport.worldWidth / 2f
        camera.position.x = max(half, player.bounds.x + player.bounds.width / 2f)
        camera.position.y = WORLD_HEIGHT / 2f
        camera.update()
    }

    private fun draw() {
        Gdx.gl.glClearColor(.055f, .09f, .14f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // World viewport occupies the upper portion of the screen; the lower band is reserved for controls.
        viewport.apply()
        shapeRenderer.projectionMatrix = camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(.30f, .60f, .87f, 1f)
        shapeRenderer.rect(camera.position.x - 400f, 0f, 800f, WORLD_HEIGHT)
        shapeRenderer.color = Color(.43f, .72f, .90f, .26f)
        shapeRenderer.circle(camera.position.x - 220f, 300f, 105f)
        shapeRenderer.circle(camera.position.x + 190f, 275f, 135f)
        shapeRenderer.color = Color(.20f, .48f, .36f, .90f)
        shapeRenderer.circle(camera.position.x - 180f, 82f, 95f)
        shapeRenderer.circle(camera.position.x + 220f, 72f, 115f)
        for (tile in level.solidTiles) {
            if (tile.x + tile.width < camera.position.x - 500f || tile.x > camera.position.x + 500f) continue
            VectorArt.tile(shapeRenderer, tile, tile.y == 0f)
        }
        for (pipe in level.pipeSpawns) {
            if (pipe.x + pipe.width < camera.position.x - 500f || pipe.x > camera.position.x + 500f) continue
            VectorArt.pipe(shapeRenderer, pipe)
        }
        for (coin in coins) {
            if (coin.bounds.x + coin.bounds.width < camera.position.x - 500f || coin.bounds.x > camera.position.x + 500f) continue
            VectorArt.coin(shapeRenderer, coin.bounds, animTime * 5f)
        }
        val visible = !player.isInvincible || (animTime % .2f) < .1f
        if (visible) VectorArt.player(shapeRenderer, player.bounds, player.facingRight, player.size.name == "BIG", player.isDead)
        shapeRenderer.end()

        // Full-screen UI viewport: controls are physically below the gameplay viewport.
        hudViewport.apply()
        shapeRenderer.projectionMatrix = hudCamera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(.04f, .07f, .11f, .84f)
        shapeRenderer.rect(0f, 0f, 800f, 82f)
        shapeRenderer.color = Color(1f, 1f, 1f, .045f)
        shapeRenderer.rect(0f, 82f, 800f, 2f)
        shapeRenderer.color = Color(0f, 0f, 0f, .16f)
        shapeRenderer.rect(10f, 425f, 780f, 47f)
        shapeRenderer.color = Color(1f, 1f, 1f, .045f)
        shapeRenderer.rect(14f, 429f, 183f, 39f);shapeRenderer.rect(203f, 429f, 183f, 39f)
        shapeRenderer.rect(392f, 429f, 183f, 39f);shapeRenderer.rect(581f, 429f, 183f, 39f)

        VectorArt.button(shapeRenderer, input.leftButton, input.isLeftPressed(), Color(.08f,.14f,.20f,1f), .28f)
        VectorArt.button(shapeRenderer, input.rightButton, input.isRightPressed(), Color(.08f,.14f,.20f,1f), .28f)
        VectorArt.button(shapeRenderer, input.jumpButton, input.isJumpPressed(), Color(.86f,.22f,.12f,1f), .32f)
        VectorArt.pause(shapeRenderer, input.pauseButton, paused)
        VectorArt.leftIcon(shapeRenderer, Rectangle(input.leftButton.x+10f,input.leftButton.y+8f,30f,30f))
        VectorArt.rightIcon(shapeRenderer, Rectangle(input.rightButton.x+10f,input.rightButton.y+8f,30f,30f))
        VectorArt.jumpIcon(shapeRenderer, Rectangle(input.jumpButton.x+11f,input.jumpButton.y+7f,40f,40f))

        if (paused) {
            shapeRenderer.color=Color(.02f,.035f,.06f,.76f);shapeRenderer.rect(0f,0f,800f,480f)
            shapeRenderer.color=Color(.07f,.11f,.16f,.98f);shapeRenderer.rect(210f,105f,380f,285f)
            shapeRenderer.color=Color(1f,1f,1f,.06f);shapeRenderer.rect(224f,119f,352f,257f)
            VectorArt.button(shapeRenderer,input.restartButton,input.isRestartJustPressed(),Color(.10f,.24f,.34f,1f),.82f)
            VectorArt.button(shapeRenderer,input.menuButton,input.isMenuJustPressed(),Color(.10f,.24f,.34f,1f),.82f)
            VectorArt.restartIcon(shapeRenderer,Rectangle(267f,161f,42f,42f));VectorArt.homeIcon(shapeRenderer,Rectangle(432f,161f,42f,42f))
        }
        shapeRenderer.end()

        batch.projectionMatrix = hudCamera.combined
        batch.begin()
        hud.render(batch, player, elapsedTime.toInt())
        if (paused) {
            drawCentered("PAUSED", 335f, 1.7f, Color.WHITE)
            drawCentered("Game paused", 305f, 1.05f, Color(.72f,.82f,.92f,1f))
            drawCenteredAt("RESTART", 326f, 185f, 1.05f)
            drawCenteredAt("MENU", 470f, 185f, 1.05f)
        }
        batch.end()
    }

    private fun drawCentered(text:String,y:Float,scale:Float,color:Color){font.data.setScale(scale);font.color=color;textLayout.setText(font,text);font.draw(batch,text,400f-textLayout.width/2f,y)}
    private fun drawCenteredAt(text:String,centerX:Float,y:Float,scale:Float){font.data.setScale(scale);font.color=Color.WHITE;textLayout.setText(font,text);font.draw(batch,text,centerX-textLayout.width/2f,y)}

    override fun resize(width:Int,height:Int){
        viewport.update(width,height,false)
        val controlHeight=(height*.17f).toInt().coerceIn(78,height/3)
        viewport.setScreenBounds(0,controlHeight,width,(height-controlHeight).coerceAtLeast(1))
        hudViewport.update(width,height,true)
    }
    override fun pause(){paused=true}
    override fun resume(){}
    override fun hide(){}
    override fun dispose(){shapeRenderer.dispose();batch.dispose();font.dispose()}
}
