package com.yourgame.mario.audio

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import kotlin.math.PI
import kotlin.math.sin

/** Original runtime-generated arcade sound bank with persistent volume controls. */
class SoundManager {
    private val sounds = HashMap<String, Sound>()
    private val directory = Gdx.files.local(".maryou_sounds")
    private val prefs = Gdx.app.getPreferences("maryou_settings")

    var masterVolume: Float = prefs.getFloat("sfxVolume", 0.45f).coerceIn(0f, 1f)
        private set
    var muted: Boolean = prefs.getBoolean("sfxMuted", false)
        private set

    init {
        directory.mkdirs()
        load("jump", 0.16f, 520f, 820f)
        load("coin", 0.12f, 880f, 1320f)
        load("stomp", 0.13f, 180f, 90f)
        load("hit", 0.20f, 150f, 55f)
        load("step", 0.07f, 95f, 70f)
        load("milestone", 0.24f, 660f, 990f)
        load("gameover", 0.48f, 420f, 110f)
    }

    private fun load(name: String, duration: Float, startHz: Float, endHz: Float) {
        try {
            val file = directory.child("$name.wav")
            if (!file.exists()) file.writeBytes(makeWav(duration, startHz, endHz), false)
            sounds[name] = Gdx.audio.newSound(file)
        } catch (_: Throwable) {
            // Audio is optional so gameplay remains functional on devices without a working backend.
        }
    }

    private fun makeWav(duration: Float, startHz: Float, endHz: Float): ByteArray {
        val sampleRate = 22050
        val count = (duration * sampleRate).toInt()
        val pcm = ByteArray(count * 2)
        for (i in 0 until count) {
            val t = i.toFloat() / sampleRate
            val progress = i.toFloat() / count.coerceAtLeast(1)
            val hz = startHz + (endHz - startHz) * progress
            val envelope = (1f - progress).coerceAtLeast(0f) * (0.75f + 0.25f * sin(PI * progress)).toFloat()
            val sample = (sin(2.0 * PI * hz * t) * 0.55 * envelope * Short.MAX_VALUE)
                .toInt().coerceIn(-32768, 32767)
            pcm[i * 2] = (sample and 0xff).toByte()
            pcm[i * 2 + 1] = ((sample ushr 8) and 0xff).toByte()
        }
        val out = ByteArrayOutputStream(44 + pcm.size)
        val data = DataOutputStream(out)
        fun le16(v: Int) { data.writeByte(v and 255); data.writeByte((v ushr 8) and 255) }
        fun le32(v: Int) { le16(v and 65535); le16(v ushr 16) }
        data.writeBytes("RIFF"); le32(36 + pcm.size); data.writeBytes("WAVE")
        data.writeBytes("fmt "); le32(16); le16(1); le16(1); le32(sampleRate); le32(sampleRate * 2); le16(2); le16(16)
        data.writeBytes("data"); le32(pcm.size); data.write(pcm); data.flush()
        return out.toByteArray()
    }

    fun setMasterVolume(value: Float) {
        masterVolume = value.coerceIn(0f, 1f)
        prefs.putFloat("sfxVolume", masterVolume).flush()
    }

    fun setMuted(value: Boolean) {
        muted = value
        prefs.putBoolean("sfxMuted", muted).flush()
    }

    fun play(name: String, volume: Float = 1f) {
        if (muted) return
        sounds[name]?.play((volume * masterVolume).coerceIn(0f, 1f))
    }

    fun dispose() {
        sounds.values.forEach { it.dispose() }
        sounds.clear()
    }
}
