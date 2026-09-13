package com.yourgame.mario.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.yourgame.mario.entities.Player

/** Clean, consistently aligned HUD for the fixed 800x480 game viewport. */
class HUD(private val font: BitmapFont) {
    private val layout = GlyphLayout()
    private val primary = Color.WHITE
    private val secondary = Color(.72f, .82f, .92f, 1f)

    fun render(batch: SpriteBatch, player: Player, timeRemaining: Int) {
        drawCentered(batch, "SCORE  ${player.score.toString().padStart(5, '0')}", 112f, 449f, primary, 1.02f)
        drawCentered(batch, "LIVES  ${player.lives}", 300f, 449f, primary, 1.02f)
        drawCentered(batch, "TIME  ${timeRemaining.toString().padStart(3, '0')}", 488f, 449f, primary, 1.02f)
        drawCentered(batch, if (player.size.name == "BIG") "POWER  BIG" else "POWER  SMALL", 688f, 449f, secondary, .96f)
    }

    private fun drawCentered(batch: SpriteBatch, text: String, centerX: Float, y: Float, color: Color, scale: Float) {
        font.data.setScale(scale)
        font.color = color
        layout.setText(font, text)
        font.draw(batch, text, centerX - layout.width / 2f, y)
    }
}
