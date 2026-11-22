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
import javax.swing.SwingUtilities
import javax.swing.UIManager
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    Main.startedByWindows = "--startup" in args
    // Moving data between two computers (or testing): "--send" shows a code, "--receive CODE" takes it.
    if ("--send" in args) {
        val c = com.voicebubble.Transfer.newCode(com.voicebubble.Transfer.localIp() ?: byteArrayOf(127, 0, 0, 1))
        println("CODE: ${c.text}")
        com.voicebubble.Transfer.serve(App, c, stop = { false }) { println("SENT") }
        exitProcess(0)
    }
    args.indexOf("--receive").takeIf { it >= 0 }?.let {
        val n = com.voicebubble.Transfer.import(App, com.voicebubble.Transfer.receive(com.voicebubble.Transfer.parse(args[it + 1])!!))
        println("IMPORTED $n"); exitProcess(0)
    }
    val i = args.indexOf("--transcribe")
    if (i >= 0) {
        Engine.dryRun = { println("TYPED: $it") }
        Engine.onToast = { println("NOTE: $it") }
        Engine.transcribeFile(args[i + 1])
        exitProcess(0)
    }
    Main.run()
    args.indexOf("--open").takeIf { it >= 0 }?.let { MainWindow.open(args.getOrNull(it + 1)) }
    // For checking the look: "--pick translate" opens that picker.
    if ("--pick" in args) javax.swing.Timer(1500) { MainWindow.pickTranslate() }.apply { isRepeats = false; start() }
}

object Main {
    private const val PORT = 47613
    var startedByWindows = false
    var icon: Image? = null; private set
    private var tray: TrayIcon? = null

    fun run() {
        // Only one Voice Bubble at a time: a second start just opens Settings of the first one.
        val lock = runCatching { RandomAccessFile(File(App.filesDir, "running.lock"), "rw").channel.tryLock() }.getOrNull()
        if (lock == null) {
            // Already running: open its window instead.
            runCatching { java.net.Socket("127.0.0.1", PORT).use { it.getOutputStream().write("open\n".toByteArray()) } }
            exitProcess(0)
        }
        Thread({
            runCatching {
                java.net.ServerSocket(PORT, 4, java.net.InetAddress.getLoopbackAddress()).use { server ->
                    while (true) server.accept().use { MainWindow.open("home") }
                }
            }
        }, "open-listener").apply { isDaemon = true; start() }
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
            MainWindow.open("home")
            SwingUtilities.invokeLater {
                Dlg.show("Welcome to Voice Bubble!",
                    "1. Download the speech model (Home → Download the speech model). Turbo fast is a good start.\n" +
                    "2. Click where you want to type: Discord, Chrome, Word, a game chat…\n" +
                    "3. Press ${Hotkey.pretty(App.hotkey)} or click the bubble, talk, then press it again. Your words are typed for you.\n\n" +
                    "Right-click the bubble for characters, translation and quick phrases. Esc cancels. " +
                    "Your AI key (characters, rewrite, translate) goes under the gear → AI.\n\n" +
                    "Have Voice Bubble on your phone? Gear → Move from your phone brings everything over with a code.",
                    buttons = listOf(Dlg.Btn("Let's go", true)), width = 440)
            }
        } else if (System.getProperty("jpackage.app-path") != null && !startedByWindows) {
            MainWindow.open("home")
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
        menu.add(item("Open Voice Bubble") { MainWindow.open("home") })
        menu.add(item("Settings") { MainWindow.open("settings") })
        menu.addSeparator()
        menu.add(item("Quit") { quit() })
        val t = TrayIcon(icon!!.getScaledInstance(16, 16, Image.SCALE_SMOOTH), "Voice Bubble", menu)
        t.isImageAutoSize = true
        t.addActionListener { MainWindow.open("home") }
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
            if (on) ProcessBuilder("reg", "add", RUN, "/v", "VoiceBubble", "/t", "REG_SZ", "/d", "\"$path\" --startup", "/f").start().waitFor()
            else ProcessBuilder("reg", "delete", RUN, "/v", "VoiceBubble", "/f").start().waitFor()
        }
        Toast.show(if (on) "Starts with Windows ✓" else "Won't start with Windows")
    }
}
