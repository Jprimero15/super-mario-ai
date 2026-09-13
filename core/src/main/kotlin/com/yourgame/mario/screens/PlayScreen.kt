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
import com.yourgame.mario.entities.Coin
import com.yourgame.mario.entities.Player
import com.yourgame.mario.entities.WalkerEnemy
import com.yourgame.mario.input.TouchInputController
import com.yourgame.mario.physics.CollisionHandler
import com.yourgame.mario.physics.Physics
import com.yourgame.mario.ui.HUD
import com.yourgame.mario.ui.VectorArt
import com.yourgame.mario.world.Level

class PlayScreen(private val game: MarioGame) : Screen {
    companion object { const val VIEWPORT_WIDTH=800f; const val VIEWPORT_HEIGHT=480f; const val LEVEL_TIME_LIMIT=200 }
    private val level=Level.level1(); private val collision=CollisionHandler(level.solidTiles,level.tileSize)
    private val camera=OrthographicCamera(); private val viewport:Viewport=FitViewport(VIEWPORT_WIDTH,VIEWPORT_HEIGHT,camera)
    private val hudCamera=OrthographicCamera(); private val hudViewport:Viewport=FitViewport(VIEWPORT_WIDTH,VIEWPORT_HEIGHT,hudCamera)
    private val input=TouchInputController(hudViewport)
    private val shapeRenderer=ShapeRenderer(); private val batch=SpriteBatch(); private val font=BitmapFont().apply{data.setScale(1.25f)}; private val hud=HUD(font)
    private val player=Player(level.playerStart.x,level.playerStart.y)
    private val enemies=level.enemySpawns.map{WalkerEnemy(it.x,it.y)}.toMutableList()
    private val coins=level.coinSpawns.map{Coin(it.x,it.y,it.width,it.height)}.toMutableList()
    private var paused=false; private var elapsedTime=0f; private var levelComplete=false; private var animTime=0f

    override fun show(){hudCamera.position.set(hudViewport.worldWidth/2f,hudViewport.worldHeight/2f,0f)}
    override fun render(delta:Float){val d=delta.coerceIn(0f,1f/30f);input.poll();if(input.isPauseJustPressed())paused=!paused;if(!paused&&!levelComplete)update(d);draw();input.endFrame()}
    private fun update(delta:Float){elapsedTime+=delta;animTime+=delta;val remaining=LEVEL_TIME_LIMIT-elapsedTime.toInt();if(remaining<=0&&!player.isDead)player.killInstantly();player.updatePhysics(delta,input,collision);for(e in enemies)if(e.alive)e.updatePhysics(delta,collision);handlePlayerEnemyCollisions();handleCoinCollisions();if(player.bounds.y<-200f&&!player.isDead)player.killInstantly();if(player.isDead){player.velocity.y+=Physics.GRAVITY*delta;player.bounds.y+=player.velocity.y*delta;if(player.bounds.y<-400f)respawnOrGameOver()};if(player.bounds.x+player.bounds.width>=level.widthInPixels-level.tileSize)levelComplete=true;updateCamera()}
    private fun handlePlayerEnemyCollisions(){for(enemy in enemies){if(!enemy.alive||player.isDead||!player.bounds.overlaps(enemy.bounds))continue;val stomp=player.velocity.y<0f&&player.bounds.y>=enemy.bounds.y+enemy.bounds.height-10f;if(stomp){enemy.alive=false;player.velocity.y=Player.JUMP_VELOCITY*.5f;player.score+=100}else if(!player.isInvincible){if(player.shrinkOrDie())player.killInstantly()else player.velocity.x=if(player.bounds.x<enemy.bounds.x)-200f else 200f}};enemies.removeAll{!it.alive}}
    private fun handleCoinCollisions(){val i=coins.iterator();while(i.hasNext()){val c=i.next();if(player.bounds.overlaps(c.bounds)){player.score+=10;i.remove()}}}
    private fun respawnOrGameOver(){player.lives-=1;if(player.lives<=0){game.screen=GameOverScreen(game,player.score);dispose();return};player.resetTo(level.playerStart.x,level.playerStart.y)}
    private fun updateCamera(){val half=viewport.worldWidth/2f;camera.position.x=if(level.widthInPixels>viewport.worldWidth)(player.bounds.x+player.bounds.width/2f).coerceIn(half,level.widthInPixels-half)else half;camera.position.y=viewport.worldHeight/2f;camera.update()}

    private fun draw(){
        Gdx.gl.glClearColor(.34f,.66f,.91f,1f);Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        shapeRenderer.projectionMatrix=camera.combined;shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        // Soft layered sky and distant hills.
        shapeRenderer.color=Color(.47f,.74f,.94f,1f);shapeRenderer.rect(camera.position.x-400f,0f,800f,480f)
        shapeRenderer.color=Color(.30f,.62f,.55f,1f);shapeRenderer.circle(camera.position.x-170f,120f,120f);shapeRenderer.circle(camera.position.x+170f,100f,145f)
        for(tile in level.solidTiles)VectorArt.tile(shapeRenderer,tile,tile.y+tile.height>=level.heightInPixels-level.tileSize)
        for(coin in coins)VectorArt.coin(shapeRenderer,coin.bounds,animTime*5f)
        for(enemy in enemies)VectorArt.enemy(shapeRenderer,enemy.bounds)
        val visible=!player.isInvincible||(animTime%0.2f)<0.1f;if(visible)VectorArt.player(shapeRenderer,player.bounds,player.facingRight,player.size.name=="BIG",player.isDead)
        shapeRenderer.end()

        shapeRenderer.projectionMatrix=hudCamera.combined;shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        drawButton(input.leftButton,input.isLeftPressed(),Color(.09f,.14f,.21f,1f));drawButton(input.rightButton,input.isRightPressed(),Color(.09f,.14f,.21f,1f));drawButton(input.jumpButton,input.isJumpPressed(),Color(.87f,.16f,.08f,1f));VectorArt.pause(shapeRenderer,input.pauseButton,paused);shapeRenderer.end()
        batch.projectionMatrix=hudCamera.combined;batch.begin();hud.render(batch,player,(LEVEL_TIME_LIMIT-elapsedTime.toInt()).coerceAtLeast(0))
        font.draw(batch,"◀",input.leftButton.x+28f,input.leftButton.y+60f);font.draw(batch,"▶",input.rightButton.x+28f,input.rightButton.y+60f);font.draw(batch,"JUMP",input.jumpButton.x+18f,input.jumpButton.y+65f)
        if(paused)font.draw(batch,"PAUSED",350f,285f);if(levelComplete)font.draw(batch,"LEVEL COMPLETE!   SCORE ${player.score}",220f,285f);batch.end()
    }
    private fun drawButton(b:Rectangle,pressed:Boolean,accent:Color){VectorArt.button(shapeRenderer,b,pressed,accent)}
    override fun resize(width:Int,height:Int){viewport.update(width,height);hudViewport.update(width,height,true)}
    override fun pause(){};override fun resume(){};override fun hide(){};override fun dispose(){shapeRenderer.dispose();batch.dispose();font.dispose()}
}
