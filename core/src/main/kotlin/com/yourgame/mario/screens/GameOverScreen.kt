package com.yourgame.mario.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.yourgame.mario.MarioGame
import com.yourgame.mario.ui.VectorArt

class GameOverScreen(
    private val game: MarioGame,
    private val finalScore: Int,
    private val finalCoins: Int = 0,
    private val finalSteps: Int = 0
) : Screen {
    private val camera=OrthographicCamera();private val viewport:Viewport=FitViewport(800f,480f,camera)
    private val batch=SpriteBatch();private val shapes=ShapeRenderer();private val font=BitmapFont()
    private val layout=GlyphLayout()
    private val restart=Rectangle(250f,125f,135f,58f);private val menu=Rectangle(415f,125f,135f,58f)

    override fun show(){camera.position.set(400f,240f,0f);camera.update()}
    override fun render(delta:Float){
        Gdx.gl.glClearColor(.025f,.045f,.075f,1f);Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        var restartTouch=false;var menuTouch=false
        if(Gdx.input.justTouched()){
            val p=viewport.unproject(Vector2(Gdx.input.getX().toFloat(),Gdx.input.getY().toFloat()))
            restartTouch=restart.contains(p);menuTouch=menu.contains(p)
        }
        shapes.projectionMatrix=camera.combined;shapes.begin(ShapeRenderer.ShapeType.Filled)
        shapes.color=Color(.07f,.11f,.17f,1f);shapes.rect(0f,0f,800f,480f)
        shapes.color=Color(.25f,.11f,.16f,.55f);shapes.circle(100f,70f,125f);shapes.circle(705f,90f,150f)
        shapes.color=Color(.055f,.085f,.13f,.98f);shapes.rect(170f,62f,460f,350f)
        shapes.color=Color(1f,1f,1f,.045f);shapes.rect(185f,77f,430f,320f)
        VectorArt.button(shapes,restart,false,Color(.10f,.27f,.39f,1f),.92f);VectorArt.button(shapes,menu,false,Color(.10f,.27f,.39f,1f),.92f)
        VectorArt.restartIcon(shapes,Rectangle(268f,133f,42f,42f));VectorArt.homeIcon(shapes,Rectangle(433f,133f,42f,42f));shapes.end()

        batch.projectionMatrix=camera.combined;batch.begin()
        drawCentered("RUN OVER",345f,2.05f,Color(.98f,.43f,.32f,1f))
        drawCentered("SCORE  $finalScore",295f,1.22f,Color.WHITE)
        drawCentered("COINS  $finalCoins     STEPS  $finalSteps",255f,1.08f,Color(.78f,.88f,.95f,1f))
        drawCentered("Your run has been recorded",220f,.98f,Color(.60f,.72f,.82f,1f))
        drawCenteredAt("RESTART",318f,158f,1.0f);drawCenteredAt("MENU",468f,158f,1.0f)
        batch.end()
        if(restartTouch){game.screen=PlayScreen(game);dispose()}else if(menuTouch){game.screen=MainMenuScreen(game);dispose()}
    }
    private fun drawCentered(text:String,y:Float,scale:Float,color:Color){font.data.setScale(scale);font.color=color;layout.setText(font,text);font.draw(batch,text,400f-layout.width/2f,y)}
    private fun drawCenteredAt(text:String,x:Float,y:Float,scale:Float){font.data.setScale(scale);font.color=Color.WHITE;layout.setText(font,text);font.draw(batch,text,x-layout.width/2f,y)}
    override fun resize(width:Int,height:Int)=viewport.update(width,height,true)
    override fun pause(){};override fun resume(){};override fun hide(){};override fun dispose(){batch.dispose();shapes.dispose();font.dispose()}
}
