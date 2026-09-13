package com.yourgame.mario.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.yourgame.mario.entities.Player

/** Consistent four-column HUD for the 800x480 UI viewport. */
class HUD(private val font: BitmapFont) {
    private val layout = GlyphLayout()
    private val primary = Color(.96f, .98f, 1f, 1f)
    private val secondary = Color(.72f, .82f, .91f, 1f)

    fun render(batch: SpriteBatch, player: Player, timeRemaining: Int) {
        drawCentered(batch, "SCORE  ${player.score.toString().padStart(5, '0')}", 105f, 452f, primary, 1.0f)
        drawCentered(batch, "LIVES  ${player.lives}", 295f, 452f, primary, 1.0f)
        drawCentered(batch, "TIME  ${timeRemaining.toString().padStart(3, '0')}", 485f, 452f, primary, 1.0f)
        drawCentered(batch, if (player.size.name == "BIG") "POWER  BIG" else "POWER  SMALL", 675f, 452f, secondary, .94f)
    }

    private fun drawCentered(batch: SpriteBatch, text: String, centerX: Float, baselineY: Float, color: Color, scale: Float) {
        font.data.setScale(scale)
        font.color = color
        layout.setText(font, text)
        font.draw(batch, text, centerX - layout.width / 2f, baselineY)
    }
}
