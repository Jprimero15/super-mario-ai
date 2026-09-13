package com.yourgame.mario.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle

/** Dependency-free vector-style game art. All shapes scale cleanly with the viewport. */
object VectorArt {
    fun player(r: ShapeRenderer, b: Rectangle, facingRight: Boolean, big: Boolean, dead: Boolean) {
        val x = b.x; val y = b.y; val w = b.width; val h = b.height
        if (dead) { r.color = Color(0.55f, 0.58f, 0.62f, 1f); r.rect(x, y, w, h * .72f); return }
        // shadow
        r.color = Color(0f, 0f, 0f, .18f); r.ellipse(x + w*.12f, y - 3f, w*.76f, 7f)
        // boots
        r.color = Color(0.28f, 0.09f, 0.05f, 1f)
        r.ellipse(x + w*.02f, y, w*.45f, h*.20f); r.ellipse(x + w*.53f, y, w*.45f, h*.20f)
        // overalls/body
        r.color = Color(0.08f, 0.30f, 0.72f, 1f); r.rect(x+w*.18f, y+h*.18f, w*.64f, h*.43f)
        // shirt
        r.color = Color(0.88f, 0.08f, 0.06f, 1f); r.rect(x+w*.08f, y+h*.40f, w*.84f, h*.32f)
        // face
        r.color = Color(1f, .70f, .48f, 1f); r.ellipse(x+w*.16f, y+h*.55f, w*.68f, h*.36f)
        // cap
        r.color = Color(0.90f, .06f, .05f, 1f); r.ellipse(x+w*.10f, y+h*.75f, w*.78f, h*.22f); r.rect(x+w*.22f, y+h*.77f, w*.58f, h*.13f)
        // moustache / nose / eye
        r.color = Color(0.22f, .09f, .04f, 1f); r.ellipse(x+w*.39f, y+h*.62f, w*.28f, h*.10f); r.ellipse(x+w*.52f, y+h*.70f, w*.08f, h*.06f)
        r.color = Color.BLACK; val eyeX = if (facingRight) x+w*.63f else x+w*.31f; r.circle(eyeX, y+h*.72f, w*.035f)
        if (big) { r.color = Color(1f,1f,1f,.15f); r.rect(x, y, w, h) }
    }

    fun enemy(r: ShapeRenderer, b: Rectangle) {
        val x=b.x; val y=b.y; val w=b.width; val h=b.height
        r.color = Color(0.35f,.16f,.07f,1f); r.ellipse(x, y+h*.18f, w, h*.65f)
        r.color = Color(.72f,.31f,.10f,1f); r.ellipse(x+w*.10f,y+h*.35f,w*.80f,h*.50f)
        r.color = Color.WHITE; r.ellipse(x+w*.18f,y+h*.48f,w*.25f,h*.22f); r.ellipse(x+w*.57f,y+h*.48f,w*.25f,h*.22f)
        r.color = Color.BLACK; r.circle(x+w*.31f,y+h*.57f,w*.05f); r.circle(x+w*.69f,y+h*.57f,w*.05f)
        r.color = Color(.20f,.07f,.03f,1f); r.rect(x+w*.05f,y,w*.35f,h*.22f); r.rect(x+w*.60f,y,w*.35f,h*.22f)
    }

    fun coin(r: ShapeRenderer, b: Rectangle, phase: Float) {
        val cx=b.x+b.width/2f; val cy=b.y+b.height/2f; val radius=b.width*.46f
        val width = radius * (.58f + .42f * kotlin.math.abs(kotlin.math.cos(phase)))
        r.color = Color(.98f,.69f,.08f,1f); r.ellipse(cx-width,cy-radius,width*2f,radius*2f)
        r.color = Color(1f,.88f,.30f,1f); r.ellipse(cx-width*.55f,cy-radius*.70f,width*.45f,radius*1.05f)
    }

    fun tile(r: ShapeRenderer, b: Rectangle, top: Boolean) {
        r.color = if (top) Color(.35f,.65f,.18f,1f) else Color(.48f,.27f,.12f,1f)
        r.rect(b.x,b.y,b.width,b.height)
        r.color = Color(0f,0f,0f,.12f); r.rect(b.x,b.y,b.width,3f)
        if (top) { r.color=Color(.53f,.78f,.25f,1f); r.rect(b.x,b.y+b.height-5f,b.width,5f) }
    }

    fun button(r: ShapeRenderer, b: Rectangle, pressed: Boolean, accent: Color = Color(.12f,.16f,.22f,1f)) {
        val lift = if (pressed) 0f else 5f
        r.color = Color(0f,0f,0f,.28f); r.rect(b.x,b.y,b.width,b.height*.82f)
        r.color = accent; r.rect(b.x,b.y+lift,b.width,b.height*.82f)
        r.color = Color(1f,1f,1f,.12f); r.rect(b.x+4f,b.y+b.height*.72f+lift,b.width-8f,3f)
    }

    fun pause(r: ShapeRenderer, b: Rectangle, pressed: Boolean) {
        button(r,b,pressed,Color(.10f,.13f,.18f,1f))
        r.color=Color.WHITE; r.rect(b.x+b.width*.30f,b.y+b.height*.25f,b.width*.12f,b.height*.38f); r.rect(b.x+b.width*.58f,b.y+b.height*.25f,b.width*.12f,b.height*.38f)
    }
}
