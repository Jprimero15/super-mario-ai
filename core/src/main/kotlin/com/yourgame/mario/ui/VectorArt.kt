package com.yourgame.mario.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import kotlin.math.abs
import kotlin.math.cos

/** Clean vector-style artwork. Shapes are resolution independent and asset-free. */
object VectorArt {
    fun player(r: ShapeRenderer,b: Rectangle,facingRight:Boolean,big:Boolean,dead:Boolean){val x=b.x;val y=b.y;val w=b.width;val h=b.height
        if(dead){r.color=Color(.45f,.48f,.53f,1f);r.rect(x,y,w,h*.72f);return}
        r.color=Color(0f,0f,0f,.16f);r.ellipse(x+w*.12f,y-3f,w*.76f,7f);r.color=Color(.28f,.09f,.05f,1f);r.ellipse(x+w*.02f,y,w*.45f,h*.20f);r.ellipse(x+w*.53f,y,w*.45f,h*.20f)
        r.color=Color(.08f,.30f,.72f,1f);r.rect(x+w*.18f,y+h*.18f,w*.64f,h*.43f);r.color=Color(.88f,.08f,.06f,1f);r.rect(x+w*.08f,y+h*.40f,w*.84f,h*.32f);r.color=Color(1f,.70f,.48f,1f);r.ellipse(x+w*.16f,y+h*.55f,w*.68f,h*.36f)
        r.color=Color(.90f,.06f,.05f,1f);r.ellipse(x+w*.10f,y+h*.75f,w*.78f,h*.22f);r.rect(x+w*.22f,y+h*.77f,w*.58f,h*.13f);r.color=Color(.22f,.09f,.04f,1f);r.ellipse(x+w*.39f,y+h*.62f,w*.28f,h*.10f);r.ellipse(x+w*.52f,y+h*.70f,w*.08f,h*.06f)
        r.color=Color.BLACK;val eyeX=if(facingRight)x+w*.63f else x+w*.31f;r.circle(eyeX,y+h*.72f,w*.035f);if(big){r.color=Color(1f,1f,1f,.14f);r.rect(x,y,w,h)}
    }
    fun enemy(r:ShapeRenderer,b:Rectangle){val x=b.x;val y=b.y;val w=b.width;val h=b.height;r.color=Color(.35f,.16f,.07f,1f);r.ellipse(x,y+h*.18f,w,h*.65f);r.color=Color(.72f,.31f,.10f,1f);r.ellipse(x+w*.10f,y+h*.35f,w*.80f,h*.50f);r.color=Color.WHITE;r.ellipse(x+w*.18f,y+h*.48f,w*.25f,h*.22f);r.ellipse(x+w*.57f,y+h*.48f,w*.25f,h*.22f);r.color=Color.BLACK;r.circle(x+w*.31f,y+h*.57f,w*.05f);r.circle(x+w*.69f,y+h*.57f,w*.05f);r.color=Color(.20f,.07f,.03f,1f);r.rect(x+w*.05f,y,w*.35f,h*.22f);r.rect(x+w*.60f,y,w*.35f,h*.22f)}
    fun coin(r:ShapeRenderer,b:Rectangle,phase:Float){val cx=b.x+b.width/2f;val cy=b.y+b.height/2f;val radius=b.width*.46f;val width=radius*(.58f+.42f*abs(cos(phase)));r.color=Color(.98f,.69f,.08f,1f);r.ellipse(cx-width,cy-radius,width*2f,radius*2f);r.color=Color(1f,.88f,.30f,1f);r.ellipse(cx-width*.55f,cy-radius*.70f,width*.45f,radius*1.05f)}
    fun tile(r:ShapeRenderer,b:Rectangle,top:Boolean){r.color=if(top)Color(.35f,.65f,.18f,1f) else Color(.48f,.27f,.12f,1f);r.rect(b.x,b.y,b.width,b.height);r.color=Color(0f,0f,0f,.10f);r.rect(b.x,b.y,b.width,3f);if(top){r.color=Color(.53f,.78f,.25f,1f);r.rect(b.x,b.y+b.height-5f,b.width,5f)}}
    fun button(r:ShapeRenderer,b:Rectangle,pressed:Boolean,accent:Color=Color(.12f,.16f,.22f,1f),alpha:Float=.48f){val lift=if(pressed)0f else 3f;r.color=Color(0f,0f,0f,.14f);r.rect(b.x,b.y,b.width,b.height*.84f);r.color=Color(accent.r,accent.g,accent.b,alpha);r.rect(b.x,b.y+lift,b.width,b.height*.84f);r.color=Color(1f,1f,1f,.16f);r.rect(b.x+5f,b.y+b.height*.72f+lift,b.width-10f,2f)}
    fun pause(r:ShapeRenderer,b:Rectangle,pressed:Boolean){button(r,b,pressed,Color(.08f,.11f,.16f,1f),.42f);r.color=Color(1f,1f,1f,.90f);r.rect(b.x+b.width*.30f,b.y+b.height*.27f,b.width*.11f,b.height*.36f);r.rect(b.x+b.width*.59f,b.y+b.height*.27f,b.width*.11f,b.height*.36f)}
    fun leftIcon(r:ShapeRenderer,b:Rectangle){r.color=Color(1f,1f,1f,.92f);r.triangle(b.x+b.width*.66f,b.y+b.height*.28f,b.x+b.width*.34f,b.y+b.height*.50f,b.x+b.width*.66f,b.y+b.height*.72f)}
    fun rightIcon(r:ShapeRenderer,b:Rectangle){r.color=Color(1f,1f,1f,.92f);r.triangle(b.x+b.width*.34f,b.y+b.height*.28f,b.x+b.width*.66f,b.y+b.height*.50f,b.x+b.width*.34f,b.y+b.height*.72f)}
    fun jumpIcon(r:ShapeRenderer,b:Rectangle){r.color=Color(1f,1f,1f,.94f);r.triangle(b.x+b.width*.50f,b.y+b.height*.76f,b.x+b.width*.24f,b.y+b.height*.46f,b.x+b.width*.76f,b.y+b.height*.46f);r.rect(b.x+b.width*.43f,b.y+b.height*.22f,b.width*.14f,b.height*.28f)}
    fun playIcon(r:ShapeRenderer,b:Rectangle){r.color=Color(1f,1f,1f,.95f);r.triangle(b.x+b.width*.38f,b.y+b.height*.28f,b.x+b.width*.38f,b.y+b.height*.72f,b.x+b.width*.70f,b.y+b.height*.50f)}
    fun restartIcon(r:ShapeRenderer,b:Rectangle){r.color=Color.WHITE;r.arc(b.x+b.width*.50f,b.y+b.height*.50f,b.width*.24f,40f,285f);r.triangle(b.x+b.width*.73f,b.y+b.height*.67f,b.x+b.width*.75f,b.y+b.height*.48f,b.x+b.width*.57f,b.y+b.height*.58f)}
    fun homeIcon(r:ShapeRenderer,b:Rectangle){r.color=Color.WHITE;r.triangle(b.x+b.width*.20f,b.y+b.height*.52f,b.x+b.width*.50f,b.y+b.height*.78f,b.x+b.width*.80f,b.y+b.height*.52f);r.rect(b.x+b.width*.30f,b.y+b.height*.24f,b.width*.40f,b.height*.30f)}
}
