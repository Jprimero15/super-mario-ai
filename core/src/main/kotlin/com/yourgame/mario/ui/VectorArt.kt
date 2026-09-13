package com.yourgame.mario.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.yourgame.mario.entities.WalkerEnemy
import com.yourgame.mario.world.MonsterTier
import kotlin.math.abs
import kotlin.math.cos

/** Resolution-independent vector artwork; no bitmap assets required. */
object VectorArt {
    fun player(r: ShapeRenderer, b: Rectangle, facingRight: Boolean, big: Boolean, dead: Boolean) {
        val x=b.x; val y=b.y; val w=b.width; val h=b.height
        if(dead){r.color=Color(.35f,.40f,.48f,1f);r.rect(x,y,w,h*.72f);return}
        r.color=Color(0f,0f,0f,.16f);r.ellipse(x+w*.10f,y-3f,w*.80f,7f)
        r.color=Color(.25f,.09f,.05f,1f);r.ellipse(x+w*.02f,y,w*.45f,h*.20f);r.ellipse(x+w*.53f,y,w*.45f,h*.20f)
        r.color=Color(.12f,.32f,.72f,1f);r.rect(x+w*.18f,y+h*.18f,w*.64f,h*.43f)
        r.color=Color(.92f,.16f,.13f,1f);r.rect(x+w*.08f,y+h*.40f,w*.84f,h*.32f)
        r.color=Color(1f,.72f,.52f,1f);r.ellipse(x+w*.16f,y+h*.55f,w*.68f,h*.36f)
        r.color=Color(.90f,.09f,.10f,1f);r.ellipse(x+w*.10f,y+h*.75f,w*.78f,h*.22f);r.rect(x+w*.22f,y+h*.77f,w*.58f,h*.13f)
        r.color=Color(.20f,.08f,.04f,1f);r.ellipse(x+w*.39f,y+h*.62f,w*.28f,h*.10f)
        r.color=Color.BLACK;val eyeX=if(facingRight)x+w*.63f else x+w*.31f;r.circle(eyeX,y+h*.72f,w*.035f)
        if(big){r.color=Color(1f,1f,1f,.10f);r.rect(x,y,w,h)}
    }

    fun monster(r:ShapeRenderer,b:Rectangle,tier:MonsterTier,dead:Boolean=false){
        if(dead){r.color=Color(.34f,.38f,.45f,1f);r.ellipse(b.x,b.y,b.width,b.height*.35f);return}
        val cx=b.x+b.width/2f
        val body=when(tier){MonsterTier.SMALL->Color(.46f,.20f,.58f,1f);MonsterTier.MEDIUM->Color(.72f,.25f,.42f,1f);MonsterTier.LARGE->Color(.76f,.31f,.16f,1f)}
        r.color=Color(0f,0f,0f,.15f);r.ellipse(b.x,b.y-3f,b.width,7f)
        r.color=body;r.ellipse(b.x,b.y,b.width,b.height*.86f)
        r.color=Color(.95f,.80f,.72f,1f);r.circle(b.x+b.width*.32f,b.y+b.height*.55f,b.width*.09f);r.circle(b.x+b.width*.68f,b.y+b.height*.55f,b.width*.09f)
        r.color=Color(.08f,.05f,.12f,1f);r.circle(b.x+b.width*.32f,b.y+b.height*.55f,b.width*.04f);r.circle(b.x+b.width*.68f,b.y+b.height*.55f,b.width*.04f)
        r.color=Color(.14f,.06f,.16f,1f);r.rect(cx-b.width*.20f,b.y+b.height*.25f,b.width*.40f,b.height*.08f)
        if(tier==MonsterTier.LARGE){r.color=Color(1f,.72f,.25f,1f);r.circle(b.x+b.width*.18f,b.y+b.height*.83f,b.width*.10f);r.circle(b.x+b.width*.82f,b.y+b.height*.83f,b.width*.10f)}
    }

    fun coin(r:ShapeRenderer,b:Rectangle,phase:Float){val cx=b.x+b.width/2f;val cy=b.y+b.height/2f;val radius=b.width*.46f;val width=radius*(.58f+.42f*abs(cos(phase)));r.color=Color(.98f,.72f,.12f,1f);r.ellipse(cx-width,cy-radius,width*2f,radius*2f);r.color=Color(1f,.92f,.48f,1f);r.ellipse(cx-width*.55f,cy-radius*.70f,width*.45f,radius*1.05f)}

    fun tile(r:ShapeRenderer,b:Rectangle,top:Boolean){r.color=if(top)Color(.22f,.58f,.38f,1f) else Color(.34f,.24f,.18f,1f);r.rect(b.x,b.y,b.width,b.height);r.color=Color(0f,0f,0f,.08f);r.rect(b.x,b.y,b.width,3f);if(top){r.color=Color(.38f,.72f,.44f,1f);r.rect(b.x,b.y+b.height-5f,b.width,5f)}}

    fun pipe(r:ShapeRenderer,b:Rectangle){
        val x=b.x;val y=b.y;val w=b.width;val h=b.height
        r.color=Color(0f,0f,0f,.14f);r.ellipse(x-2f,y-4f,w+4f,9f)
        r.color=Color(.08f,.45f,.29f,1f);r.rect(x+w*.10f,y,w*.80f,h*.88f)
        r.color=Color(.16f,.63f,.38f,1f);r.rect(x,y+h*.82f,w,h*.18f)
        r.color=Color(.33f,.76f,.48f,1f);r.rect(x+w*.10f,y+h*.84f,w*.18f,h*.13f)
        r.color=Color(0f,0f,0f,.12f);r.rect(x+w*.78f,y,w*.12f,h*.82f)
        r.color=Color(.06f,.25f,.17f,.85f);r.ellipse(x+w*.14f,y+h*.80f,w*.72f,h*.10f)
    }

    fun hole(r:ShapeRenderer,b:Rectangle){
        r.color=Color(0f,0f,0f,.38f);r.ellipse(b.x-3f,b.y-2f,b.width+6f,15f)
        r.color=Color(.025f,.035f,.06f,1f);r.rect(b.x,b.y,b.width,16f)
        r.color=Color(.10f,.07f,.09f,1f);r.ellipse(b.x,b.y-1f,b.width,18f)
        r.color=Color(1f,1f,1f,.06f);r.rect(b.x+5f,b.y+11f,b.width-10f,2f)
    }

    fun button(r:ShapeRenderer,b:Rectangle,pressed:Boolean,accent:Color=Color(.10f,.14f,.20f,1f),alpha:Float=.34f){val lift=if(pressed)0f else 2f;r.color=Color(0f,0f,0f,.14f);r.rect(b.x,b.y,b.width,b.height*.84f);r.color=Color(accent.r,accent.g,accent.b,alpha);r.rect(b.x,b.y+lift,b.width,b.height*.84f);r.color=Color(1f,1f,1f,.12f);r.rect(b.x+5f,b.y+b.height*.72f+lift,b.width-10f,2f)}
    fun pause(r:ShapeRenderer,b:Rectangle,pressed:Boolean){button(r,b,pressed,Color(.08f,.11f,.16f,1f),.36f);r.color=Color(1f,1f,1f,.88f);r.rect(b.x+b.width*.30f,b.y+b.height*.27f,b.width*.11f,b.height*.36f);r.rect(b.x+b.width*.59f,b.y+b.height*.27f,b.width*.11f,b.height*.36f)}
    fun leftIcon(r:ShapeRenderer,b:Rectangle){r.color=Color(1f,1f,1f,.92f);r.triangle(b.x+b.width*.66f,b.y+b.height*.28f,b.x+b.width*.34f,b.y+b.height*.50f,b.x+b.width*.66f,b.y+b.height*.72f)}
    fun rightIcon(r:ShapeRenderer,b:Rectangle){r.color=Color(1f,1f,1f,.92f);r.triangle(b.x+b.width*.34f,b.y+b.height*.28f,b.x+b.width*.66f,b.y+b.height*.50f,b.x+b.width*.34f,b.y+b.height*.72f)}
    fun jumpIcon(r:ShapeRenderer,b:Rectangle){r.color=Color(1f,1f,1f,.94f);r.triangle(b.x+b.width*.50f,b.y+b.height*.76f,b.x+b.width*.24f,b.y+b.height*.46f,b.x+b.width*.76f,b.y+b.height*.46f);r.rect(b.x+b.width*.43f,b.y+b.height*.22f,b.width*.14f,b.height*.28f)}
    fun restartIcon(r:ShapeRenderer,b:Rectangle){r.color=Color.WHITE;r.arc(b.x+b.width*.50f,b.y+b.height*.50f,b.width*.24f,40f,285f);r.triangle(b.x+b.width*.73f,b.y+b.height*.67f,b.x+b.width*.75f,b.y+b.height*.48f,b.x+b.width*.57f,b.y+b.height*.58f)}
    fun homeIcon(r:ShapeRenderer,b:Rectangle){r.color=Color.WHITE;r.triangle(b.x+b.width*.20f,b.y+b.height*.52f,b.x+b.width*.50f,b.y+b.height*.78f,b.x+b.width*.80f,b.y+b.height*.52f);r.rect(b.x+b.width*.30f,b.y+b.height*.24f,b.width*.40f,b.height*.30f)}
}
