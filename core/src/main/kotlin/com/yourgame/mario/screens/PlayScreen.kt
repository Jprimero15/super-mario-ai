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
    companion object { const val VIEWPORT_WIDTH=800f; const val VIEWPORT_HEIGHT=480f; const val LEVEL_TIME_LIMIT=360 }
    private val level=Level.level1()
    private val collision=CollisionHandler(level.solidTiles,level.tileSize)
    private val camera=OrthographicCamera();private val viewport:Viewport=FitViewport(VIEWPORT_WIDTH,VIEWPORT_HEIGHT,camera)
    private val hudCamera=OrthographicCamera();private val hudViewport:Viewport=FitViewport(VIEWPORT_WIDTH,VIEWPORT_HEIGHT,hudCamera)
    private val input=TouchInputController(hudViewport)
    private val shapeRenderer=ShapeRenderer();private val batch=SpriteBatch();private val font=BitmapFont();private val hud=HUD(font);private val textLayout=GlyphLayout()
    private val player=Player(level.playerStart.x,level.playerStart.y)
    private val enemies=level.enemySpawns.map{WalkerEnemy(it.x,it.y)}.toMutableList()
    private val coins=level.coinSpawns.map{Coin(it.x,it.y,it.width,it.height)}.toMutableList()
    private var paused=false;private var elapsedTime=0f;private var levelComplete=false;private var animTime=0f

    override fun show(){hudCamera.position.set(hudViewport.worldWidth/2f,hudViewport.worldHeight/2f,0f);hudCamera.update()}

    override fun render(delta:Float){
        val d=delta.coerceIn(0f,1f/30f);input.poll()
        if(input.isPauseJustPressed()&&!levelComplete)paused=!paused
        if(paused){if(input.isRestartJustPressed()){restart();return};if(input.isMenuJustPressed()){game.screen=MainMenuScreen(game);dispose();return}}
        if(!paused&&!levelComplete)update(d)
        draw();input.endFrame()
    }

    private fun restart(){game.screen=PlayScreen(game);dispose()}

    private fun update(delta:Float){
        elapsedTime+=delta;animTime+=delta
        if(LEVEL_TIME_LIMIT-elapsedTime.toInt()<=0&&!player.isDead)player.killInstantly()
        player.updatePhysics(delta,input,collision)
        for(e in enemies)if(e.alive)e.updatePhysics(delta,collision)
        handlePlayerEnemyCollisions();handleCoinCollisions()
        if(player.bounds.y<-200f&&!player.isDead)player.killInstantly()
        if(player.isDead){player.velocity.y+=Physics.GRAVITY*delta;player.bounds.y+=player.velocity.y*delta;if(player.bounds.y<-400f)respawnOrGameOver()}
        if(player.bounds.x+player.bounds.width>=level.widthInPixels-level.tileSize)levelComplete=true
        updateCamera()
    }

    private fun handlePlayerEnemyCollisions(){for(enemy in enemies){if(!enemy.alive||player.isDead||!player.bounds.overlaps(enemy.bounds))continue;val stomp=player.velocity.y<0f&&player.bounds.y>=enemy.bounds.y+enemy.bounds.height-10f;if(stomp){enemy.alive=false;player.velocity.y=Player.JUMP_VELOCITY*.5f;player.score+=100}else if(!player.isInvincible){if(player.shrinkOrDie())player.killInstantly()else player.velocity.x=if(player.bounds.x<enemy.bounds.x)-200f else 200f}};enemies.removeAll{!it.alive}}
    private fun handleCoinCollisions(){val i=coins.iterator();while(i.hasNext()){val c=i.next();if(player.bounds.overlaps(c.bounds)){player.score+=10;i.remove()}}}
    private fun respawnOrGameOver(){player.lives-=1;if(player.lives<=0){game.screen=GameOverScreen(game,player.score);dispose();return};player.resetTo(level.playerStart.x,level.playerStart.y)}
    private fun updateCamera(){val half=viewport.worldWidth/2f;camera.position.x=if(level.widthInPixels>viewport.worldWidth)(player.bounds.x+player.bounds.width/2f).coerceIn(half,level.widthInPixels-half)else half;camera.position.y=viewport.worldHeight/2f;camera.update()}

    private fun draw(){
        Gdx.gl.glClearColor(.34f,.66f,.91f,1f);Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        shapeRenderer.projectionMatrix=camera.combined;shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color=Color(.47f,.74f,.94f,1f);shapeRenderer.rect(camera.position.x-400f,0f,800f,480f)
        shapeRenderer.color=Color(.30f,.62f,.55f,1f);shapeRenderer.circle(camera.position.x-170f,120f,120f);shapeRenderer.circle(camera.position.x+170f,100f,145f)
        for(tile in level.solidTiles)VectorArt.tile(shapeRenderer,tile,tile.y+tile.height>=level.heightInPixels-level.tileSize)
        for(coin in coins)VectorArt.coin(shapeRenderer,coin.bounds,animTime*5f)
        for(enemy in enemies)VectorArt.enemy(shapeRenderer,enemy.bounds)
        val visible=!player.isInvincible||(animTime%0.2f)<0.1f;if(visible)VectorArt.player(shapeRenderer,player.bounds,player.facingRight,player.size.name=="BIG",player.isDead)
        shapeRenderer.end()

        shapeRenderer.projectionMatrix=hudCamera.combined;shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        // Soft HUD strip: four equal visual zones keep every label aligned.
        shapeRenderer.color=Color(0f,0f,0f,.18f);shapeRenderer.rect(10f,425f,780f,47f)
        shapeRenderer.color=Color(1f,1f,1f,.055f);shapeRenderer.rect(14f,429f,183f,39f);shapeRenderer.rect(203f,429f,183f,39f);shapeRenderer.rect(392f,429f,183f,39f);shapeRenderer.rect(581f,429f,183f,39f)
        VectorArt.button(shapeRenderer,input.leftButton,input.isLeftPressed(),Color(.08f,.12f,.18f,1f),.22f)
        VectorArt.button(shapeRenderer,input.rightButton,input.isRightPressed(),Color(.08f,.12f,.18f,1f),.22f)
        VectorArt.button(shapeRenderer,input.jumpButton,input.isJumpPressed(),Color(.95f,.18f,.08f,1f),.25f)
        VectorArt.pause(shapeRenderer,input.pauseButton,paused)
        VectorArt.leftIcon(shapeRenderer,Rectangle(input.leftButton.x+13f,input.leftButton.y+10f,30f,30f))
        VectorArt.rightIcon(shapeRenderer,Rectangle(input.rightButton.x+13f,input.rightButton.y+10f,30f,30f))
        VectorArt.jumpIcon(shapeRenderer,Rectangle(input.jumpButton.x+12f,input.jumpButton.y+8f,40f,40f))
        if(paused){
            shapeRenderer.color=Color(.02f,.04f,.08f,.72f);shapeRenderer.rect(0f,0f,800f,480f)
            shapeRenderer.color=Color(.07f,.10f,.16f,.97f);shapeRenderer.rect(210f,105f,380f,285f)
            shapeRenderer.color=Color(1f,1f,1f,.06f);shapeRenderer.rect(224f,119f,352f,257f)
            VectorArt.button(shapeRenderer,input.restartButton,input.isRestartJustPressed(),Color(.12f,.20f,.30f,1f),.82f)
            VectorArt.button(shapeRenderer,input.menuButton,input.isMenuJustPressed(),Color(.12f,.20f,.30f,1f),.82f)
            VectorArt.restartIcon(shapeRenderer,Rectangle(267f,171f,42f,42f));VectorArt.homeIcon(shapeRenderer,Rectangle(432f,171f,42f,42f))
        }
        shapeRenderer.end()

        batch.projectionMatrix=hudCamera.combined;batch.begin()
        hud.render(batch,player,(LEVEL_TIME_LIMIT-elapsedTime.toInt()).coerceAtLeast(0))
        if(paused){drawCentered("PAUSED",335f,1.70f,Color.WHITE);drawCentered("Game paused",305f,1.05f,Color(.72f,.82f,.92f,1f));drawCenteredAt("RESTART",326f,190f,1.05f);drawCenteredAt("MENU",470f,190f,1.05f)}
        if(levelComplete){drawCentered("ADVENTURE COMPLETE!",320f,1.35f,Color.WHITE);drawCenteredAt("Score  ${player.score}   •   Great run!",400f,265f,1.05f)}
        batch.end()
    }

    private fun drawCentered(text:String,y:Float,scale:Float,color:Color){font.data.setScale(scale);font.color=color;textLayout.setText(font,text);font.draw(batch,text,400f-textLayout.width/2f,y)}
    private fun drawCenteredAt(text:String,centerX:Float,y:Float,scale:Float){font.data.setScale(scale);font.color=Color.WHITE;textLayout.setText(font,text);font.draw(batch,text,centerX-textLayout.width/2f,y)}

    override fun resize(width:Int,height:Int){viewport.update(width,height);hudViewport.update(width,height,true)}
    override fun pause(){paused=true}
    override fun resume(){}
    override fun hide(){}
    override fun dispose(){shapeRenderer.dispose();batch.dispose();font.dispose()}
}
