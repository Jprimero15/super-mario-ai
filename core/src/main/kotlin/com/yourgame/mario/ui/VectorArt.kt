package com.yourgame.mario.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import kotlin.math.abs
import kotlin.math.cos

/** Resolution-independent vector artwork; no bitmap assets required. */
object VectorArt {
    private val sky = Color(.30f, .60f, .86f, 1f)
    private val grass = Color(.20f, .54f, .34f, 1f)
    private val dirt = Color(.39f, .25f, .16f, 1f)

    fun player(r: ShapeRenderer, b: Rectangle, facingRight: Boolean, big: Boolean, dead: Boolean) {
        val x=b.x; val y=b.y; val w=b.width; val h=b.height
        if(dead){r.color=Color(.42f,.45f,.50f,1f);r.rect(x,y,w,h*.72f);return}
        r.color=Color(0f,0f,0f,.15f);r.ellipse(x+w*.12f,y-3f,w*.76f,7f)
        r.color=Color(.28f,.10f,.06f,1f);r.ellipse(x+w*.02f,y,w*.45f,h*.20f);r.ellipse(x+w*.53f,y,w*.45f,h*.20f)
        r.color=Color(.08f,.31f,.67f,1f);r.rect(x+w*.18f,y+h*.18f,w*.64f,h*.43f)
        r.color=Color(.88f,.12f,.10f,1f);r.rect(x+w*.08f,y+h*.40f,w*.84f,h*.32f)
        r.color=Color(1f,.71f,.50f,1f);r.ellipse(x+w*.16f,y+h*.55f,w*.68f,h*.36f)
        r.color=Color(.88f,.08f,.08f,1f);r.ellipse(x+w*.10f,y+h*.75f,w*.78f,h*.22f);r.rect(x+w*.22f,y+h*.77f,w*.58f,h*.13f)
        r.color=Color(.22f,.09f,.04f,1f);r.ellipse(x+w*.39f,y+h*.62f,w*.28f,h*.10f)
        r.color=Color.BLACK;val eyeX=if(facingRight)x+w*.63f else x+w*.31f;r.circle(eyeX,y+h*.72f,w*.035f)
        if(big){r.color=Color(1f,1f,1f,.12f);r.rect(x,y,w,h)}
    }

    fun coin(r:ShapeRenderer,b:Rectangle,phase:Float){val cx=b.x+b.width/2f;val cy=b.y+b.height/2f;val radius=b.width*.46f;val width=radius*(.58f+.42f*abs(cos(phase)));r.color=Color(.98f,.72f,.12f,1f);r.ellipse(cx-width,cy-radius,width*2f,radius*2f);r.color=Color(1f,.90f,.42f,1f);r.ellipse(cx-width*.55f,cy-radius*.70f,width*.45f,radius*1.05f)}

    fun tile(r:ShapeRenderer,b:Rectangle,top:Boolean){r.color=if(top)grass else dirt;r.rect(b.x,b.y,b.width,b.height);r.color=Color(0f,0f,0f,.08f);r.rect(b.x,b.y,b.width,3f);if(top){r.color=Color(.34f,.67f,.40f,1f);r.rect(b.x,b.y+b.height-5f,b.width,5f)}}

    /** Standing pipe obstacle. The collision shape remains rectangular while the artwork is rounded/vector styled. */
    fun pipe(r:ShapeRenderer,b:Rectangle){
        val x=b.x;val y=b.y;val w=b.width;val h=b.height
        r.color=Color(.10f,.46f,.29f,1f);r.rect(x+w*.10f,y,w*.80f,h*.88f)
        r.color=Color(.14f,.59f,.35f,1f);r.rect(x,y+h*.82f,w,h*.18f)
        r.color=Color(.24f,.72f,.43f,1f);r.rect(x+w*.10f,y+h*.84f,w*.20f,h*.13f)
        r.color=Color(0f,0f,0f,.10f);r.rect(x+w*.78f,y,w*.12f,h*.82f)
    }

    fun button(r:ShapeRenderer,b:Rectangle,pressed:Boolean,accent:Color=Color(.10f,.14f,.20f,1f),alpha:Float=.34f){val lift=if(pressed)0f else 2f;r.color=Color(0f,0f,0f,.10f);r.rect(b.x,b.y,b.width,b.height*.84f);r.color=Color(accent.r,accent.g,accent.b,alpha);r.rect(b.x,b.y+lift,b.width,b.height*.84f);r.color=Color(1f,1f,1f,.12f);r.rect(b.x+5f,b.y+b.height*.72f+lift,b.width-10f,2f)}
    fun pause(r:ShapeRenderer,b:Rectangle,pressed:Boolean){button(r,b,pressed,Color(.08f,.11f,.16f,1f),.36f);r.color=Color(1f,1f,1f,.88f);r.rect(b.x+b.width*.30f,b.y+b.height*.27f,b.width*.11f,b.height*.36f);r.rect(b.x+b.width*.59f,b.y+b.height*.27f,b.width*.11f,b.height*.36f)}
    fun leftIcon(r:ShapeRenderer,b:Rectangle){r.color=Color(1f,1f,1f,.92f);r.triangle(b.x+b.width*.66f,b.y+b.height*.28f,b.x+b.width*.34f,b.y+b.height*.50f,b.x+b.width*.66f,b.y+b.height*.72f)}
    fun rightIcon(r:ShapeRenderer,b:Rectangle){r.color=Color(1f,1f,1f,.92f);r.triangle(b.x+b.width*.34f,b.y+b.height*.28f,b.x+b.width*.66f,b.y+b.height*.50f,b.x+b.width*.34f,b.y+b.height*.72f)}
    fun jumpIcon(r:ShapeRenderer,b:Rectangle){r.color=Color(1f,1f,1f,.94f);r.triangle(b.x+b.width*.50f,b.y+b.height*.76f,b.x+b.width*.24f,b.y+b.height*.46f,b.x+b.width*.76f,b.y+b.height*.46f);r.rect(b.x+b.width*.43f,b.y+b.height*.22f,b.width*.14f,b.height*.28f)}
    fun restartIcon(r:ShapeRenderer,b:Rectangle){r.color=Color.WHITE;r.arc(b.x+b.width*.50f,b.y+b.height*.50f,b.width*.24f,40f,285f);r.triangle(b.x+b.width*.73f,b.y+b.height*.67f,b.x+b.width*.75f,b.y+b.height*.48f,b.x+b.width*.57f,b.y+b.height*.58f)}
    fun homeIcon(r:ShapeRenderer,b:Rectangle){r.color=Color.WHITE;r.triangle(b.x+b.width*.20f,b.y+b.height*.52f,b.x+b.width*.50f,b.y+b.height*.78f,b.x+b.width*.80f,b.y+b.height*.52f);r.rect(b.x+b.width*.30f,b.y+b.height*.24f,b.width*.40f,b.height*.30f)}
}
