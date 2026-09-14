package com.yourgame.mario

import com.badlogic.gdx.Game
import com.yourgame.mario.screens.MainMenuScreen

/** Root LibGDX Game instance that owns the active screen. */
class MaryouGame : Game() {
    override fun create() {
        setScreen(MainMenuScreen(this))
    }
}
