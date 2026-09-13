package com.yourgame.mario.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.Viewport

/** Responsive multi-touch controller for either Android landscape orientation. */
class TouchInputController(private val viewport: Viewport) : InputController {
    val leftButton=Rectangle(20f,18f,62f,62f)
    val rightButton=Rectangle(92f,18f,62f,62f)
    val jumpButton=Rectangle(710f,18f,68f,68f)
    val pauseButton=Rectangle(746f,424f,34f,34f)
    val restartButton=Rectangle(275f,160f,110f,50f)
    val menuButton=Rectangle(415f,160f,110f,50f)
    private var leftDown=false;private var rightDown=false;private var jumpDown=false;private var pauseDown=false;private var restartDown=false;private var menuDown=false
    private var jumpWasDown=false;private var pauseWasDown=false;private var restartWasDown=false;private var menuWasDown=false
    private val touchPoint=Vector2()
    fun poll(){
        leftDown=false;rightDown=false;jumpDown=false;pauseDown=false;restartDown=false;menuDown=false
        for(pointer in 0 until 10){
            if(!Gdx.input.isTouched(pointer))continue
            touchPoint.set(Gdx.input.getX(pointer).toFloat(),Gdx.input.getY(pointer).toFloat());viewport.unproject(touchPoint)
            if(leftButton.contains(touchPoint))leftDown=true;if(rightButton.contains(touchPoint))rightDown=true;if(jumpButton.contains(touchPoint))jumpDown=true;if(pauseButton.contains(touchPoint))pauseDown=true;if(restartButton.contains(touchPoint))restartDown=true;if(menuButton.contains(touchPoint))menuDown=true
        }
    }
    fun endFrame(){jumpWasDown=jumpDown;pauseWasDown=pauseDown;restartWasDown=restartDown;menuWasDown=menuDown}
    override fun isLeftPressed()=leftDown
    override fun isRightPressed()=rightDown
    override fun isJumpPressed()=jumpDown
    override fun isJumpJustPressed()=jumpDown&&!jumpWasDown
    override fun isPauseJustPressed()=pauseDown&&!pauseWasDown
    fun isRestartJustPressed()=restartDown&&!restartWasDown
    fun isMenuJustPressed()=menuDown&&!menuWasDown
}
