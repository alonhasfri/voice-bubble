package com.voicebubble.desktop

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.swing.SwingUtilities

/** Looks for a newer build (the same releases as the phone app) and offers it. */
object Updates {
    /** A newer build, when there is one (shown on Home). */
    @Volatile var available: Int? = null

    const val DOWNLOAD = "https://github.com/alonhasfri/voice-bubble/releases/latest/download/VoiceBubble-Setup.exe"

    fun check(quiet: Boolean) = Thread {
        val latest = runCatching {
            val c = URL("https://api.github.com/repos/alonhasfri/voice-bubble/releases/latest").openConnection() as HttpURLConnection
            c.connectTimeout = 8000; c.readTimeout = 8000
            c.setRequestProperty("Accept", "application/vnd.github+json")
            JSONObject(c.inputStream.bufferedReader().use { it.readText() }).optString("tag_name").substringAfterLast('-').toIntOrNull()
        }.getOrNull()
        SwingUtilities.invokeLater {
            when {
                latest == null -> if (!quiet) Dlg.message("Updates", "Couldn't check for updates (no internet?)")
                App.version in 1 until latest && quiet -> { available = latest; MainWindow.refresh() }
                App.version in 1 until latest -> {
                    available = latest
                    Dlg.confirm("Update", "Build $latest of Voice Bubble is out (you have ${App.version}). Download it? Run VoiceBubble-Setup.exe; your settings stay.", "Download") { Settings.browse(DOWNLOAD) }
                }
                else -> if (!quiet) Dlg.message("Updates", "You have the newest version ✓")
            }
        }
    }.apply { isDaemon = true }.start()
}
