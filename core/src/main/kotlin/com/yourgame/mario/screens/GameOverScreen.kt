package com.yourgame.mario.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.yourgame.mario.MarioGame

class GameOverScreen(private val game: MarioGame, private val finalScore: Int) : Screen {

    private val camera = OrthographicCamera()
    private val viewport: Viewport = FitViewport(800f, 480f, camera)
    private val batch = SpriteBatch()
    private val font = BitmapFont().apply { data.setScale(2f) }

    override fun show() {
        camera.position.set(viewport.worldWidth / 2f, viewport.worldHeight / 2f, 0f)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        if (Gdx.input.justTouched()) {
            game.screen = MainMenuScreen(game)
            dispose()
            return
        }

        camera.update()
        batch.projectionMatrix = camera.combined
        batch.begin()
        font.draw(batch, "GAME OVER", 300f, 300f)
        font.draw(batch, "Final Score: $finalScore", 260f, 240f)
        font.draw(batch, "Tap to return to menu", 240f, 180f)
        batch.end()
    }

    override fun resize(width: Int, height: Int) = viewport.update(width, height, true)
    override fun pause() {}
    override fun resume() {}
    override fun hide() {}
    override fun dispose() {
        batch.dispose()
        font.dispose()
    }
}
