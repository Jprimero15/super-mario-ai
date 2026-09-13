package com.yourgame.mario.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.yourgame.mario.MarioGame
import com.yourgame.mario.ui.VectorArt

class GameOverScreen(private val game: MarioGame, private val finalScore: Int) : Screen {
    private val camera=OrthographicCamera();private val viewport:Viewport=FitViewport(800f,480f,camera)
    private val batch=SpriteBatch();private val shapes=ShapeRenderer();private val font=BitmapFont().apply{data.setScale(1.25f)}
    private val restart=Rectangle(250f,150f,135f,58f);private val menu=Rectangle(415f,150f,135f,58f)

    override fun show(){camera.position.set(400f,240f,0f)}
    override fun render(delta:Float){
        Gdx.gl.glClearColor(.04f,.06f,.10f,1f);Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);camera.update()
        var restartTouch=false;var menuTouch=false
        if(Gdx.input.justTouched()){
            val x=Gdx.input.getX().toFloat();val y=Gdx.input.getY().toFloat();val p=viewport.unproject(com.badlogic.gdx.math.Vector2(x,y))
            restartTouch=restart.contains(p);menuTouch=menu.contains(p)
        }
        shapes.projectionMatrix=camera.combined;shapes.begin(ShapeRenderer.ShapeType.Filled)
        shapes.color=Color(.08f,.12f,.18f,1f);shapes.rect(0f,0f,800f,480f)
        shapes.color=Color(.25f,.12f,.16f,1f);shapes.circle(110f,80f,120f);shapes.circle(700f,100f,145f)
        shapes.color=Color(.10f,.14f,.20f,.96f);shapes.rect(185f,75f,430f,325f)
        VectorArt.button(shapes,restart,false,Color(.12f,.22f,.32f,1f),.92f);VectorArt.button(shapes,menu,false,Color(.12f,.22f,.32f,1f),.92f)
        VectorArt.restartIcon(shapes,Rectangle(268f,158f,42f,42f));VectorArt.homeIcon(shapes,Rectangle(433f,158f,42f,42f));shapes.end()

        batch.projectionMatrix=camera.combined;batch.begin()
        font.setColor(Color(.98f,.35f,.28f,1f));font.getData().setScale(2.2f);font.draw(batch,"RUN OVER",305f,322f)
        font.getData().setScale(1.35f);font.setColor(Color.WHITE);font.draw(batch,"Final score  $finalScore",320f,270f)
        font.getData().setScale(1.0f);font.setColor(Color(.68f,.76f,.86f,1f));font.draw(batch,"Try again or return to the adventure menu",273f,230f)
        font.setColor(Color.WHITE);font.draw(batch,"RESTART",295f,182f);font.draw(batch,"MENU",456f,182f);batch.end()
        if(restartTouch){game.screen=PlayScreen(game);dispose()}else if(menuTouch){game.screen=MainMenuScreen(game);dispose()}
    }
    override fun resize(width:Int,height:Int)=viewport.update(width,height,true)
    override fun pause(){};override fun resume(){};override fun hide(){};override fun dispose(){batch.dispose();shapes.dispose();font.dispose()}
}
