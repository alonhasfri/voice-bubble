package com.voicebubble.desktop

import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.Mixer
import javax.sound.sampled.TargetDataLine

/** The microphone: 16 kHz mono, what Whisper wants. */
class Mic {
    companion object {
        const val RATE = 16000
        private val format = AudioFormat(RATE.toFloat(), 16, 1, true, false)
        /** At most 30 minutes in one go. */
        private const val MAX = RATE * 60 * 30

        /** Microphones Windows knows about (names), for Settings. */
        fun names(): List<String> = AudioSystem.getMixerInfo().filter { info ->
            runCatching { AudioSystem.getMixer(info).isLineSupported(DataLine.Info(TargetDataLine::class.java, format)) }.getOrDefault(false)
        }.map { it.name }.distinct()

        private fun open(): TargetDataLine {
            val want = App.micName
            val info = DataLine.Info(TargetDataLine::class.java, format)
            val mixer: Mixer? = want?.let { n -> AudioSystem.getMixerInfo().firstOrNull { it.name == n }?.let { AudioSystem.getMixer(it) } }
            val line = (mixer?.takeIf { it.isLineSupported(info) }?.getLine(info) ?: AudioSystem.getLine(info)) as TargetDataLine
            line.open(format, RATE / 5 * 2)
            return line
        }
    }

    private var line: TargetDataLine? = null
    private var samples = FloatArray(RATE * 60)
    @Volatile private var size = 0
    @Volatile var level = 0f; private set
    @Volatile private var running = false
    private var thread: Thread? = null

    /** Starts listening. Throws when there's no microphone. */
    fun start() {
        val l = open()
        line = l; size = 0; running = true
        l.start()
        thread = Thread({
            val buf = ByteArray(RATE / 10 * 2)
            while (running) {
                val n = l.read(buf, 0, buf.size)
                if (n <= 0) continue
                var sum = 0.0
                synchronized(this) {
                    for (i in 0 until n / 2) {
                        val v = ((buf[2 * i + 1].toInt() shl 8) or (buf[2 * i].toInt() and 0xFF)).toShort() / 32768f
                        if (size >= MAX) break
                        if (size >= samples.size) samples = samples.copyOf(minOf(MAX, samples.size * 2))
                        samples[size++] = v
                        sum += v * v
                    }
                }
                level = Math.sqrt(sum / (n / 2).coerceAtLeast(1)).toFloat()
            }
        }, "mic").apply { isDaemon = true; start() }
    }

    val seconds get() = size / RATE.toFloat()

    /** Stops and gives the recording. */
    fun stop(): FloatArray {
        running = false
        runCatching { line?.stop(); line?.close() }
        thread?.join(500)
        line = null; level = 0f
        return synchronized(this) { samples.copyOf(size) }
    }
}
