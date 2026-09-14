package com.yourgame.mario.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator

/** Creates crisp fonts at their target pixel size instead of scaling the default bitmap font. */
object FontFactory {
    fun create(size: Int, shadow: Boolean = true): BitmapFont {
        val fontFile = Gdx.files.absolute("/system/fonts/Roboto-Regular.ttf")
        return try {
            val generator = FreeTypeFontGenerator(fontFile)
            val parameter = FreeTypeFontGenerator.FreeTypeFontParameter().apply {
                this.size = size
                this.minFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear
                this.magFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear
                if (shadow) {
                    shadowOffsetX = 2
                    shadowOffsetY = 2
                    shadowColor = com.badlogic.gdx.graphics.Color(0f, 0f, 0f, 0.35f)
                }
            }
            val font = generator.generateFont(parameter)
            generator.dispose()
            font
        } catch (_: Throwable) {
            // Android ships with Roboto; BitmapFont is only a defensive fallback for unusual test environments.
            BitmapFont().also {
                it.data.setScale(size / 15f)
            }
        }
    }
}
