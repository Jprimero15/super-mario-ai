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
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.yourgame.mario.MaryouGame
import com.yourgame.mario.ui.FontFactory
import com.yourgame.mario.ui.VectorArt
import kotlin.math.sin

class MainMenuScreen(private val game: MaryouGame) : Screen {
    private val camera = OrthographicCamera()
    private val viewport: Viewport = FitViewport(800f, 480f, camera)
    private val batch = SpriteBatch()
    private val shapes = ShapeRenderer()
    private val layout = GlyphLayout()
    private val title: BitmapFont = FontFactory.create(54)
    private val font: BitmapFont = FontFactory.create(24)
    private val small: BitmapFont = FontFactory.create(18)
    private val play = Rectangle(250f, 145f, 300f, 72f)
    private val settings = Rectangle(305f, 82f, 190f, 46f)
    private var time = 0f
    private var fade = 1f
    private var playPulse = 0f

    override fun show() {
        camera.position.set(400f, 240f, 0f)
        camera.update()
    }

    override fun render(delta: Float) {
        time += delta
        fade = (fade - delta * 3.5f).coerceAtLeast(0f)
        playPulse = (playPulse - delta * 5f).coerceAtLeast(0f)

        var playTouched = false
        var settingsTouched = false
        if (Gdx.input.justTouched()) {
            val p = viewport.unproject(Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat()))
            playTouched = play.contains(p)
            settingsTouched = settings.contains(p)
            if (playTouched) playPulse = .16f
        }

        Gdx.gl.glClearColor(.025f, .045f, .075f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        shapes.projectionMatrix = camera.combined
        shapes.begin(ShapeRenderer.ShapeType.Filled)
        shapes.color = Color(.10f, .28f, .46f, 1f)
        shapes.rect(0f, 0f, 800f, 480f)
        val drift = sin(time * .35f) * 15f
        shapes.color = Color(.23f, .50f, .68f, .55f)
        shapes.circle(65f + drift, 45f, 150f)
        shapes.circle(745f - drift, 55f, 175f)
        shapes.color = Color(.17f, .42f, .32f, .72f)
        shapes.circle(145f - drift * .45f, 75f, 105f)
        shapes.circle(665f + drift * .40f, 85f, 115f)
        shapes.color = Color(.035f, .065f, .105f, .90f)
        shapes.rect(150f, 55f, 500f, 370f)
        shapes.color = Color(1f, 1f, 1f, .05f)
        shapes.rect(166f, 71f, 468f, 338f)
        VectorArt.coin(shapes, Rectangle(607f, 326f, 34f, 34f), time * 3f)
        VectorArt.player(shapes, Rectangle(92f, 60f, 70f, 70f), true, false, false, 1f, 1f)
        val playScale = if (playPulse > 0f) .97f else 1f
        val playRect = Rectangle(play.x + play.width * (1f - playScale) / 2f, play.y + play.height * (1f - playScale) / 2f, play.width * playScale, play.height * playScale)
        VectorArt.button(shapes, playRect, playPulse > 0f, Color(.90f, .18f, .11f, 1f), .92f)
        VectorArt.button(shapes, settings, false, Color(.10f, .16f, .22f, 1f), .72f)
        shapes.color = Color.WHITE
        shapes.triangle(305f, 165f, 305f, 195f, 331f, 180f)
        shapes.end()

        batch.projectionMatrix = camera.combined
        batch.begin()
        drawCentered(title, "MARYOU AI", 370f, Color.WHITE)
        drawCentered(font, "ENDLESS RUN", 326f, Color(.78f, .91f, 1f, 1f))
        drawCentered(small, "AUTO-RUN  •  JUMP  •  COLLECT  •  SURVIVE", 300f, Color(.68f, .80f, .89f, 1f))
        drawCentered(font, "PLAY", 181f, Color.WHITE)
        drawCentered(small, "SETTINGS", 112f, Color(.88f, .94f, 1f, 1f))

        val prefs = Gdx.app.getPreferences("maryou_run_records")
        drawCentered(small, "BEST ${prefs.getInteger("bestScore", 0)}   •   COINS ${prefs.getInteger("bestCoins", 0)}   •   STEPS ${prefs.getInteger("bestSteps", 0)}", 87f, Color(.72f, .83f, .92f, 1f))
        drawCentered(small, "Records are saved on this device", 62f, Color(.55f, .67f, .78f, 1f))
        batch.end()

        if (fade > 0f) {
            shapes.projectionMatrix = camera.combined
            shapes.begin(ShapeRenderer.ShapeType.Filled)
            shapes.color = Color(0f, 0f, 0f, fade)
            shapes.rect(0f, 0f, 800f, 480f)
            shapes.end()
        }

        if (playTouched) { game.screen = PlayScreen(game); dispose() }
        else if (settingsTouched) { game.screen = SettingsScreen(game); dispose() }
    }

    private fun drawCentered(font: BitmapFont, text: String, y: Float, color: Color) {
        font.color = color
        font.data.setScale(1f)
        layout.setText(font, text)
        font.draw(batch, text, 400f - layout.width / 2f, y)
    }

    override fun resize(width: Int, height: Int) = viewport.update(width, height, true)
    override fun pause() {}
    override fun resume() {}
    override fun hide() {}
    override fun dispose() { batch.dispose(); shapes.dispose(); title.dispose(); font.dispose(); small.dispose() }
}
