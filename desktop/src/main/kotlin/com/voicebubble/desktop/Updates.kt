package com.voicebubble.desktop

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

/** Looks for a newer build (the same releases as the phone app) and offers it. */
object Updates {
    const val DOWNLOAD = "https://github.com/alonhasfri/voice-bubble/releases/latest"

    fun check(quiet: Boolean) = Thread {
        val latest = runCatching {
            val c = URL("https://api.github.com/repos/alonhasfri/voice-bubble/releases/latest").openConnection() as HttpURLConnection
            c.connectTimeout = 8000; c.readTimeout = 8000
            c.setRequestProperty("Accept", "application/vnd.github+json")
            JSONObject(c.inputStream.bufferedReader().use { it.readText() }).optString("tag_name").substringAfterLast('-').toIntOrNull()
        }.getOrNull()
        SwingUtilities.invokeLater {
            when {
                latest == null -> if (!quiet) JOptionPane.showMessageDialog(null, "Couldn't check for updates (no internet?)")
                App.version in 1 until latest -> {
                    val r = JOptionPane.showConfirmDialog(null, "Build $latest of Voice Bubble is out (you have ${App.version}).\n" +
                        "Open the download page? Run VoiceBubble-Setup.exe from there; your settings stay.", "Update", JOptionPane.YES_NO_OPTION)
                    if (r == JOptionPane.YES_OPTION) Settings.browse(DOWNLOAD)
                }
                else -> if (!quiet) JOptionPane.showMessageDialog(null, "You have the newest version ✓")
            }
        }
    }.apply { isDaemon = true }.start()
}
