package com.voicebubble.desktop

import java.awt.Color
import java.awt.Font
import java.awt.GraphicsEnvironment

/** The phone app's look (Ui.kt): the same light/dark colours, icon-tile tones and type sizes. */
object Theme {
    class Pal(
        val bg: Color, val card: Color, val border: Color, val ink: Color, val muted: Color,
        val accent: Color, val accentInk: Color, val accentSoft: Color, val track: Color, val divider: Color,
        val danger: Color, val good: Color, val field: Color, val hint: Color,
    )

    private fun c(hex: Long) = Color(hex.toInt(), true)

    private val darkPal = Pal(
        bg = c(0xFF0F1220), card = c(0xFF1A1F33), border = c(0xFF2A3048), ink = c(0xFFEEF1FA),
        muted = c(0xFFA3ABC2), accent = c(0xFF3A5BD9), accentInk = c(0xFFA9BBFF), accentSoft = c(0xFF263266),
        track = c(0xFF2A3048), divider = c(0xFF262C42), danger = c(0xFFFF9DA2), good = c(0xFF7FD9A6),
        field = c(0xFF141829), hint = c(0xFF6E7690),
    )
    private val lightPal = Pal(
        bg = c(0xFFF4F6FB), card = c(0xFFFFFFFF), border = c(0xFFE3E7F1), ink = c(0xFF141A2E),
        muted = c(0xFF5B6378), accent = c(0xFF3A5BD9), accentInk = c(0xFF2B47B8), accentSoft = c(0xFFE3E9FF),
        track = c(0xFFE6EAF5), divider = c(0xFFEEF0F6), danger = c(0xFFA8262C), good = c(0xFF17663A),
        field = c(0xFFF4F6FB), hint = c(0xFF8A92A8),
    )

    /** "system", "light" or "dark" (the same setting as on the phone). */
    var mode: String
        get() = App.getSharedPreferences("voice", 0).getString("theme", "system") ?: "system"
        set(v) { App.getSharedPreferences("voice", 0).edit().putString("theme", v).apply(); systemDark = null }

    private var systemDark: Boolean? = null
    private fun windowsDark(): Boolean = systemDark ?: runCatching {
        val p = ProcessBuilder("reg", "query", "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize", "/v", "AppsUseLightTheme")
            .redirectErrorStream(true).start()
        val out = p.inputStream.bufferedReader().readText()
        out.contains("0x0")
    }.getOrDefault(false).also { systemDark = it }

    val dark: Boolean get() = when (mode) { "dark" -> true; "light" -> false; else -> App.isWindows && windowsDark() }
    val p: Pal get() = if (dark) darkPal else lightPal

    /** Icon-tile colours (background, icon) for a named tone. */
    fun tone(name: String): Pair<Color, Color> = if (dark) when (name) {
        "purple" -> c(0xFF34245E) to c(0xFFC9B2FF); "orange" -> c(0xFF4A2A1C) to c(0xFFFFB08A)
        "green" -> c(0xFF1D3A2A) to c(0xFF7FD9A6); "teal" -> c(0xFF163646) to c(0xFF8FD3F5)
        "red" -> c(0xFF4A1E22) to c(0xFFFF9DA2); "gray" -> c(0xFF2A3048) to c(0xFFC3C9DA)
        "gold" -> c(0xFF3F3218) to c(0xFFF2C66B); else -> c(0xFF263266) to c(0xFFA9BBFF)
    } else when (name) {
        "purple" -> c(0xFFECE3FF) to c(0xFF5A2FC7); "orange" -> c(0xFFFDE6DE) to c(0xFFB23A0B)
        "green" -> c(0xFFE3F4EA) to c(0xFF17663A); "teal" -> c(0xFFDDF0FB) to c(0xFF0E6A9E)
        "red" -> c(0xFFFBE3E4) to c(0xFFA8262C); "gray" -> c(0xFFEEF0F6) to c(0xFF3A4157)
        "gold" -> c(0xFFFFF0D1) to c(0xFF8A5A00); else -> c(0xFFE3E9FF) to c(0xFF2B47B8)
    }

    private val family: String by lazy {
        val have = GraphicsEnvironment.getLocalGraphicsEnvironment().availableFontFamilyNames.toSet()
        listOf("Segoe UI Variable Text", "Segoe UI", "Roboto", "Noto Sans", "DejaVu Sans").firstOrNull { it in have } ?: Font.SANS_SERIF
    }
    private val semibold: String by lazy {
        val have = GraphicsEnvironment.getLocalGraphicsEnvironment().availableFontFamilyNames.toSet()
        listOf("Segoe UI Semibold", "Segoe UI Variable Display Semib").firstOrNull { it in have } ?: family
    }
    private val emoji: String? by lazy {
        GraphicsEnvironment.getLocalGraphicsEnvironment().availableFontFamilyNames.firstOrNull { it == "Segoe UI Emoji" }
    }

    /** The phone's sizes are in sp; on a computer one sp is about one pixel at 100 % scaling. */
    fun font(size: Float, bold: Boolean): Font =
        if (bold && semibold != family) Font(semibold, Font.PLAIN, 1).deriveFont(size)
        else Font(family, if (bold) Font.BOLD else Font.PLAIN, 1).deriveFont(size)

    fun emojiFont(size: Float): Font? = emoji?.let { Font(it, Font.PLAIN, 1).deriveFont(size) }

    fun alpha(c: Color, a: Int) = Color(c.red, c.green, c.blue, a)
}
