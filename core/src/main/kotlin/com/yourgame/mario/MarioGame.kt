package com.yourgame.mario

import com.badlogic.gdx.Game
import com.yourgame.mario.screens.MainMenuScreen

/**
 * Root LibGDX Game instance. Owns nothing but the currently active Screen;
 * all real logic lives in the screens/entities/physics packages.
 */
class MarioGame : Game() {
    override fun create() {
        setScreen(MainMenuScreen(this))
    }
}
