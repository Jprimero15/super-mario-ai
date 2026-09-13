package com.yourgame.mario.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.yourgame.mario.MarioGame
import com.yourgame.mario.ui.VectorArt

class MainMenuScreen(private val game: MarioGame) : Screen {
    private val camera=OrthographicCamera();private val viewport:Viewport=FitViewport(800f,480f,camera)
    private val batch=SpriteBatch();private val shapes=ShapeRenderer();private val title=BitmapFont().apply{data.setScale(3.1f)};private val font=BitmapFont().apply{data.setScale(1.35f)}
    private val play= com.badlogic.gdx.math.Rectangle(285f,150f,230f,70f)
    override fun show(){camera.position.set(400f,240f,0f)}
    override fun render(delta:Float){
        Gdx.gl.glClearColor(.06f,.10f,.16f,1f);Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        val touched=Gdx.input.justTouched();camera.update();shapes.projectionMatrix=camera.combined;shapes.begin(ShapeRenderer.ShapeType.Filled)
        shapes.color=Color(.10f,.20f,.34f,1f);shapes.rect(0f,0f,800f,480f);shapes.color=Color(.13f,.30f,.48f,1f);shapes.circle(110f,70f,95f);shapes.circle(690f,90f,120f)
        VectorArt.player(shapes,com.badlogic.gdx.math.Rectangle(92f,54f,62f,62f),true,false,false)
        VectorArt.coin(shapes,com.badlogic.gdx.math.Rectangle(650f,355f,34f,34f),0f)
        VectorArt.button(shapes,play,false,Color(.88f,.15f,.08f,1f));shapes.end()
        batch.projectionMatrix=camera.combined;batch.begin()
        title.setColor(Color.WHITE);title.draw(batch,"MARYOU AI",238f,350f);font.setColor(Color(.75f,.86f,1f,1f));font.draw(batch,"A fast, colorful platform adventure",245f,305f);font.setColor(Color.WHITE);font.draw(batch,"PLAY",375f,193f);font.setColor(Color(.72f,.78f,.86f,1f));font.draw(batch,"Vector graphics • responsive controls • AI-ready gameplay",178f,105f);batch.end()
        if(touched){game.screen=PlayScreen(game);dispose()}
    }
    override fun resize(width:Int,height:Int)=viewport.update(width,height,true);override fun pause(){};override fun resume(){};override fun hide(){};override fun dispose(){batch.dispose();shapes.dispose();title.dispose();font.dispose()}
}
