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
import com.yourgame.mario.world.HoleSpawn
import com.yourgame.mario.world.Level
import com.yourgame.mario.world.MonsterSpawn
import kotlin.math.max

class PlayScreen(private val game: MarioGame) : Screen {
    companion object { const val VIEWPORT_WIDTH=800f; const val WORLD_HEIGHT=400f; const val UI_HEIGHT=480f }
    private val level=Level.level1();private val collision=CollisionHandler(level.solidTiles,level.tileSize)
    private val camera=OrthographicCamera();private val viewport:Viewport=FitViewport(VIEWPORT_WIDTH,WORLD_HEIGHT,camera)
    private val hudCamera=OrthographicCamera();private val hudViewport:Viewport=FitViewport(VIEWPORT_WIDTH,UI_HEIGHT,hudCamera)
    private val input=TouchInputController(hudViewport);private val shapeRenderer=ShapeRenderer();private val batch=SpriteBatch();private val font=BitmapFont();private val hud=HUD(font);private val textLayout=GlyphLayout()
    private val player=Player(level.playerStart.x,level.playerStart.y);private val coins=mutableListOf<Coin>();private val monsters=mutableListOf<WalkerEnemy>()
    private var appliedSolidCount=level.solidTiles.size;private var spawnedCoinCount=0;private var spawnedMonsterCount=0;private var paused=false;private var elapsedTime=0f;private var animTime=0f;private var checkpointX=level.playerStart.x;private var previousPlayerX=player.bounds.x;private var previousPlayerBottom=player.bounds.y+player.bounds.height

    override fun show(){hudCamera.position.set(hudViewport.worldWidth/2f,hudViewport.worldHeight/2f,0f);hudCamera.update();syncWorld()}
    override fun render(delta:Float){
        val d=delta.coerceIn(0f,1f/30f);input.poll();if(input.isPauseJustPressed())paused=!paused
        if(paused){if(input.isRestartJustPressed()){restart();return};if(input.isMenuJustPressed()){game.screen=MainMenuScreen(game);dispose();return}}
        if(!paused)update(d);draw();input.endFrame()
    }
    private fun restart(){game.screen=PlayScreen(game);dispose()}

    private fun update(delta:Float){
        elapsedTime+=delta;animTime+=delta;previousPlayerX=player.bounds.x;previousPlayerBottom=player.bounds.y+player.bounds.height
        level.ensureGeneratedThrough(player.bounds.x+2200f);syncWorld()
        val steps=currentSteps();player.updatePhysics(delta,input,collision,steps);handlePipeHits();handleMonsterCollisions();handleCoinCollisions()
        if(player.bounds.y < -120f && !player.isDead) player.killInstantly()
        if(player.isDead){player.velocity.y+=Physics.GRAVITY*delta;player.bounds.y+=player.velocity.y*delta;if(player.bounds.y < -360f) respawn()}
        val nextCheckpoint=(player.bounds.x/256f).toInt()*256f+64f;if(nextCheckpoint>checkpointX)checkpointX=nextCheckpoint
        updateCamera()
    }
    private fun currentSteps():Int=max(0,(player.bounds.x/level.tileSize).toInt())

    private fun syncWorld(){
        if(appliedSolidCount<level.solidTiles.size){collision.addSolids(level.solidTiles.subList(appliedSolidCount,level.solidTiles.size));appliedSolidCount=level.solidTiles.size}
        while(spawnedCoinCount<level.coinSpawns.size){val s=level.coinSpawns[spawnedCoinCount++];coins+=Coin(s.x,s.y,s.width,s.height)}
        while(spawnedMonsterCount<level.monsterSpawns.size){val s:MonsterSpawn=level.monsterSpawns[spawnedMonsterCount++];monsters+=WalkerEnemy(s.bounds.x,s.bounds.y,s.tier)}
    }
    private fun handleCoinCollisions(){val it=coins.iterator();while(it.hasNext()){val coin=it.next();if(player.bounds.overlaps(coin.bounds)){player.coinsCollected++;player.score+=10;it.remove()}}}

    private fun handlePipeHits(){
        if(player.isInvincible||player.isDead||player.velocity.x<=0f)return
        for(pipe in level.pipeSpawns){
            if(pipe.x>player.bounds.x+180f||pipe.x+pipe.width<player.bounds.x-30f)continue
            val pipeTop=pipe.y+pipe.height
            val landedOnTop=player.bounds.y+player.bounds.height>=pipeTop-2f&&player.bounds.y+player.bounds.height<=pipeTop+8f&&previousPlayerBottom<=pipeTop+10f&&player.velocity.y==0f
            val verticalContact=player.bounds.y<pipeTop&&player.bounds.y+player.bounds.height>pipe.y
            val ranIntoSide=previousPlayerX+player.bounds.width<=pipe.x+5f&&player.bounds.x<=pipe.x+2f&&verticalContact
            if(ranIntoSide&&!landedOnTop){if(player.shrinkOrDie())player.killInstantly();return}
        }
    }

    private fun handleMonsterCollisions(){
        if(player.isDead)return
        for(monster in monsters){
            if(!monster.alive||!player.bounds.overlaps(monster.bounds))continue
            val monsterTop=monster.bounds.y+monster.bounds.height
            val stomp=player.velocity.y<0f&&previousPlayerBottom>=monsterTop-3f&&player.bounds.y+player.bounds.height<=monsterTop+12f
            if(stomp){monster.alive=false;player.score+=50;player.velocity.y=Player.JUMP_VELOCITY*.62f}
            else if(!player.isInvincible){if(player.shrinkOrDie())player.killInstantly();return}
        }
    }

    private fun respawn(){
        player.lives-=1
        if(player.lives<=0){recordRun();game.screen=GameOverScreen(game,player.score,player.coinsCollected,currentSteps());dispose();return}
        player.resetTo(checkpointX,level.tileSize)
    }
    private fun recordRun(){
        val prefs=Gdx.app.getPreferences("maryou_run_records");val bestScore=prefs.getInteger("bestScore",0);val bestCoins=prefs.getInteger("bestCoins",0);val bestSteps=prefs.getInteger("bestSteps",0)
        prefs.putInteger("bestScore",max(bestScore,player.score));prefs.putInteger("bestCoins",max(bestCoins,player.coinsCollected));prefs.putInteger("bestSteps",max(bestSteps,currentSteps()));prefs.flush()
    }

    private fun updateCamera(){val half=viewport.worldWidth/2f;camera.position.x=max(half,player.bounds.x+player.bounds.width/2f+90f);camera.position.y=WORLD_HEIGHT/2f;camera.update()}

    private fun draw(){
        Gdx.gl.glClearColor(.035f,.055f,.085f,1f);Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        viewport.apply();shapeRenderer.projectionMatrix=camera.combined;shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color=Color(.28f,.55f,.82f,1f);shapeRenderer.rect(camera.position.x-420f,0f,840f,WORLD_HEIGHT)
        shapeRenderer.color=Color(.47f,.72f,.88f,.22f);shapeRenderer.circle(camera.position.x-230f,305f,110f);shapeRenderer.circle(camera.position.x+190f,285f,135f)
        shapeRenderer.color=Color(.17f,.42f,.34f,.92f);shapeRenderer.circle(camera.position.x-180f,80f,100f);shapeRenderer.circle(camera.position.x+220f,70f,120f)
        for(hole:HoleSpawn in level.holeSpawns)if(hole.bounds.x+hole.bounds.width>=camera.position.x-520f&&hole.bounds.x<=camera.position.x+520f)VectorArt.hole(shapeRenderer,hole.bounds)
        for(tile in level.solidTiles)if(tile.x+tile.width>=camera.position.x-520f&&tile.x<=camera.position.x+520f)VectorArt.tile(shapeRenderer,tile,tile.y==0f)
        for(pipe in level.pipeSpawns)if(pipe.x+pipe.width>=camera.position.x-520f&&pipe.x<=camera.position.x+520f)VectorArt.pipe(shapeRenderer,pipe)
        for(monster in monsters)if(monster.alive&&monster.bounds.x+monster.bounds.width>=camera.position.x-520f&&monster.bounds.x<=camera.position.x+520f)VectorArt.monster(shapeRenderer,monster.bounds,monster.tier)
        for(coin in coins)if(coin.bounds.x+coin.bounds.width>=camera.position.x-520f&&coin.bounds.x<=camera.position.x+520f)VectorArt.coin(shapeRenderer,coin.bounds,animTime*5f)
        if(!player.isInvincible||(animTime%.2f)<.1f)VectorArt.player(shapeRenderer,player.bounds,player.facingRight,player.size.name=="BIG",player.isDead)
        shapeRenderer.end()

        hudViewport.apply();shapeRenderer.projectionMatrix=hudCamera.combined;shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color=Color(.035f,.055f,.085f,.90f);shapeRenderer.rect(0f,0f,800f,82f);shapeRenderer.color=Color(1f,1f,1f,.055f);shapeRenderer.rect(0f,82f,800f,2f)
        shapeRenderer.color=Color(0f,0f,0f,.20f);shapeRenderer.rect(12f,423f,776f,50f);shapeRenderer.color=Color(1f,1f,1f,.045f)
        for(x in floatArrayOf(16f,210f,404f,598f))shapeRenderer.rect(x,428f,186f,40f)
        VectorArt.button(shapeRenderer,input.leftButton,input.isLeftPressed(),Color(.08f,.14f,.21f,1f),.32f);VectorArt.button(shapeRenderer,input.rightButton,input.isRightPressed(),Color(.08f,.14f,.21f,1f),.32f);VectorArt.button(shapeRenderer,input.jumpButton,input.isJumpPressed(),Color(.86f,.22f,.12f,1f),.36f);VectorArt.pause(shapeRenderer,input.pauseButton,paused)
        VectorArt.leftIcon(shapeRenderer,Rectangle(input.leftButton.x+10f,input.leftButton.y+8f,30f,30f));VectorArt.rightIcon(shapeRenderer,Rectangle(input.rightButton.x+10f,input.rightButton.y+8f,30f,30f));VectorArt.jumpIcon(shapeRenderer,Rectangle(input.jumpButton.x+11f,input.jumpButton.y+7f,40f,40f))
        if(paused){shapeRenderer.color=Color(.02f,.035f,.06f,.78f);shapeRenderer.rect(0f,0f,800f,480f);shapeRenderer.color=Color(.065f,.10f,.15f,.99f);shapeRenderer.rect(190f,98f,420f,300f);shapeRenderer.color=Color(1f,1f,1f,.06f);shapeRenderer.rect(205f,113f,390f,270f);VectorArt.button(shapeRenderer,input.restartButton,input.isRestartJustPressed(),Color(.10f,.27f,.39f,1f),.88f);VectorArt.button(shapeRenderer,input.menuButton,input.isMenuJustPressed(),Color(.10f,.27f,.39f,1f),.88f);VectorArt.restartIcon(shapeRenderer,Rectangle(267f,161f,42f,42f));VectorArt.homeIcon(shapeRenderer,Rectangle(432f,161f,42f,42f))}
        shapeRenderer.end()

        batch.projectionMatrix=hudCamera.combined;batch.begin();hud.render(batch,player,currentSteps());font.data.setScale(.88f);font.color=Color(.72f,.82f,.92f,1f)
        val progress="AUTO RUN  •  STEPS ${currentSteps()}  •  COINS ${player.coinsCollected}  •  SPEED ${player.currentRunSpeed(currentSteps()).toInt()}";textLayout.setText(font,progress);font.draw(batch,progress,400f-textLayout.width/2f,40f)
        if(paused){drawCentered("PAUSED",335f,1.7f,Color.WHITE);drawCentered("Auto-run paused",305f,1.05f,Color(.72f,.82f,.92f,1f));drawCenteredAt("RESTART",326f,185f,1.05f);drawCenteredAt("MENU",470f,185f,1.05f)}
        batch.end()
    }
    private fun drawCentered(text:String,y:Float,scale:Float,color:Color){font.data.setScale(scale);font.color=color;textLayout.setText(font,text);font.draw(batch,text,400f-textLayout.width/2f,y)}
    private fun drawCenteredAt(text:String,centerX:Float,y:Float,scale:Float){font.data.setScale(scale);font.color=Color.WHITE;textLayout.setText(font,text);font.draw(batch,text,centerX-textLayout.width/2f,y)}
    override fun resize(width:Int,height:Int){viewport.update(width,height,false);val controlHeight=(height*.17f).toInt().coerceIn(78,height/3);viewport.setScreenBounds(0,controlHeight,width,(height-controlHeight).coerceAtLeast(1));hudViewport.update(width,height,true)}
    override fun pause(){paused=true};override fun resume(){};override fun hide(){};override fun dispose(){shapeRenderer.dispose();batch.dispose();font.dispose()}
}
