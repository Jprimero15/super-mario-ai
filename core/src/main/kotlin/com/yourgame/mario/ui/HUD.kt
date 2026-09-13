package com.yourgame.mario.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.yourgame.mario.entities.Player

/** Compact game HUD with a modern card-like information strip. */
class HUD(private val font: BitmapFont) {
    fun render(batch: SpriteBatch, player: Player, timeRemaining: Int) {
        font.setColor(Color.WHITE)
        font.draw(batch, "SCORE  ${player.score.toString().padStart(5, '0')}", 24f, 452f)
        font.draw(batch, "♥ ${player.lives}", 285f, 452f)
        font.draw(batch, "TIME  ${timeRemaining.toString().padStart(3, '0')}", 500f, 452f)
        font.setColor(Color(.78f,.86f,.95f,1f))
        font.draw(batch, if (player.size.name == "BIG") "POWER: BIG" else "POWER: SMALL", 650f, 452f)
    }
}
