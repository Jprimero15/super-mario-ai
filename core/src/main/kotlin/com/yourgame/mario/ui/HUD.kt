package com.yourgame.mario.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.yourgame.mario.entities.Player

/** Compact card-style HUD using icon-like glyphs and crisp generated fonts. */
class HUD(private val font: BitmapFont) {
    private val layout = GlyphLayout()
    private val primary = Color(.97f, .99f, 1f, 1f)
    private val secondary = Color(.72f, .84f, .94f, 1f)

    fun render(batch: SpriteBatch, player: Player, steps: Int, combo: Int, nextTier: String, untilTier: Int) {
        drawStat(batch, "●", player.score.toString().padStart(5, '0'), 108f, primary)
        drawStat(batch, "♥", player.lives.toString(), 300f, primary)
        drawStat(batch, "→", steps.toString(), 492f, primary)
        drawStat(batch, "✦", player.coinsCollected.toString(), 684f, secondary)
        if (combo > 1) {
            drawCentered(batch, "COMBO x$combo", 400f, 398f, Color(.98f, .72f, .25f, 1f), .82f)
        }
        val tierMessage = if (untilTier > 0) "$nextTier TIER IN $untilTier" else "MAX TIER"
        drawCentered(batch, tierMessage, 400f, 370f, Color(.72f, .84f, .94f, .92f), .62f)
    }

    private fun drawStat(batch: SpriteBatch, icon: String, value: String, centerX: Float, color: Color) {
        font.data.setScale(.78f)
        font.color = color
        val text = "$icon  $value"
        layout.setText(font, text)
        font.draw(batch, text, centerX - layout.width / 2f, 452f)
    }

    private fun drawCentered(batch: SpriteBatch, text: String, centerX: Float, baselineY: Float, color: Color, scale: Float) {
        font.data.setScale(scale)
        font.color = color
        layout.setText(font, text)
        font.draw(batch, text, centerX - layout.width / 2f, baselineY)
    }
}
