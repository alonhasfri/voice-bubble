package com.voicebubble.desktop

import android.content.Context
import com.voicebubble.Prefs
import java.io.File

/** Voice Bubble's home on Windows: settings, models and skins live in %APPDATA%\VoiceBubble. */
object App : Context(appFolder()) {
    val prefs by lazy { Prefs(this) }

    /** This build's number (the same numbers as the phone app), 0 when run from source. */
    val version: Int by lazy { App::class.java.`package`?.implementationVersion?.toIntOrNull() ?: 0 }

    val isWindows = System.getProperty("os.name").lowercase().contains("win")

    /** Desktop-only settings (kept next to the phone-style ones). */
    private val desk get() = getSharedPreferences("desktop", MODE_PRIVATE)
    var hotkey: String
        get() = desk.getString("hotkey", "ctrl+shift+space")!!
        set(v) = desk.edit().putString("hotkey", v).apply()
    var holdToTalk: Boolean
        get() = desk.getBoolean("hold", false)
        set(v) = desk.edit().putBoolean("hold", v).apply()
    var showBubble: Boolean
        get() = desk.getBoolean("showBubble", true)
        set(v) = desk.edit().putBoolean("showBubble", v).apply()
    var bubblePx: Int
        get() = desk.getInt("bubblePx", 64)
        set(v) = desk.edit().putInt("bubblePx", v).apply()
    var bubbleX: Int
        get() = desk.getInt("bx", -1)
        set(v) = desk.edit().putInt("bx", v).apply()
    var bubbleY: Int
        get() = desk.getInt("by", -1)
        set(v) = desk.edit().putInt("by", v).apply()
    var firstRunDone: Boolean
        get() = desk.getBoolean("firstRun", false)
        set(v) = desk.edit().putBoolean("firstRun", v).apply()
    var micName: String?
        get() = desk.getString("mic", null)
        set(v) = desk.edit().putString("mic", v).apply()
}

private fun appFolder(): File {
    val base = System.getenv("APPDATA")?.let { File(it) } ?: File(System.getProperty("user.home"), ".config")
    var dir = File(base, "VoiceBubble")
    // The speech engine can't open files in folders with non-English letters (e.g. a Hebrew user name): use the
    // shared Public folder then.
    if (dir.path.any { it.code > 127 }) dir = File(System.getenv("PUBLIC") ?: "C:\\Users\\Public", "VoiceBubble")
    return dir.apply { mkdirs() }
}
