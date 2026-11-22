package com.voicebubble.desktop

import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import kotlin.math.PI
import kotlin.math.sin

/** Two soft notes up when it starts listening, down when it stops (Settings → Bubble → Sounds). */
object Sound {
    private fun play(vararg notes: Double) {
        if (!App.prefs.sound.let { it != "off" }) return
        Thread {
            runCatching {
                val rate = 44100f
                val each = (rate * 0.07).toInt()
                val data = ByteArray(each * notes.size * 2)
                notes.forEachIndexed { n, f ->
                    for (i in 0 until each) {
                        val env = sin(PI * i / each)
                        val v = (sin(2 * PI * f * i / rate) * env * 0.18 * Short.MAX_VALUE).toInt()
                        val k = (n * each + i) * 2
                        data[k] = v.toByte(); data[k + 1] = (v shr 8).toByte()
                    }
                }
                val line = AudioSystem.getSourceDataLine(AudioFormat(rate, 16, 1, true, false))
                line.open(); line.start(); line.write(data, 0, data.size); line.drain(); line.close()
            }
        }.apply { isDaemon = true }.start()
    }
    fun start() = play(660.0, 880.0)
    fun stop() = play(880.0, 660.0)
}
