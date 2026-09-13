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

class MainMenuScreen(private val game: MarioGame) : Screen {

    private val camera = OrthographicCamera()
    private val viewport: Viewport = FitViewport(800f, 480f, camera)
    private val batch = SpriteBatch()
    private val font = BitmapFont().apply { data.setScale(2f) }

    override fun show() {
        camera.position.set(viewport.worldWidth / 2f, viewport.worldHeight / 2f, 0f)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        if (Gdx.input.justTouched()) {
            game.screen = PlayScreen(game)
            dispose()
            return
        }

        camera.update()
        batch.projectionMatrix = camera.combined
        batch.begin()
        font.draw(batch, "KOTLIN MARIO-LIKE", 220f, 300f)
        font.draw(batch, "Tap anywhere to start", 260f, 240f)
        font.draw(batch, "On-screen buttons: < > move, JUMP to jump, || to pause", 30f, 190f)
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
