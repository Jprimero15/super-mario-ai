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

class MainMenuScreen(private val game: MarioGame) : Screen {
    private val camera=OrthographicCamera();private val viewport:Viewport=FitViewport(800f,480f,camera)
    private val batch=SpriteBatch();private val shapes=ShapeRenderer()
    private val title=BitmapFont().apply{data.setScale(3.1f)};private val font=BitmapFont().apply{data.setScale(1.18f)}
    private val play=Rectangle(305f,154f,190f,66f)

    override fun show(){camera.position.set(400f,240f,0f)}
    override fun render(delta:Float){
        Gdx.gl.glClearColor(.04f,.07f,.12f,1f);Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);camera.update()
        val touched=Gdx.input.justTouched()
        shapes.projectionMatrix=camera.combined;shapes.begin(ShapeRenderer.ShapeType.Filled)
        shapes.color=Color(.10f,.22f,.36f,1f);shapes.rect(0f,0f,800f,480f)
        shapes.color=Color(.15f,.34f,.50f,1f);shapes.circle(80f,45f,120f);shapes.circle(735f,65f,150f)
        shapes.color=Color(.22f,.46f,.34f,1f);shapes.circle(135f,90f,85f);shapes.circle(675f,100f,100f)
        VectorArt.player(shapes,Rectangle(92f,58f,70f,70f),true,false,false);VectorArt.coin(shapes,Rectangle(650f,355f,34f,34f),delta*3f)
        shapes.color=Color(.03f,.06f,.10f,.70f);shapes.rect(185f,88f,430f,315f)
        VectorArt.button(shapes,play,false,Color(.95f,.18f,.08f,1f),.86f);VectorArt.playIcon(shapes,Rectangle(318f,165f,42f,42f));shapes.end()

        batch.projectionMatrix=camera.combined;batch.begin()
        title.setColor(Color.WHITE);title.draw(batch,"MARYOU AI",238f,352f)
        font.setColor(Color(.78f,.90f,1f,1f));font.draw(batch,"A fast, colorful platform adventure",266f,310f)
        font.setColor(Color.WHITE);font.draw(batch,"PLAY",382f,190f)
        font.setColor(Color(.70f,.79f,.88f,1f));font.draw(batch,"Run  •  Jump  •  Collect  •  Survive",281f,118f)
        font.setColor(Color(.58f,.67f,.76f,1f));font.draw(batch,"Touch controls • Vector UI • Offline adventure",252f,98f)
        batch.end()
        if(touched){game.screen=PlayScreen(game);dispose()}
    }
    override fun resize(width:Int,height:Int)=viewport.update(width,height,true)
    override fun pause(){};override fun resume(){};override fun hide(){};override fun dispose(){batch.dispose();shapes.dispose();title.dispose();font.dispose()}
}
