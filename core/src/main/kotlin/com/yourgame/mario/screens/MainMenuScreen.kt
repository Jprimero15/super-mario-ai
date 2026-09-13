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
import com.yourgame.mario.ui.VectorArt

class MainMenuScreen(private val game: MarioGame) : Screen {
    private val camera=OrthographicCamera();private val viewport:Viewport=FitViewport(800f,480f,camera)
    private val batch=SpriteBatch();private val shapes=ShapeRenderer();private val layout=GlyphLayout()
    private val title=BitmapFont();private val font=BitmapFont();private val small=BitmapFont()
    private val play=Rectangle(280f,140f,240f,70f)

    override fun show(){camera.position.set(400f,240f,0f);camera.update()}
    override fun render(delta:Float){
        Gdx.gl.glClearColor(.025f,.045f,.075f,1f);Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        val touched=Gdx.input.justTouched();val p=viewport.unproject(com.badlogic.gdx.math.Vector2(Gdx.input.x.toFloat(),Gdx.input.y.toFloat()))
        shapes.projectionMatrix=camera.combined;shapes.begin(ShapeRenderer.ShapeType.Filled)
        shapes.color=Color(.10f,.28f,.46f,1f);shapes.rect(0f,0f,800f,480f)
        shapes.color=Color(.23f,.50f,.68f,.55f);shapes.circle(65f,45f,150f);shapes.circle(745f,55f,175f)
        shapes.color=Color(.17f,.42f,.32f,.72f);shapes.circle(145f,75f,105f);shapes.circle(665f,85f,115f)
        shapes.color=Color(.035f,.065f,.105f,.88f);shapes.rect(150f,55f,500f,370f)
        shapes.color=Color(1f,1f,1f,.05f);shapes.rect(166f,71f,468f,338f)
        VectorArt.coin(shapes,Rectangle(607f,326f,34f,34f),delta*3f);VectorArt.player(shapes,Rectangle(92f,60f,70f,70f),true,false,false)
        VectorArt.button(shapes,play,false,Color(.90f,.18f,.11f,1f),.92f);shapes.color=Color.WHITE;shapes.triangle(312f,160f,312f,190f,337f,175f)
        shapes.end()

        batch.projectionMatrix=camera.combined;batch.begin()
        title.data.setScale(3.0f);title.color=Color.WHITE;drawCentered(title,"MARYOU AI",370f)
        font.data.setScale(1.10f);font.color=Color(.78f,.91f,1f,1f);drawCentered(font,"ENDLESS RUN",325f)
        font.data.setScale(.92f);font.color=Color(.66f,.77f,.87f,1f);drawCentered(font,"Auto-run  •  Jump  •  Collect  •  Survive",300f)
        font.data.setScale(1.08f);font.color=Color.WHITE;drawCenteredAt(font,"PLAY",420f,180f)
        val prefs=Gdx.app.getPreferences("maryou_run_records")
        font.data.setScale(.82f);font.color=Color(.72f,.83f,.92f,1f);drawCentered(font,"BEST  ${prefs.getInteger("bestScore",0)}   •   COINS  ${prefs.getInteger("bestCoins",0)}   •   STEPS  ${prefs.getInteger("bestSteps",0)}",108f)
        small.data.setScale(.72f);small.color=Color(.55f,.67f,.78f,1f);drawCentered(small,"Records are saved on this device",87f)
        batch.end()
        if(touched&&play.contains(p)){game.screen=PlayScreen(game);dispose()}
    }
    private fun drawCentered(f:BitmapFont,text:String,y:Float){layout.setText(f,text);f.draw(batch,text,400f-layout.width/2f,y)}
    private fun drawCenteredAt(f:BitmapFont,text:String,x:Float,y:Float){layout.setText(f,text);f.draw(batch,text,x-layout.width/2f,y)}
    override fun resize(width:Int,height:Int)=viewport.update(width,height,true)
    override fun pause(){};override fun resume(){};override fun hide(){};override fun dispose(){batch.dispose();shapes.dispose();title.dispose();font.dispose();small.dispose()}
}
