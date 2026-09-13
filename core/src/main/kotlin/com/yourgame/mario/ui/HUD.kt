package com.yourgame.mario.ui

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.yourgame.mario.entities.Player

/** Draws score/lives/time. Caller must set batch.projectionMatrix to a screen-space camera first. */
class HUD(private val font: BitmapFont) {
    fun render(batch: SpriteBatch, player: Player, timeRemaining: Int) {
        font.draw(batch, "SCORE: ${player.score}", 16f, 470f)
        font.draw(batch, "LIVES: ${player.lives}", 260f, 470f)
        font.draw(batch, "TIME: $timeRemaining", 460f, 470f)
        font.draw(batch, "SIZE: ${player.size}", 620f, 470f)
    }
}
