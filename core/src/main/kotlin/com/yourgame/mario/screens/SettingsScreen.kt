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

class SettingsScreen(private val game: MaryouGame) : Screen {
    private val camera = OrthographicCamera()
    private val viewport: Viewport = FitViewport(800f, 480f, camera)
    private val batch = SpriteBatch()
    private val shapes = ShapeRenderer()
    private val title: BitmapFont = FontFactory.create(46)
    private val font: BitmapFont = FontFactory.create(24)
    private val small: BitmapFont = FontFactory.create(18)
    private val layout = GlyphLayout()
    private val prefs = Gdx.app.getPreferences("maryou_settings")
    private val back = Rectangle(25f, 400f, 120f, 52f)
    private val mute = Rectangle(230f, 295f, 340f, 58f)
    private val handed = Rectangle(230f, 210f, 340f, 58f)
    private val volumeTrack = Rectangle(235f, 145f, 330f, 12f)
    private var fade = 1f

    override fun show() {
        camera.position.set(400f, 240f, 0f)
        camera.update()
    }

    override fun render(delta: Float) {
        fade = (fade - delta * 3f).coerceAtLeast(0f)
        if (Gdx.input.justTouched()) {
            val p = viewport.unproject(Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat()))
            when {
                back.contains(p) -> { game.screen = MainMenuScreen(game); dispose(); return }
                mute.contains(p) -> prefs.putBoolean("sfxMuted", !prefs.getBoolean("sfxMuted", false)).flush()
                handed.contains(p) -> prefs.putBoolean("leftHanded", !prefs.getBoolean("leftHanded", false)).flush()
                volumeTrack.contains(p) -> {
                    val volume = ((p.x - volumeTrack.x) / volumeTrack.width).coerceIn(0f, 1f)
                    prefs.putFloat("sfxVolume", volume).flush()
                }
            }
        }

        val muted = prefs.getBoolean("sfxMuted", false)
        val leftHanded = prefs.getBoolean("leftHanded", false)
        val volume = prefs.getFloat("sfxVolume", .45f).coerceIn(0f, 1f)

        Gdx.gl.glClearColor(.025f, .045f, .075f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        shapes.projectionMatrix = camera.combined
        shapes.begin(ShapeRenderer.ShapeType.Filled)
        shapes.color = Color(.08f, .14f, .21f, 1f)
        shapes.rect(0f, 0f, 800f, 480f)
        shapes.color = Color(.18f, .38f, .52f, .28f)
        shapes.circle(90f, 420f, 150f)
        shapes.circle(720f, 60f, 175f)
        shapes.color = Color(.03f, .06f, .10f, .96f)
        shapes.rect(145f, 45f, 510f, 390f)
        shapes.color = Color(1f, 1f, 1f, .05f)
        shapes.rect(160f, 60f, 480f, 360f)
        VectorArt.button(shapes, back, false, Color(.10f, .16f, .22f, 1f), .65f)
        VectorArt.button(shapes, mute, false, Color(.10f, .16f, .22f, 1f), .75f)
        VectorArt.button(shapes, handed, false, Color(.10f, .16f, .22f, 1f), .75f)
        shapes.color = Color(1f, 1f, 1f, .10f)
        shapes.rect(volumeTrack.x, volumeTrack.y, volumeTrack.width, volumeTrack.height)
        shapes.color = Color(.22f, .70f, .44f, 1f)
        shapes.rect(volumeTrack.x, volumeTrack.y, volumeTrack.width * volume, volumeTrack.height)
        shapes.end()

        batch.projectionMatrix = camera.combined
        batch.begin()
        drawCentered(title, "SETTINGS", 370f, Color.WHITE)
        drawCentered(font, "Sound effects", 322f, Color(.78f, .91f, 1f, 1f))
        drawCentered(small, "Volume  ${(volume * 100f).toInt()}%", 270f, Color(.68f, .80f, .89f, 1f))
        drawCentered(small, if (muted) "Muted" else "Enabled", 310f, Color(.98f, .72f, .25f, 1f))
        drawCentered(font, if (muted) "SFX: OFF" else "SFX: ON", 316f, Color.WHITE)
        drawCentered(font, if (leftHanded) "Controls: LEFT-HANDED" else "Controls: RIGHT-HANDED", 231f, Color.WHITE)
        drawCentered(small, "Tap the button to swap movement/jump sides", 185f, Color(.62f, .74f, .84f, 1f))
        drawCenteredAt(font, "BACK", 85f, 432f, Color.WHITE)
        if (fade > 0f) {
            // The fade is intentionally left to the next frame after batch flush.
        }
        batch.end()
    }

    private fun drawCentered(font: BitmapFont, text: String, y: Float, color: Color) {
        font.color = color
        font.data.setScale(1f)
        layout.setText(font, text)
        font.draw(batch, text, 400f - layout.width / 2f, y)
    }

    private fun drawCenteredAt(font: BitmapFont, text: String, x: Float, y: Float, color: Color) {
        font.color = color
        layout.setText(font, text)
        font.draw(batch, text, x - layout.width / 2f, y)
    }

    override fun resize(width: Int, height: Int) = viewport.update(width, height, true)
    override fun pause() {}
    override fun resume() {}
    override fun hide() {}
    override fun dispose() { batch.dispose(); shapes.dispose(); title.dispose(); font.dispose(); small.dispose() }
}
