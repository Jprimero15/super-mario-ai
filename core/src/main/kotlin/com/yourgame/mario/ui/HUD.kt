package com.yourgame.mario.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.yourgame.mario.entities.Player

/** Compact, evenly spaced HUD with measured text so labels never collide. */
class HUD(private val font: BitmapFont) {
    private val layout = GlyphLayout()
    private val primary = Color(.97f, .99f, 1f, 1f)
    private val secondary = Color(.72f, .84f, .94f, 1f)

    fun render(batch: SpriteBatch, player: Player, steps: Int) {
        drawCentered(batch, "SCORE  ${player.score.toString().padStart(5, '0')}", 108f, 452f, primary, .92f)
        drawCentered(batch, "LIVES  ${player.lives}", 302f, 452f, primary, .92f)
        drawCentered(batch, "STEPS  $steps", 496f, 452f, primary, .92f)
        drawCentered(batch, "COINS  ${player.coinsCollected}", 690f, 452f, secondary, .92f)
    }

    private fun drawCentered(batch: SpriteBatch, text: String, centerX: Float, baselineY: Float, color: Color, scale: Float) {
        font.data.setScale(scale)
        font.color = color
        layout.setText(font, text)
        font.draw(batch, text, centerX - layout.width / 2f, baselineY)
    }
}
