package com.voicebubble.desktop

import com.voicebubble.Ai
import com.voicebubble.Characters
import com.voicebubble.Commands
import com.voicebubble.Downloader
import com.voicebubble.Features
import com.voicebubble.Langs
import com.voicebubble.Models
import com.voicebubble.Royal
import com.voicebubble.Shortcut
import com.voicebubble.SkinArt
import com.voicebubble.Stats
import com.voicebubble.Words
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Desktop
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.GridLayout
import java.awt.image.BufferedImage
import java.io.File
import java.net.URI
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.ButtonGroup
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JPasswordField
import javax.swing.JProgressBar
import javax.swing.JRadioButton
import javax.swing.JScrollPane
import javax.swing.JSlider
import javax.swing.JTabbedPane
import javax.swing.JTextArea
import javax.swing.JTextField
import javax.swing.SwingUtilities
import javax.swing.WindowConstants

/** Settings: Speech, AI & characters, Words, Bubble, About. */
object Settings {
    private var frame: JFrame? = null
    private val prefs get() = App.prefs

    fun open(tab: Int = -1) = SwingUtilities.invokeLater {
        val f = frame ?: build().also { frame = it }
        if (tab >= 0) (f.contentPane.getComponent(0) as? JTabbedPane)?.selectedIndex = tab
        f.isVisible = true
        f.toFront(); f.requestFocus()
    }

    private fun build(): JFrame {
        val f = JFrame("Voice Bubble")
        f.defaultCloseOperation = WindowConstants.HIDE_ON_CLOSE
        Main.icon?.let { f.iconImage = it }
        val tabs = JTabbedPane()
        tabs.addTab("Speech", scroll(speech()))
        tabs.addTab("AI & characters", scroll(ai()))
        tabs.addTab("Words", scroll(words()))
        tabs.addTab("Bubble & skins", bubble())
        tabs.addTab("About", scroll(about()))
        f.contentPane.add(tabs, BorderLayout.CENTER)
        f.size = Dimension(720, 640)
        f.setLocationRelativeTo(null)
        return f
    }

    // ---------------- helpers ----------------

    /** A panel as wide as the window (it scrolls down, never sideways). */
    private class Fit : JPanel(), javax.swing.Scrollable {
        override fun getPreferredScrollableViewportSize() = preferredSize
        override fun getScrollableUnitIncrement(r: java.awt.Rectangle, o: Int, d: Int) = 16
        override fun getScrollableBlockIncrement(r: java.awt.Rectangle, o: Int, d: Int) = r.height - 32
        override fun getScrollableTracksViewportWidth() = true
        override fun getScrollableTracksViewportHeight() = false
    }
    private fun scroll(c: JComponent) = JScrollPane(c, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)
        .apply { border = null; verticalScrollBar.unitIncrement = 16 }
    private fun column() = Fit().apply { layout = BoxLayout(this, BoxLayout.Y_AXIS); border = BorderFactory.createEmptyBorder(14, 16, 14, 16) }
    private fun JPanel.title(t: String) { add(Box.createVerticalStrut(10)); add(JLabel(t).apply { font = font.deriveFont(Font.BOLD, 15f); alignmentX = 0f }); add(Box.createVerticalStrut(4)) }
    private fun JPanel.note(t: String) = add(JLabel("<html><div style='width:520px;color:#666'>$t</div></html>").apply { alignmentX = 0f })
    private fun JPanel.row(vararg cs: JComponent) = add(JPanel(FlowLayout(FlowLayout.LEFT, 6, 2)).apply { alignmentX = 0f; cs.forEach { add(it) }; maximumSize = Dimension(2000, preferredSize.height) })
    private fun check(t: String, on: Boolean, set: (Boolean) -> Unit) = JCheckBox(t, on).apply { alignmentX = 0f; addActionListener { set(isSelected); Bubble.refresh() } }
    private fun button(t: String, f: () -> Unit) = JButton(t).apply { addActionListener { f() } }
    fun browse(url: String) = runCatching { Desktop.getDesktop().browse(URI(url)) }

    // ---------------- Speech ----------------

    private fun speech(): JPanel {
        val p = column()
        p.title("Speech model")
        p.note("Turns your voice into text on this computer: your voice never leaves it. Download one (a computer is fast enough for the accurate ones).")
        val group = ButtonGroup()
        for (m in Models.all.filter { !it.fastOnly }) {
            val radio = JRadioButton("<html><b>${m.title}</b><br><span style='color:#666'>${m.detail.replace("phones", "computers")}</span></html>", prefs.modelId == m.id || (prefs.modelId.isEmpty() && m == Models.byId(null)))
            group.add(radio)
            val bar = JProgressBar(0, 100).apply { isVisible = false; preferredSize = Dimension(120, 14) }
            val dl = JButton(if (Models.isDownloaded(App, m)) "Downloaded ✓" else "Download")
            dl.isEnabled = !Models.isDownloaded(App, m)
            radio.addActionListener {
                prefs.modelId = m.id
                if (!Models.isDownloaded(App, m)) dl.doClick()
            }
            dl.addActionListener {
                dl.isEnabled = false; dl.text = "Downloading…"; bar.isVisible = true
                Thread {
                    val err = Downloader.download(m.url, Models.file(App, m), m.bytes) { prog -> SwingUtilities.invokeLater { bar.value = (prog * 100).toInt() } }
                    SwingUtilities.invokeLater {
                        bar.isVisible = false
                        if (err == null) { dl.text = "Downloaded ✓"; radio.isSelected = true; prefs.modelId = m.id; Toast.show("${m.title} is ready. Press ${Hotkey.pretty(App.hotkey)} or click the bubble to talk") }
                        else { dl.text = "Try again"; dl.isEnabled = true; JOptionPane.showMessageDialog(null, "Download failed: $err") }
                    }
                }.start()
            }
            p.add(JPanel(BorderLayout()).apply { alignmentX = 0f; isOpaque = false; add(radio, BorderLayout.CENTER); add(JPanel().apply { add(bar); add(dl) }, BorderLayout.EAST); maximumSize = Dimension(2000, preferredSize.height) })
        }
        p.title("Language")
        val langs = JComboBox(Langs.all.map { it.second }.toTypedArray()).apply {
            selectedIndex = Langs.all.indexOfFirst { it.first == prefs.lang }.coerceAtLeast(0)
            addActionListener { prefs.lang = Langs.all[selectedIndex].first }
        }
        p.row(JLabel("You speak:"), langs)
        p.note("English-only models always write English. Pick an \"all languages\" model for Hebrew, German and more.")
        p.add(check("Extra accuracy (slower)", prefs.extraAccuracy) { prefs.extraAccuracy = it })
        p.title("Microphone")
        val mics = listOf("Windows default") + Mic.names()
        val micBox = JComboBox(mics.toTypedArray()).apply {
            selectedItem = App.micName ?: "Windows default"
            addActionListener { App.micName = (selectedItem as String).takeIf { it != "Windows default" } }
        }
        p.row(micBox)
        return p
    }

    // ---------------- AI & characters ----------------

    private fun ai(): JPanel {
        val p = column()
        p.title("Your AI key")
        p.note("Characters, Royal English, rewrite, translate, replies and fixing mistakes use your own key. " +
            "An OpenRouter key (sk-or-…) is free. Claude (sk-ant-…) and Gemini keys work too. It stays on this computer.")
        val key = JPasswordField(Ai.key(App), 32)
        p.row(key, button("Save") { Ai.setKey(App, String(key.password)); Toast.show(if (Ai.hasKey(App)) "AI key saved ✓" else "AI key removed") },
            button("Get a free key") { browse("https://openrouter.ai/keys") })
        p.add(check("AI fixes misheard words", prefs.aiFix) { prefs.aiFix = it })

        p.title("Talk like a character")
        val names = listOf("✕ Normal (my own voice)") + (prefs.recentPersonas + Characters.presets + Characters.custom(App).keys).distinct()
        val who = JComboBox(names.toTypedArray()).apply { isEditable = true; selectedItem = prefs.persona ?: names[0] }
        p.row(who, button("Use") {
            val v = (who.selectedItem as? String)?.trim().orEmpty()
            prefs.persona = if (v.isEmpty() || v.startsWith("✕")) null else v
            Bubble.refresh()
            Toast.show(prefs.persona?.let { "Talking like $it" } ?: "Back to your normal voice")
        })
        p.note("Type any name (\"Gojo + Spamton\" mixes two). Say \"${prefs.commandWord}, talk normally\" to stop. Royal English is \"${Royal.NAME}\".")
        val levels = arrayOf("Low", "Medium", "Intense", "Extreme")
        val inten = JSlider(0, 3, prefs.charIntensity).apply {
            majorTickSpacing = 1; paintTicks = true; snapToTicks = true
            labelTable = java.util.Hashtable<Int, JComponent>().apply { levels.forEachIndexed { i, s -> put(i, JLabel(s)) } }; paintLabels = true
            addChangeListener { if (!valueIsAdjusting) prefs.charIntensity = value }
        }
        p.row(JLabel("How strong:"), inten)
        p.add(check("Instant characters (no AI, for the ones that have it: Spamton, Rouxls, Temmie, Flowey)", prefs.instantChars) { prefs.instantChars = it })
        p.add(check("Instant Royal English (no AI)", prefs.instantRoyal) { prefs.instantRoyal = it })
        val len = JComboBox(Ai.lengths.map { it.second }.toTypedArray()).apply {
            selectedIndex = Ai.lengths.indexOfFirst { it.first == prefs.aiLength }.coerceAtLeast(0)
            addActionListener { prefs.aiLength = Ai.lengths[selectedIndex].first }
        }
        p.row(JLabel("AI text length:"), len)

        p.title("Translate")
        val langs = listOf("Off", "English", "Hebrew", "Spanish", "French", "German", "Italian", "Portuguese", "Arabic", "Russian", "Japanese", "Korean", "Chinese")
        val tr = JComboBox(langs.toTypedArray()).apply {
            isEditable = true; selectedItem = prefs.translateTo ?: "Off"
            addActionListener { prefs.translateTo = (selectedItem as? String)?.trim()?.takeIf { it.isNotEmpty() && it != "Off" }; Bubble.refresh() }
        }
        p.row(JLabel("Everything I say, in:"), tr)
        p.note("Into English it's done on this computer. Other languages use your AI key.")

        p.title("Games & chat")
        p.add(check("Roblox-safe words (so Roblox doesn't hide your message with ####)", prefs.robloxSafe) { prefs.robloxSafe = it })
        p.add(check("CAPS LOCK", prefs.capsLock) { prefs.capsLock = it })
        return p
    }

    // ---------------- Words ----------------

    private fun words(): JPanel {
        val p = column()
        p.title("Voice features")
        for (f in listOf(Commands.Feature.COMMANDS, Commands.Feature.EMOJI, Commands.Feature.FILLER, Commands.Feature.NUMBERS,
                Commands.Feature.PACK_JJK, Commands.Feature.PACK_TOBY, Commands.Feature.PACK_GENZ))
            p.add(check(f.title, Features.isOn(App, f)) { Features.set(App, f, it) })
        val word = JTextField(prefs.commandWord, 16)
        p.row(JLabel("Command word:"), word, button("Save") { prefs.commandWord = word.text.trim().ifEmpty { "out of context" }; Toast.show("Saved") })
        p.note("Say it before a command: \"out of context, translate to Spanish\", \"…, make this more polite\", \"…, talk like Sans\", \"…, undo\". " +
            "\"Scratch that\" takes back the last thing typed.")

        p.title("Your dictionary")
        p.note("Names and words to always spell your way, one per line.")
        val dict = JTextArea(Words.dictionary(App).joinToString("\n"), 6, 40)
        p.add(JScrollPane(dict).apply { alignmentX = 0f })
        p.row(button("Save dictionary") { Words.saveDictionary(App, dict.text.lines().map { it.trim() }.filter { it.isNotEmpty() }); Toast.show("Dictionary saved") })

        p.title("Shortcuts")
        p.note("Say a short phrase, get longer text. One per line: <b>say =&gt; write</b>, e.g. <i>my email =&gt; me@example.com</i>")
        val sc = JTextArea(Words.shortcuts(App).joinToString("\n") { "${it.say} => ${it.write}" }, 5, 40)
        p.add(JScrollPane(sc).apply { alignmentX = 0f })
        p.row(button("Save shortcuts") {
            Words.saveShortcuts(App, sc.text.lines().mapNotNull { l -> l.split("=>", limit = 2).takeIf { it.size == 2 }?.let { Shortcut(it[0].trim(), it[1].trim()) } }
                .filter { it.say.isNotEmpty() && it.write.isNotEmpty() })
            Toast.show("Shortcuts saved")
        })
        return p
    }

    // ---------------- Bubble & skins ----------------

    private fun icon(s: DeskSkins.Skin, px: Int): ImageIcon {
        val img = BufferedImage(px, px, BufferedImage.TYPE_INT_ARGB)
        val g = img.createGraphics()
        SkinPainter.paint(g, s.spec, px)
        if (DeskSkins.showsMic(s)) SkinPainter.mic(g, px, SkinPainter.color(s.spec?.opt("mic"), Color.WHITE))
        g.dispose()
        return ImageIcon(img)
    }

    private fun bubble(): JPanel {
        val top = column()
        top.title("Bubble")
        top.add(check("Show the floating bubble", App.showBubble) { Bubble.setVisible(it) })
        val size = JSlider(40, 140, App.bubblePx).apply { addChangeListener { if (!valueIsAdjusting) { App.bubblePx = value; Bubble.refresh() } } }
        top.row(JLabel("Size:"), size)
        val shortcut = JButton(Hotkey.pretty(App.hotkey))
        shortcut.addActionListener {
            shortcut.text = "Press your new shortcut…"
            Hotkey.capture = { k -> SwingUtilities.invokeLater { App.hotkey = k; Hotkey.reload(); shortcut.text = Hotkey.pretty(k); Toast.show("Shortcut: ${Hotkey.pretty(k)}") } }
        }
        top.row(JLabel("Keyboard shortcut:"), shortcut, check("Hold to talk (let go to type)", App.holdToTalk) { App.holdToTalk = it })
        top.row(check("Sounds", prefs.sound != "off") { prefs.sound = if (it) "chime" else "off" },
            check("Mic on every skin", prefs.micOnAll) { prefs.micOnAll = it },
            check("Start with Windows", Main.startsWithWindows()) { Main.setStartWithWindows(it) })

        val grid = JPanel(GridLayout(0, 5, 6, 6))
        val cats = listOf("characters" to "Characters", "themes" to "Themes", "fun" to "Fun", "basic" to "Basic", "mine" to "Mine")
        val catBar = JPanel(FlowLayout(FlowLayout.LEFT))
        fun show(cat: String) {
            grid.removeAll()
            val list = DeskSkins.all().filter { it.category == cat || (cat == "fun" && it.category !in cats.map { c -> c.first }) }
            for (s in list) grid.add(JButton(icon(s, 52)).apply {
                toolTipText = s.title
                text = "<html><center><span style='font-size:8px'>${s.title.take(16)}</span></center></html>"
                verticalTextPosition = JButton.BOTTOM; horizontalTextPosition = JButton.CENTER
                if (s.id == prefs.skin) border = BorderFactory.createLineBorder(Color(58, 91, 217), 3)
                addActionListener { prefs.skin = s.id; Bubble.refresh(); show(cat) }
            })
            if (cat == "mine") {
                grid.add(button("✨ Make with AI") { makeSkin { show("mine") } })
                grid.add(button("Paste a code") {
                    val code = JOptionPane.showInputDialog(null, "Paste a skin code someone shared:") ?: return@button
                    val id = SkinArt.importCode(App, code.trim())
                    if (id == null) Toast.show("That isn't a skin code") else { prefs.skin = id; Bubble.refresh(); show("mine") }
                })
            }
            grid.revalidate(); grid.repaint()
        }
        cats.forEach { (id, name) -> catBar.add(button(name) { show(id) }) }
        show(if (DeskSkins.byId(prefs.skin).category in cats.map { it.first }) DeskSkins.byId(prefs.skin).category else "characters")

        val p = JPanel(BorderLayout())
        p.add(top, BorderLayout.NORTH)
        p.add(scroll(Fit().apply { layout = BorderLayout(); add(grid, BorderLayout.NORTH); border = BorderFactory.createEmptyBorder(6, 12, 6, 12) }), BorderLayout.CENTER)
        p.add(catBar, BorderLayout.SOUTH)
        return p
    }

    private fun makeSkin(done: () -> Unit) {
        if (!Ai.hasKey(App)) { JOptionPane.showMessageDialog(null, "Making skins needs your AI key (AI & characters tab)."); return }
        val what = JOptionPane.showInputDialog(null, "Describe the skin (e.g. \"Jevil\", \"a frog in a hat\", \"Gojo's eyes\"):") ?: return
        if (what.isBlank()) return
        Toast.show("Designing \"$what\"… (looks it up on the wikis first)")
        Thread {
            val spec = runCatching { SkinArt.parseSpec(Ai.designSkin(App, what), minShapes = 6) }.getOrNull()
            SwingUtilities.invokeLater {
                if (spec == null) Toast.show("The AI couldn't draw that one. Try again or describe it differently")
                else { val id = SkinArt.addDesign(App, spec); prefs.skin = id; Bubble.refresh(); done(); Toast.show("New skin ✓") }
            }
        }.start()
    }

    // ---------------- About ----------------

    private fun about(): JPanel {
        val p = column()
        p.title("Voice Bubble for Windows" + if (App.version > 0) " · build ${App.version}" else "")
        p.note("Press <b>${Hotkey.pretty(App.hotkey)}</b> (or click the bubble), talk, press it again: the text is typed where your cursor is, in any program. " +
            "Esc cancels. Right-click the bubble for characters and translation.")
        p.title("Your stats")
        p.note("${Stats.total(App)} dictations · ${Stats.words(App)} words · about ${Stats.savedMinutes(App)} minutes saved · ${Stats.todayWords(App)} words today")
        p.title("Recent")
        val hist = JTextArea(Stats.history(App).take(25).joinToString("\n\n") { "${Stats.ago(it.time)}: ${it.text}" }, 8, 40).apply { isEditable = false; lineWrap = true; wrapStyleWord = true }
        p.add(JScrollPane(hist).apply { alignmentX = 0f })
        p.row(button("Check for updates") { Updates.check(quiet = false) }, button("Open settings folder") { runCatching { Desktop.getDesktop().open(App.filesDir) } })
        return p
    }
}
