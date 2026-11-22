package com.voicebubble.desktop

import com.voicebubble.Models
import java.awt.Color
import java.awt.Image
import java.awt.MenuItem
import java.awt.PopupMenu
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.image.BufferedImage
import java.io.File
import java.io.RandomAccessFile
import javax.swing.JOptionPane
import javax.swing.SwingUtilities
import javax.swing.UIManager
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val i = args.indexOf("--transcribe")
    if (i >= 0) {
        Engine.dryRun = { println("TYPED: $it") }
        Engine.onToast = { println("NOTE: $it") }
        Engine.transcribeFile(args[i + 1])
        exitProcess(0)
    }
    Main.run()
    args.indexOf("--settings").takeIf { it >= 0 }?.let { Settings.open(args.getOrNull(it + 1)?.toIntOrNull() ?: 0) }
}

object Main {
    var icon: Image? = null; private set
    private var tray: TrayIcon? = null

    fun run() {
        // Only one Voice Bubble at a time: a second start just opens Settings of the first one.
        val lock = runCatching { RandomAccessFile(File(App.filesDir, "running.lock"), "rw").channel.tryLock() }.getOrNull()
        if (lock == null) {
            JOptionPane.showMessageDialog(null, "Voice Bubble is already running: look for its icon by the clock (bottom-right).")
            exitProcess(0)
        }
        runCatching { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()) }
        icon = BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB).also { img ->
            val g = img.createGraphics()
            SkinPainter.paint(g, DeskSkins.byId("classic").spec, 64); SkinPainter.mic(g, 64, Color.WHITE); g.dispose()
        }
        Engine.onState = { Bubble.onState(); updateTray() }
        Engine.onToast = { Toast.show(it) }
        Engine.onReplies = { list, title -> Choices.show(list, title) }
        Win.startTracking()
        SwingUtilities.invokeAndWait {
            Bubble.create()
            makeTray()
        }
        Hotkey.start()
        Runtime.getRuntime().addShutdownHook(Thread { Hotkey.stop() })

        if (!App.firstRunDone || Models.all.none { Models.isDownloaded(App, it) }) {
            App.firstRunDone = true
            SwingUtilities.invokeLater {
                JOptionPane.showMessageDialog(null, "<html><b>Welcome to Voice Bubble!</b><br><br>" +
                    "1. Download a speech model (next window, Speech tab). Turbo fast is a good start.<br>" +
                    "2. Click where you want to type (Discord, Chrome, Word, a game chat…).<br>" +
                    "3. Press <b>${Hotkey.pretty(App.hotkey)}</b> or click the bubble, talk, then press it again.<br>" +
                    "&nbsp;&nbsp;&nbsp;Your words are typed for you.<br><br>" +
                    "Right-click the bubble for characters and translation. Esc cancels.<br>" +
                    "Your AI key (for characters, rewrite, translate) goes in the AI tab.</html>",
                    "Voice Bubble", JOptionPane.INFORMATION_MESSAGE)
                Settings.open(tab = 0)
            }
        } else {
            Toast.show("Voice Bubble is on: ${Hotkey.pretty(App.hotkey)} to talk")
        }
        Updates.check(quiet = true)
    }

    private fun makeTray() {
        if (!SystemTray.isSupported()) return
        val menu = PopupMenu()
        fun item(t: String, f: () -> Unit) = MenuItem(t).apply { addActionListener { f() } }
        menu.add(item("Talk / stop (${Hotkey.pretty(App.hotkey)})") { Thread { Engine.toggle() }.start() })
        menu.add(item("Show the bubble") { Bubble.setVisible(true) })
        menu.add(item("Hide the bubble") { Bubble.setVisible(false) })
        menu.add(item("Settings") { Settings.open() })
        menu.addSeparator()
        menu.add(item("Quit") { quit() })
        val t = TrayIcon(icon!!.getScaledInstance(16, 16, Image.SCALE_SMOOTH), "Voice Bubble", menu)
        t.isImageAutoSize = true
        t.addActionListener { Settings.open() }
        runCatching { SystemTray.getSystemTray().add(t); tray = t }
    }

    private fun updateTray() {
        tray?.toolTip = when (Engine.state) {
            Engine.State.LISTENING -> "Voice Bubble: listening…"
            Engine.State.WORKING -> "Voice Bubble: writing…"
            else -> "Voice Bubble"
        }
    }

    fun quit() {
        Hotkey.stop()
        exitProcess(0)
    }

    // ---------------- start with Windows ----------------

    private val exe: String? get() = System.getProperty("jpackage.app-path")
    private const val RUN = "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run"

    fun startsWithWindows(): Boolean = App.isWindows && runCatching {
        ProcessBuilder("reg", "query", RUN, "/v", "VoiceBubble").start().waitFor() == 0
    }.getOrDefault(false)

    fun setStartWithWindows(on: Boolean) {
        if (!App.isWindows) return
        val path = exe ?: run { Toast.show("Only works in the installed app"); return }
        runCatching {
            if (on) ProcessBuilder("reg", "add", RUN, "/v", "VoiceBubble", "/t", "REG_SZ", "/d", "\"$path\"", "/f").start().waitFor()
            else ProcessBuilder("reg", "delete", RUN, "/v", "VoiceBubble", "/f").start().waitFor()
        }
        Toast.show(if (on) "Starts with Windows ✓" else "Won't start with Windows")
    }
}
