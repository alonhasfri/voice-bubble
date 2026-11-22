package com.voicebubble.desktop

import com.voicebubble.Adapt
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
import com.voicebubble.Styles
import com.voicebubble.Words
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Desktop
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Insets
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.io.File
import java.net.URI
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.Timer
import javax.swing.WindowConstants

/**
 * The app window, laid out like the phone app: Home (with Main · Writing · Options · Activity), Dictionary,
 * Shortcuts and Style along the bottom; Settings under the gear; Speech models, Skins and History on top.
 */
object MainWindow {
    private var frame: JFrame? = null
    val frameRef: JFrame? get() = frame
    private val prefs get() = App.prefs
    private val p get() = Theme.p

    private var tab = "home"
    private var homeTab = 0
    /** Screens opened on top of a tab (null = the tab itself). */
    private var screen: String? = null
    private var skinCat = "characters"
    private var styleCat = Styles.Category.PERSONAL
    private var dictQuery = ""
    private var scrollKeep = 0

    fun open(screen: String? = null) = SwingUtilities.invokeLater {
        val f = frame ?: build().also { frame = it }
        var screen = screen
        // "home2" = Home on its third little tab (Options).
        if (screen != null && screen.startsWith("home") && screen.length == 5) { homeTab = screen.last().digitToInt(); screen = "home" }
        if (screen != null) { if (screen in listOf("home", "dictionary", "shortcuts", "style")) { tab = screen; this.screen = null } else this.screen = screen }
        render()
        f.isVisible = true
        f.extendedState = f.extendedState and JFrame.ICONIFIED.inv()
        f.toFront(); f.requestFocus()
    }

    fun refresh() = SwingUtilities.invokeLater { if (frame?.isVisible == true) render(keepScroll = true) }

    private fun build(): JFrame {
        val f = JFrame("Voice Bubble")
        f.defaultCloseOperation = WindowConstants.HIDE_ON_CLOSE
        Main.icon?.let { f.iconImage = it }
        f.size = Dimension(470, 880.coerceAtMost(java.awt.Toolkit.getDefaultToolkit().screenSize.height - 60))
        f.minimumSize = Dimension(380, 500)
        f.setLocationRelativeTo(null)
        Dlg.owner = f
        return f
    }

    private var scroller: javax.swing.JScrollPane? = null

    private fun render(keepScroll: Boolean = false) {
        val f = frame ?: return
        val keep = if (keepScroll) scroller?.verticalScrollBar?.value ?: 0 else 0
        val page = Page()
        when (screen) {
            "settings" -> settings(page)
            "models" -> models(page)
            "skins" -> { skins(f); return }
            "history" -> history(page)
            else -> when (tab) {
                "dictionary" -> dictionary(page)
                "shortcuts" -> shortcuts(page)
                "style" -> style(page)
                else -> home(page)
            }
        }
        val root = JPanel(BorderLayout()).apply { background = p.bg }
        val sc = page.scroller()
        scroller = sc
        root.add(sc, BorderLayout.CENTER)
        if (screen == null) root.add(bottomNav(), BorderLayout.SOUTH)
        f.contentPane = root
        f.contentPane.background = p.bg
        f.revalidate(); f.repaint()
        if (keep > 0) SwingUtilities.invokeLater { sc.verticalScrollBar.value = keep }
    }

    private fun go(to: String?) { screen = to; render() }

    // ---------------- bottom tab bar ----------------

    private fun bottomNav(): JPanel {
        val tabs = listOf(Triple("home", "ui_home", "Home"), Triple("dictionary", "ui_book", "Dictionary"),
            Triple("shortcuts", "ui_zap", "Shortcuts"), Triple("style", "ui_type", "Style"))
        val row = Row(Insets(8, 6, 12, 6), 0, p.card)
        for ((key, icon, label) in tabs) {
            val on = key == tab
            val item = object : V() {
                val t = Txt(label, 12f, if (on) p.accentInk else p.muted, true, center = true)
                init { layout = null; add(t); onClick = { tab = key; screen = null; render() }; hoverRadius = 14 }
                override fun hfw(w: Int) = 54
                override fun doLayout() { t.setBounds(0, 35, width, 18) }
                override fun paintComponent(g: Graphics) {
                    val g2 = aa(g)
                    val x = (width - 58) / 2
                    if (on) { g2.color = p.accentSoft; g2.fill(java.awt.geom.RoundRectangle2D.Float(x.toFloat(), 0f, 58f, 30f, 30f, 30f)) }
                    Icons.paint(g2, icon, (width - 21) / 2, 4, 21, if (on) p.accentInk else p.muted)
                }
            }
            row.add(item, 1f)
        }
        return JPanel(BorderLayout()).apply {
            background = p.border
            border = javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, p.border)
            add(object : JPanel(BorderLayout()) { init { isOpaque = true; background = p.card; add(row) }
                override fun getPreferredSize() = Dimension(100, row.hfw(width.coerceAtLeast(300))) }, BorderLayout.CENTER)
        }
    }

    // ---------------- Home ----------------

    private fun home(page: Page) {
        page.add(K.header("Voice Bubble", "Speak in any program. Free and on your computer.") { go("settings") })
        Updates.available?.let { b ->
            page.add(K.card().apply {
                add(K.row("ui_download", "green", "Update ready: build $b", sub = "You have build ${App.version}. Click to download") { Settings.browse(Updates.DOWNLOAD) })
            }, 16)
        }
        page.add(hero(), 16)
        if (Models.all.none { Models.isDownloaded(App, it) }) {
            page.add(K.card().apply {
                add(K.row("ui_download", "red", "Download the speech model", sub = "One time, about 180 MB. Then it works without internet") { go("models") })
            }, 14)
        }
        page.add(K.segmented(listOf("Main", "Writing", "Options", "Activity"), homeTab) { homeTab = it; render() }, 16)
        when (homeTab) {
            0 -> { page.add(tiles(), 14); page.add(K.sectionTitle("Try it")); page.add(tryCard()) }
            1 -> { page.add(K.sectionTitle("Writing helpers")); page.add(helpers()); page.add(commandsCard(), 12) }
            2 -> { page.add(K.sectionTitle("Options")); page.add(options())
                page.add(Col(Insets(12, 6, 0, 6)).apply { add(K.note("Everything else is under the gear at the top. Tip: right-click the bubble for the quick menu.")) }) }
            else -> { page.add(K.sectionTitle("Your numbers")); page.add(statsRow()); page.add(K.sectionTitle("Recent")); page.add(recent(8)) }
        }
    }

    private fun hero(): V {
        val on = App.showBubble
        val card = Col(Insets(20, 20, 20, 20), radius = 22,
            gradient = if (on) Color(0x3A5BD9) to Color(0x6A3FD9) else Color(0x2A3048) to Color(0x3A4157))
        val topRow = Row(gap = 12)
        val col = Col(gap = 2)
        col.add(Txt("Floating microphone", 14f, Color(255, 255, 255, 204), true))
        col.add(Txt(if (on) "On" else "Off", 30f, Color.WHITE, true))
        val state = when (Engine.state) {
            Engine.State.LISTENING -> "Listening… press ${Hotkey.pretty(App.hotkey)} again to type it"
            Engine.State.WORKING -> "Writing…"
            else -> if (on) "On screen · ${Hotkey.pretty(App.hotkey)} talks in any program" else "Hidden · ${Hotkey.pretty(App.hotkey)} still works"
        }
        col.add(Txt(state, 13f, Color(255, 255, 255, 230), false))
        topRow.add(col, 1f)
        val knob = object : V() {
            init { onClick = { Bubble.setVisible(!App.showBubble); render() }; hoverRadius = 38 }
            override fun hfw(w: Int) = 76
            override fun naturalWidth() = 76
            override fun paintComponent(g: Graphics) {
                val g2 = aa(g)
                g2.color = if (on) Color.WHITE else Color(255, 255, 255, 51); g2.fill(Ellipse2D.Float(1f, 1f, 74f, 74f))
                g2.color = Color(255, 255, 255, 102); g2.stroke = java.awt.BasicStroke(2f); g2.draw(Ellipse2D.Float(1f, 1f, 74f, 74f))
                Icons.paint(g2, "ui_mic", 22, 22, 32, if (on) Color(0x3A5BD9) else Color.WHITE)
            }
        }
        topRow.add(knob, 0f)
        card.add(topRow)
        card.add(K.button(if (on) "Hide the bubble" else "Show the bubble", if (on) "ui_x" else "ui_play",
            if (on) Color(255, 255, 255, 51) else Color.WHITE, if (on) Color.WHITE else Color(0x2B47B8), 46) {
            Bubble.setVisible(!App.showBubble); render()
        }, 16)
        card.add(Txt("Click the bubble or press ${Hotkey.pretty(App.hotkey)} to talk · again to type it · Esc cancels · drag to move · right-click for the menu",
            12f, Color(255, 255, 255, 191), false, center = true), 10)
        return card
    }

    private class Tile(val icon: String, val tone: String, val title: String, val value: String, val open: () -> Unit)

    private fun tiles(): V {
        val model = Models.byId(prefs.modelId)
        val items = listOf(
            Tile("ui_user", "orange", "Talk like", prefs.persona ?: "Normal") { pickPersona() },
            Tile("ui_star", "gold", "Bubble skin", DeskSkins.byId(prefs.skin).title) { go("skins") },
            Tile("ui_globe", "purple", "Translate", prefs.translateTo ?: "Off") { pickTranslate() },
            Tile("ui_wand", "blue", "AI length", Ai.lengthName(prefs.aiLength).substringBefore(" (")) { pickLength() },
            Tile("ui_download", "green", "Speech model", model.title.substringBefore(" (") + if (Models.isDownloaded(App, model)) "" else " · download") { go("models") },
            Tile("ui_zap", "red", "Quick phrases", "${prefs.quickPhrases.size} phrases") { editQuickPhrases() },
            Tile("ui_mic", "purple", "Recording to text", "WAV voice notes") { recordingToText() },
            Tile("ui_book", "teal", "History", "Search everything") { go("history") },
        ).map { t ->
            K.card().apply {
                pad = Insets(14, 14, 14, 14)
                add(TileV(t.icon, t.tone))
                add(Txt(t.title, 15f, p.ink, true, maxLines = 1), 10)
                add(Txt(t.value, 13f, p.muted, false, maxLines = 1), 2)
                onClick = t.open; hoverRadius = 20
            }
        }
        return K.grid(2, 12, items)
    }

    private fun tryCard(): V {
        val c = K.card(18)
        val f = Field("Click here, press ${Hotkey.pretty(App.hotkey)} and talk…", multi = true, rows = 4)
        c.add(f)
        c.add(K.note("Your words appear wherever the cursor is: here, or in any other program."), 10)
        return c
    }

    private fun helpers(): V {
        val rows = mutableListOf<V>()
        listOf(
            Triple(Commands.Feature.COMMANDS, "ui_mic" to "blue", "\"new line\", \"question mark\", \"scratch that\""),
            Triple(Commands.Feature.EMOJI, "ui_spark" to "gold", "\"fire emoji\" → 🔥, \"skull emoji\" → 💀"),
            Triple(Commands.Feature.FILLER, "ui_x" to "red", "Removes um, uh and repeated words"),
            Triple(Commands.Feature.NUMBERS, "ui_clock" to "green", "\"twenty five percent\" → 25%, \"March third\" → March 3"),
        ).forEach { (f, look, sub) -> rows += K.switchRow(look.first, look.second, f.title, sub, Features.isOn(App, f)) { Features.set(App, f, it) } }
        rows += K.row("ui_user", "orange", "Talk like", prefs.persona ?: "Off",
            sub = if (prefs.persona != null && !Ai.hasKey(App)) "Needs an AI key (Settings → AI)" else "Rewrite everything you say in someone's voice. Quick menu: right-click the bubble") { pickPersona() }
        rows += K.row("ui_globe", "purple", "Translate into", prefs.translateTo ?: "Off",
            sub = if (prefs.translateTo.equals("English", true)) "Into English happens on this computer"
            else if (prefs.translateTo != null && !Ai.hasKey(App)) "Needs an AI key (Settings → AI)" else null) { pickTranslate() }
        rows += K.row("ui_spark", "gold", "How strong", listOf("Low", "Medium", "Intense", "Extreme")[prefs.charIntensity.coerceIn(0, 3)],
            sub = "How much characters and Royal English come through") {
            Dlg.pick("How strong", listOf("Low", "Medium", "Intense", "Extreme"), prefs.charIntensity) { prefs.charIntensity = it; render(true) }
        }
        rows += K.switchRow("ui_zap", "teal", "Instant characters", "No AI needed for the ones that have it (Spamton, Rouxls, Temmie, Flowey)", prefs.instantChars) { prefs.instantChars = it }
        rows += K.switchRow("ui_star", "gold", "Instant Royal English", "1500s royal, made on this computer, no AI", prefs.instantRoyal) { prefs.instantRoyal = it }
        return K.group(rows)
    }

    private fun commandsCard(): V {
        val w = prefs.commandWord
        val c = K.card(18)
        val head = Row(gap = 10)
        head.add(Txt("Command word: \"$w\"", 15f, p.ink, true), 1f)
        head.add(K.button("Change", "ui_edit", p.accentSoft, p.accentInk, 38) {
            Dlg.input("Command word", "A phrase you wouldn't normally say, e.g. \"hey bubble\" or \"out of context\".", Commands.DEFAULT_WORD, w,
                extra = Dlg.Btn("Default") { prefs.commandWord = Commands.DEFAULT_WORD; render(true); true }) {
                prefs.commandWord = it.ifBlank { Commands.DEFAULT_WORD }; render(true)
            }
        }, 0f)
        c.add(head)
        c.add(K.note("Say it first (or after what you dictated) to give a command:\n" +
            "• \"$w, enable numbers and dates\" / \"…disable filler remover\"\n" +
            "• \"$w, rewrite, make this more polite\" / \"…make this shorter\"\n" +
            "• \"$w, translate to Spanish\" / \"…stop translating\"\n" +
            "• \"$w, reply\" / \"…reply like Gojo, saying I'm late\" (select the message first)\n" +
            "• \"$w, summarise\" (select the text first)\n" +
            "• \"$w, talk like Rouxls Kaard from now on\" / \"…talk normally\"\n" +
            "• \"$w, short\" / \"…medium\" / \"…long\": how long AI text is\n" +
            "• \"$w, royal mode\" / \"…offline royal\" / \"…AI royal\" / \"…back to normal\"\n" +
            "• \"$w, undo\", or just \"scratch that\"\n" +
            "Rewrite, reply, summarise and most translations need an AI key (Settings → AI)."), 8)
        return c
    }

    private fun options(): V {
        val model = Models.byId(prefs.modelId)
        val rows = mutableListOf<V>(
            K.row("ui_cpu", "purple", "Speech model", model.title.substringBefore(" (") + if (Models.isDownloaded(App, model)) "" else " · download",
                valueColor = if (Models.isDownloaded(App, model)) null else p.danger) { go("models") },
        )
        if (!model.englishOnly) rows += K.row("ui_globe", "teal", "Language", Langs.all.firstOrNull { it.first == prefs.lang }?.second) {
            Dlg.pick("Language you speak", Langs.all.map { it.second }, Langs.all.indexOfFirst { it.first == prefs.lang }) { prefs.lang = Langs.all[it].first; render(true) }
        }
        rows += K.row("ui_type", "blue", "Keyboard shortcut", Hotkey.pretty(App.hotkey), sub = "Talk in any program, even with the bubble hidden") { changeShortcut() }
        rows += K.switchRow("ui_mic", "orange", "Hold to talk", "Hold the shortcut while you speak, let go to type", App.holdToTalk) { App.holdToTalk = it }
        rows += K.switchRow("ui_type", "orange", "Caps lock", "EVERYTHING IN CAPITALS", prefs.capsLock) { prefs.capsLock = it; Bubble.refresh() }
        rows += K.row("ui_file", "orange", "Length limit (all programs)", if (prefs.maxChars > 0) "${prefs.maxChars}" else "Off",
            sub = "Roblox gets 200 by itself") { pickLimit() }
        rows += K.switchRow("ui_target", "green", "AI fixes mistakes",
            if (Ai.hasKey(App)) "Fixes misheard words from the context. Adds about a second" else "Needs an AI key (Settings → AI)", prefs.aiFix) { prefs.aiFix = it }
        rows += K.row("ui_wand", "purple", "AI text length", Ai.lengthName(prefs.aiLength), sub = "Talk like, rewrite, reply and summary") { pickLength() }
        rows += K.switchRow("ui_book", "green", "Learn how I talk", "Remembers your fixes, names you use and how you write", Adapt.isOn(App)) { Adapt.setOn(App, it) }
        rows += K.switchRow("ui_type", "blue", "Live text", "Shows your words next to the bubble as you talk", prefs.liveText) { prefs.liveText = it }
        rows += K.switchRow("ui_target", "green", "Extra accuracy", "Slower, more careful", prefs.extraAccuracy) { prefs.extraAccuracy = it }
        rows += K.switchRow("ui_bell", "gold", "Sounds", "A soft sound when it starts and stops listening", prefs.sound != "off") { prefs.sound = if (it) "chime" else "off" }
        rows += K.switchRow("ui_shield", "green", "Roblox-safe words", "So Roblox doesn't hide your message with ####", prefs.robloxSafe) { prefs.robloxSafe = it }
        return K.group(rows)
    }

    private fun statsRow(): V {
        fun stat(n: String, label: String, tone: String, icon: String) = K.card(14).apply {
            add(TileV(icon, tone, 30, 17))
            add(Txt(n, 22f, p.ink, true, maxLines = 1), 10)
            add(Txt(label, 12f, p.muted, false, maxLines = 2), 2)
        }
        return K.grid(3, 10, listOf(
            stat("${Stats.words(App)}", "words", "blue", "ui_type"),
            stat("${Stats.savedMinutes(App)} min", "saved", "green", "ui_clock"),
            stat("${Stats.streak(App)}", "day streak", "orange", "ui_zap"),
        ))
    }

    private fun recent(n: Int, query: String = ""): V {
        val list = Stats.history(App).filter { query.isBlank() || it.text.contains(query, true) }.take(n)
        if (list.isEmpty()) return K.card(18).apply { add(K.note(if (query.isBlank()) "Nothing yet. What you say shows up here." else "Nothing found.")) }
        return K.group(list.map { e ->
            Row(Insets(12, 16, 12, 16), 12).apply {
                add(Col(gap = 3).apply {
                    add(Txt(e.text, 14f, p.ink, false, maxLines = 3))
                    add(Txt(Stats.ago(e.time), 12f, p.muted, false))
                }, 1f)
                add(Ico("ui_copy", 18, p.muted), 0f)
                onClick = { Typer.copy(e.text); Toast.show("Copied") }
            }
        })
    }

    // ---------------- pickers ----------------

    fun pickPersona() {
        val mine = Characters.custom(App).keys.sorted()
        val all = mine + Characters.presets
        val items = listOf("Off (my own voice)", "➕ Make your own character…", "🔀 Mix two characters…") + mine.map { "★ $it" } + Characters.presets + "Someone else…"
        val cur = prefs.persona?.let { all.indexOf(it).takeIf { i -> i >= 0 }?.plus(3) } ?: 0
        Dlg.pick("Talk like", items, cur) { i ->
            when {
                i == 0 -> prefs.persona = null
                i == 1 -> makeCharacter()
                i == 2 -> Dlg.input("Mix two characters", "For example \"Gojo + Spamton\".", "Gojo + Spamton") { v -> if (v.isNotBlank()) prefs.persona = v.trim(); after() }
                i == items.size - 1 -> Dlg.input("Talk like…", null, "e.g. Yoda, a cowboy, my grandma") { v -> prefs.persona = v.trim().ifEmpty { null }; after() }
                else -> prefs.persona = all[i - 3]
            }
            after()
        }
    }

    private fun makeCharacter() {
        if (!Ai.hasKey(App)) { Dlg.message("Make a character", "This needs your AI key (Settings → AI)."); return }
        Dlg.input("Make your own character", "Who? Add notes if you like (how they talk, catchphrases).", "e.g. my friend Sam, says \"bro\" a lot") { v ->
            val name = v.substringBefore(",").substringBefore(":").trim().take(40)
            if (name.isEmpty()) return@input
            Toast.show("Writing $name's voice guide…")
            Thread {
                val guide = runCatching { Ai.characterGuide(App, name, v) }.getOrNull()
                SwingUtilities.invokeLater {
                    if (guide == null) Toast.show("Couldn't make it this time. Try again")
                    else { Characters.saveCustom(App, name, guide); prefs.persona = name; after(); Toast.show("Talking like $name") }
                }
            }.start()
        }
    }

    private fun after() { Bubble.refresh(); render(true) }

    fun pickTranslate() {
        val langs = listOf("Off", "English", "Hebrew", "Spanish", "French", "German", "Italian", "Portuguese", "Arabic", "Russian", "Japanese", "Korean", "Chinese")
        Dlg.pick("Translate everything you say into", langs, langs.indexOf(prefs.translateTo ?: "Off")) { i ->
            prefs.translateTo = if (i == 0) null else langs[i]; after()
        }
    }

    private fun pickLength() {
        Dlg.pick("How long should AI text be?", Ai.lengths.map { it.second }, Ai.lengths.indexOfFirst { it.first == prefs.aiLength }) { i ->
            prefs.aiLength = Ai.lengths[i].first; render(true)
        }
    }

    private fun pickLimit() {
        val opts = listOf(0 to "No limit", 100 to "100 characters", 150 to "150 characters", 200 to "200 characters (Roblox)",
            280 to "280 characters (X / Twitter)", 500 to "500 characters", -1 to "Custom…")
        Dlg.pick("Longest message", opts.map { it.second }, opts.indexOfFirst { it.first == prefs.maxChars }) { i ->
            val n = opts[i].first
            if (n >= 0) { prefs.maxChars = n; render(true) }
            else Dlg.input("Custom limit", null, "Characters, e.g. 250") { v -> prefs.maxChars = v.trim().toIntOrNull()?.coerceIn(20, 5000) ?: 0; render(true) }
        }
    }

    private fun editQuickPhrases() {
        Dlg.input("Quick phrases", "One per line. Right-click the bubble to type one.", "gg\nbrb\ntrade?", prefs.quickPhrases.joinToString("\n"), multi = true) { v ->
            prefs.quickPhrases = v.lines().map { it.trim() }.filter { it.isNotEmpty() }.take(12); render(true)
        }
    }

    private fun changeShortcut() {
        var d: javax.swing.JDialog? = null
        Hotkey.capture = { k -> SwingUtilities.invokeLater { App.hotkey = k; Hotkey.reload(); d?.dispose(); render(true); Toast.show("Shortcut: ${Hotkey.pretty(k)}") } }
        Thread { Thread.sleep(150); SwingUtilities.invokeLater { d = java.awt.Window.getWindows().filterIsInstance<javax.swing.JDialog>().lastOrNull { it.isVisible } } }.start()
        Dlg.show("Keyboard shortcut", "Press the keys you want now (for example Ctrl+Shift+Space, or F9).",
            buttons = listOf(Dlg.Btn("Cancel") { Hotkey.capture = null; true }))
    }

    private fun recordingToText() {
        val ch = JFileChooser().apply { dialogTitle = "Pick a recording (.wav)"; fileFilter = javax.swing.filechooser.FileNameExtensionFilter("WAV recordings", "wav") }
        if (ch.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) return
        Toast.show("Turning the recording into text…")
        Thread {
            val out = runCatching { Engine.fileToText(ch.selectedFile) }.getOrElse { "Couldn't read it: ${it.message}" }
            SwingUtilities.invokeLater {
                Dlg.show("Recording to text", null, Col().apply { add(Field("", true, 10, out)) },
                    listOf(Dlg.Btn("Close"), Dlg.Btn("Copy", true) { Typer.copy(out); Toast.show("Copied"); true }), width = 520)
            }
        }.start()
    }

    // ---------------- Dictionary ----------------

    private fun dictionary(page: Page) {
        page.add(K.header("Dictionary", "Names and words it should spell your way") { go("settings") })
        val bar = Row(gap = 10)
        val search = Field("Search", text = dictQuery)
        search.box.document.addDocumentListener(object : javax.swing.event.DocumentListener {
            fun ch() { dictQuery = search.value; listHolder.removeAll(); fillWords(listHolder); listHolder.revalidate(); page.revalidate(); page.repaint() }
            override fun insertUpdate(e: javax.swing.event.DocumentEvent) = ch()
            override fun removeUpdate(e: javax.swing.event.DocumentEvent) = ch()
            override fun changedUpdate(e: javax.swing.event.DocumentEvent) = ch()
        })
        bar.add(search, 1f)
        bar.add(K.button("Add", "ui_plus", p.accent, Color.WHITE, 46) { editWord(null) }, 0f)
        page.add(bar, 16)
        listHolder = Col()
        fillWords(listHolder)
        page.add(listHolder, 14)

        page.add(K.sectionTitle("Word packs"))
        page.add(K.group(listOf(Commands.Feature.PACK_JJK, Commands.Feature.PACK_TOBY, Commands.Feature.PACK_GENZ).map { f ->
            K.switchRow(when (f) { Commands.Feature.PACK_JJK -> "ui_zap"; Commands.Feature.PACK_TOBY -> "ui_star"; else -> "ui_spark" },
                when (f) { Commands.Feature.PACK_JJK -> "purple"; Commands.Feature.PACK_TOBY -> "gold"; else -> "orange" },
                f.title, "Spells these names and words right", Features.isOn(App, f)) { Features.set(App, f, it) }
        }))

        val fixes = Adapt.fixes(App)
        val learned = Adapt.learnedWords(App)
        if (fixes.isNotEmpty() || learned.isNotEmpty()) {
            page.add(K.sectionTitle("What it learned"))
            val rows = fixes.map { f ->
                K.row("ui_check", "green", "${f.say} → ${f.write}", sub = "Fixed automatically", chevron = false) {
                    Dlg.confirm("What it learned", "Stop fixing \"${f.say}\" → \"${f.write}\"?", "Stop") { Adapt.removeFix(App, f); render(true) }
                }
            }.toMutableList()
            if (learned.isNotEmpty()) rows += K.row("ui_spark", "gold", "Names you use", sub = learned.take(12).joinToString(", "), chevron = false)
            page.add(K.group(rows))
        }
    }

    private var listHolder = Col()

    private fun fillWords(holder: Col) {
        val words = Words.dictionary(App).filter { dictQuery.isBlank() || it.contains(dictQuery, true) }
        if (words.isEmpty()) {
            holder.add(K.card(18).apply { add(K.note(if (dictQuery.isBlank()) "Add names and words (your friends, games, places) so they're always spelled right." else "Nothing found.")) })
            return
        }
        holder.add(K.group(words.map { w -> K.row("ui_book", "teal", w) { editWord(w) } }))
    }

    private fun editWord(old: String?) {
        Dlg.input(if (old == null) "Add a word" else "Edit word", "Spelled exactly like this, e.g. Elad, Jujutsu, Rouxls Kaard.", "Word or name", old ?: "",
            extra = old?.let { Dlg.Btn("Delete") { Words.saveDictionary(App, Words.dictionary(App) - it); render(true); true } }) { v ->
            val w = v.trim()
            val list = Words.dictionary(App).toMutableList()
            if (old != null) list.remove(old)
            if (w.isNotEmpty() && list.none { it.equals(w, true) }) list.add(0, w)
            Words.saveDictionary(App, list); render(true)
        }
    }

    // ---------------- Shortcuts ----------------

    private fun shortcuts(page: Page) {
        page.add(K.header("Shortcuts", "Say a short phrase, get longer text") { go("settings") })
        page.add(K.primary("Add a shortcut", "ui_plus") { editShortcut(null) }, 16)
        val list = Words.shortcuts(App)
        if (list.isEmpty()) page.add(K.card(18).apply { add(K.note("For example: say \"my email\" and it types your email address. Say \"my address\", get your whole address.")) }, 14)
        else page.add(K.group(list.map { s -> K.row("ui_zap", "gold", s.say, sub = s.write) { editShortcut(s) } }), 14)
    }

    private fun editShortcut(old: Shortcut?) {
        val say = Field("When I say… (e.g. my email)", text = old?.say ?: "")
        val write = Field("Write… (e.g. me@example.com)", multi = true, rows = 3, text = old?.write ?: "")
        val body = Col(gap = 10).apply { add(say); add(write) }
        Dlg.show(if (old == null) "Add a shortcut" else "Edit shortcut", null, body, listOfNotNull(
            old?.let { Dlg.Btn("Delete") { Words.saveShortcuts(App, Words.shortcuts(App) - it); render(true); true } },
            Dlg.Btn("Cancel"),
            Dlg.Btn("Save", true) {
                val s = say.value.trim(); val w = write.value.trim()
                if (s.isEmpty() || w.isEmpty()) return@Btn false
                val list = Words.shortcuts(App).toMutableList(); old?.let { list.remove(it) }
                list.add(0, Shortcut(s, w)); Words.saveShortcuts(App, list); render(true); true
            }))
    }

    // ---------------- Style ----------------

    private fun style(page: Page) {
        page.add(K.header("Style", "How your words are written in each kind of program") { go("settings") })
        val cats = Styles.Category.values().toList()
        page.add(K.segmented(cats.map { it.title }, cats.indexOf(styleCat)) { styleCat = cats[it]; render() }, 16)
        val where = when (styleCat) {
            Styles.Category.PERSONAL -> "Discord, WhatsApp, Telegram, Messenger…"
            Styles.Category.WORK -> "Slack, Teams, Zoom…"
            Styles.Category.EMAIL -> "Outlook, Gmail, Thunderbird…"
            else -> "Everything else"
        }
        page.add(Col(Insets(8, 2, 0, 0)).apply { add(K.note(where)) })
        val chosen = Styles.get(App, styleCat)
        val sample = Styles.samples.getValue(styleCat)
        Styles.Style.values().forEach { s ->
            val sel = s == chosen
            val c = Col(Insets(18, 18, 18, 18), bg = p.card, radius = 20, stroke = if (sel) p.accent else p.border)
            val head = Row(gap = 10)
            head.add(Col().apply { add(Txt(s.title, 18f, p.ink, true)); add(Txt(s.subtitle, 13f, p.muted, false)) }, 1f)
            head.add(object : V() {
                override fun hfw(w: Int) = 22; override fun naturalWidth() = 22
                override fun paintComponent(g: Graphics) {
                    val g2 = aa(g); g2.color = if (sel) p.accent else p.muted; g2.stroke = java.awt.BasicStroke(2f)
                    g2.draw(Ellipse2D.Float(2f, 2f, 18f, 18f)); if (sel) g2.fill(Ellipse2D.Float(6f, 6f, 10f, 10f))
                }
            }, 0f)
            c.add(head)
            val bubble = Row(Insets(10, 12, 10, 12), 10, p.bg, 14)
            bubble.add(object : V() {
                override fun hfw(w: Int) = 24; override fun naturalWidth() = 24
                override fun paintComponent(g: Graphics) {
                    val g2 = aa(g); g2.color = p.accentSoft; g2.fill(Ellipse2D.Float(0f, 0f, 24f, 24f))
                    g2.color = p.accentInk; g2.font = Theme.font(12f, true); val fm = g2.fontMetrics
                    g2.drawString("Y", (24 - fm.stringWidth("Y")) / 2, (24 + fm.ascent - fm.descent) / 2)
                }
            }, 0f)
            bubble.add(Txt(Styles.apply(sample, s, styleCat), 14f, p.ink, false), 1f)
            c.add(bubble, 12)
            c.onClick = { Styles.set(App, styleCat, s); render(true) }; c.hoverRadius = 20
            page.add(c, 12)
        }
    }

    // ---------------- Settings ----------------

    private fun settings(page: Page) {
        page.add(K.backHeader("Settings") { go(null) })
        page.add(K.sectionTitle("AI"))
        page.add(K.group(listOf(
            K.row("ui_key", "purple", "Your AI key", if (Ai.hasKey(App)) "Saved ✓" else "Not set",
                sub = "Characters, Royal English, rewrite, translate, replies and fixing mistakes. OpenRouter (sk-or-…) is free; Claude and Gemini keys work too") {
                Dlg.input("Your AI key", "It stays on this computer.", "sk-or-…", Ai.key(App),
                    extra = Dlg.Btn("Get a free key") { browse("https://openrouter.ai/keys"); false }) { v -> Ai.setKey(App, v); render(true) }
            },
            K.row("ui_user", "orange", "Talk like", prefs.persona ?: "Off") { pickPersona() },
        )))
        page.add(K.sectionTitle("Speech"))
        page.add(K.group(listOf(
            K.row("ui_cpu", "purple", "Speech model", Models.byId(prefs.modelId).title.substringBefore(" (")) { go("models") },
            K.row("ui_mic", "blue", "Microphone", App.micName ?: "Windows default") {
                val mics = listOf("Windows default") + Mic.names()
                Dlg.pick("Microphone", mics, mics.indexOf(App.micName ?: "Windows default")) { i -> App.micName = if (i == 0) null else mics[i]; render(true) }
            },
            K.row("ui_type", "blue", "Keyboard shortcut", Hotkey.pretty(App.hotkey)) { changeShortcut() },
        )))
        page.add(K.sectionTitle("Look"))
        page.add(K.group(listOf(
            K.row("ui_star", "gold", "Bubble skin", DeskSkins.byId(prefs.skin).title) { go("skins") },
            K.row("ui_layout", "teal", "Bubble size", "${App.bubblePx}") {
                val sizes = listOf(48, 56, 64, 76, 90, 110)
                Dlg.pick("Bubble size", sizes.map { "$it" + when (it) { 48 -> " (small)"; 64 -> " (normal)"; 90 -> " (big)"; else -> "" } }, sizes.indexOf(App.bubblePx)) { i ->
                    App.bubblePx = sizes[i]; Bubble.refresh(); render(true)
                }
            },
            K.row(if (Theme.dark) "ui_moon" else "ui_sun", "gray", "Theme", when (Theme.mode) { "dark" -> "Dark"; "light" -> "Light"; else -> "Same as Windows" }) {
                val modes = listOf("system", "light", "dark")
                Dlg.pick("Theme", listOf("Same as Windows", "Light", "Dark"), modes.indexOf(Theme.mode)) { i -> Theme.mode = modes[i]; render() }
            },
        )))
        page.add(K.sectionTitle("Windows"))
        page.add(K.group(listOf(
            K.switchRow("ui_play", "green", "Start with Windows", "Ready as soon as you log in", Main.startsWithWindows()) { Main.setStartWithWindows(it) },
            K.switchRow("ui_layout", "blue", "Show the floating bubble", "The shortcut works either way", App.showBubble) { Bubble.setVisible(it) },
        )))
        page.add(K.sectionTitle("About"))
        page.add(K.group(listOf(
            K.row("ui_download", "green", "Check for updates", if (App.version > 0) "Build ${App.version}" else null) { Updates.check(quiet = false) },
            K.row("ui_file", "gray", "Open the settings folder") { runCatching { Desktop.getDesktop().open(App.filesDir) } },
            K.row("ui_x", "red", "Quit Voice Bubble", chevron = false, danger = true) { Main.quit() },
        )))
        page.add(Col(Insets(14, 6, 0, 6)).apply { add(K.note("Your voice is turned into text on this computer. Only AI features send text (never audio) to the AI you picked.")) })
    }

    fun browse(url: String) = runCatching { Desktop.getDesktop().browse(URI(url)) }

    // ---------------- Speech models ----------------

    private val downloading = HashMap<String, Float>()

    private fun models(page: Page) {
        page.add(K.backHeader("Speech model") { go(null) })
        page.add(Col(Insets(10, 2, 0, 0)).apply { add(K.note("Turns your voice into text on this computer: your voice never leaves it. A computer is fast enough for the accurate ones.")) })
        for (m in Models.all.filter { !it.fastOnly }) {
            val have = Models.isDownloaded(App, m)
            val using = prefs.modelId == m.id
            val c = Col(Insets(16, 16, 16, 16), bg = p.card, radius = 20, stroke = if (using && have) p.accent else p.border)
            val head = Row(gap = 12)
            head.add(TileV(if (m.englishOnly) "ui_type" else "ui_globe", if (using && have) "blue" else "gray"), 0f)
            head.add(Col(gap = 3).apply {
                add(Txt(m.title, 15f, p.ink, true))
                add(Txt(m.detail.replace("phones", "computers").replace("phone", "computer"), 13f, p.muted, false))
            }, 1f)
            c.add(head)
            val prog = downloading[m.id]
            when {
                prog != null -> { c.add(K.bar(prog, p.accent, p.track), 12); c.add(Txt("Downloading… ${(prog * 100).toInt()}%", 13f, p.muted, false), 6) }
                have && using -> c.add(Row(gap = 6).apply { add(Ico("ui_check", 18, p.good), 0f); add(Txt("In use", 14f, p.good, true), 1f) }, 12)
                have -> c.add(K.secondary("Use this one", "ui_check") { prefs.modelId = m.id; render(true) }, 12)
                else -> c.add(K.primary("Download", "ui_download") { download(m) }, 12)
            }
            page.add(c, 12)
        }
        page.add(K.sectionTitle("Language"))
        page.add(K.group(listOf(K.row("ui_globe", "teal", "Language you speak", Langs.all.firstOrNull { it.first == prefs.lang }?.second) {
            Dlg.pick("Language you speak", Langs.all.map { it.second }, Langs.all.indexOfFirst { it.first == prefs.lang }) { prefs.lang = Langs.all[it].first; render(true) }
        }, K.switchRow("ui_target", "green", "Extra accuracy", "Slower, more careful", prefs.extraAccuracy) { prefs.extraAccuracy = it })))
    }

    private fun download(m: com.voicebubble.Model) {
        if (downloading.containsKey(m.id)) return
        downloading[m.id] = 0f; render(true)
        Thread {
            var last = 0L
            val err = Downloader.download(m.url, Models.file(App, m), m.bytes) { f ->
                downloading[m.id] = f
                val now = System.currentTimeMillis()
                if (now - last > 400) { last = now; refresh() }
            }
            downloading.remove(m.id)
            SwingUtilities.invokeLater {
                if (err == null) { prefs.modelId = m.id; Toast.show("${m.title} is ready. Press ${Hotkey.pretty(App.hotkey)} to talk") }
                else Dlg.message("Download failed", err)
                render(true)
            }
        }.start()
    }

    // ---------------- Skins (like the phone's skin screen) ----------------

    private fun skinIcon(s: DeskSkins.Skin, px: Int): BufferedImage {
        val img = BufferedImage(px, px, BufferedImage.TYPE_INT_ARGB)
        val g = img.createGraphics()
        DeskSkins.paint(g, s, px)
        g.dispose()
        return img
    }

    private fun skins(f: JFrame) {
        val page = Page()
        page.add(K.backHeader("Bubble skin") { go(null) })
        page.add(K.segmented(listOf("Simple", "Detailed"), if (prefs.skinDetail) 1 else 0) { prefs.skinDetail = it == 1; Bubble.refresh(); render(true) }, 16)
        page.add(Row(gap = 8).apply {
            add(K.chip("ui_mic", "Mic on every skin", prefs.micOnAll) { prefs.micOnAll = !prefs.micOnAll; Bubble.refresh(); render(true) }, 0f)
            add(Gap(1), 1f)
        }, 12)
        val list = DeskSkins.all().filter { it.category == skinCat || (skinCat == "fun" && it.category !in listOf("characters", "themes", "basic", "mine")) }
        val cells = list.map { s ->
            val sel = s.id == prefs.skin
            object : V() {
                val img = skinIcon(s, 64)
                val t = Txt(s.title, 12f, if (sel) p.accentInk else p.ink, true, maxLines = 2, center = true)
                init { layout = null; add(t); onClick = { prefs.skin = s.id; Bubble.refresh(); render(true) }; hoverRadius = 16; toolTipText = s.title }
                override fun hfw(w: Int) = 12 + 64 + 8 + t.hfw(w - 8) + 10
                override fun doLayout() { t.setBounds(4, 84, width - 8, t.hfw(width - 8)) }
                override fun paintComponent(g: Graphics) {
                    val g2 = aa(g)
                    if (sel) { g2.color = p.accentSoft; g2.fill(java.awt.geom.RoundRectangle2D.Float(0f, 0f, width.toFloat(), height.toFloat(), 32f, 32f)) }
                    g2.drawImage(img, (width - 64) / 2, 12, null)
                    if (sel) { g2.color = p.accent; g2.stroke = java.awt.BasicStroke(3f); g2.draw(Ellipse2D.Float((width - 64) / 2f - 3, 9f, 70f, 70f)) }
                }
            }
        }
        val grid = K.grid(4, 6, cells)
        page.add(K.card(8).apply { add(grid) }, 14)
        if (skinCat == "mine") {
            page.add(K.primary("Make one with AI", "ui_wand") { makeSkin() }, 14)
            page.add(K.secondary("Paste a skin code", "ui_copy") {
                Dlg.input("Paste a skin code", "A code someone shared with you.", "Skin code") { code ->
                    val id = SkinArt.importCode(App, code.trim())
                    if (id == null) Toast.show("That isn't a skin code") else { prefs.skin = id; Bubble.refresh(); render(true) }
                }
            }, 10)
            if (cells.isEmpty()) page.add(Col(Insets(10, 6, 0, 6)).apply { add(K.note("Describe anything (\"Jevil\", \"a frog in a hat\", \"Gojo's eyes\") and the AI draws it. It looks characters up on the wikis first.")) })
        }
        // Category bar at the bottom, like the phone.
        val cats = listOf(Triple("characters", "ui_user", "Characters"), Triple("themes", "ui_spark", "Themes"),
            Triple("fun", "ui_star", "Fun"), Triple("basic", "ui_circle", "Basic"), Triple("mine", "ui_edit", "Mine"))
        val bar = Row(Insets(8, 6, 12, 6), 0, p.card)
        for ((key, icon, label) in cats) {
            val on = key == skinCat
            bar.add(object : V() {
                val t = Txt(label, 12f, if (on) p.accentInk else p.muted, true, center = true)
                init { layout = null; add(t); onClick = { skinCat = key; render() }; hoverRadius = 14 }
                override fun hfw(w: Int) = 54
                override fun doLayout() { t.setBounds(0, 35, width, 18) }
                override fun paintComponent(g: Graphics) {
                    val g2 = aa(g); val x = (width - 58) / 2
                    if (on) { g2.color = p.accentSoft; g2.fill(java.awt.geom.RoundRectangle2D.Float(x.toFloat(), 0f, 58f, 30f, 30f, 30f)) }
                    Icons.paint(g2, icon, (width - 21) / 2, 4, 21, if (on) p.accentInk else p.muted)
                }
            }, 1f)
        }
        val root = JPanel(BorderLayout()).apply { background = p.bg }
        val sc = page.scroller(); scroller = sc
        root.add(sc, BorderLayout.CENTER)
        root.add(object : JPanel(BorderLayout()) {
            init { background = p.card; border = javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, p.border); add(bar) }
            override fun getPreferredSize() = Dimension(100, bar.hfw(width.coerceAtLeast(300)) + 1)
        }, BorderLayout.SOUTH)
        f.contentPane = root
        f.revalidate(); f.repaint()
    }

    private fun makeSkin() {
        if (!Ai.hasKey(App)) { Dlg.message("Make a skin", "This needs your AI key (Settings → AI)."); return }
        Dlg.input("Make a skin with AI", "Describe it: a character, an animal, a food, anything.", "e.g. Jevil, a frog in a hat, Gojo's eyes") { what ->
            if (what.isBlank()) return@input
            Toast.show("Designing \"$what\"… (looks it up on the wikis first)")
            Thread {
                val spec = runCatching { SkinArt.parseSpec(Ai.designSkin(App, what), minShapes = 6) }.getOrNull()
                SwingUtilities.invokeLater {
                    if (spec == null) Toast.show("The AI couldn't draw that one. Try again or describe it differently")
                    else { val id = SkinArt.addDesign(App, spec); prefs.skin = id; skinCat = "mine"; Bubble.refresh(); render(true); Toast.show("New skin ✓") }
                }
            }.start()
        }
    }

    // ---------------- History ----------------

    private var historyQuery = ""

    private fun history(page: Page) {
        page.add(K.backHeader("History") { go(null) })
        val holder = Col()
        val search = Field("Search everything you said", text = historyQuery)
        search.box.document.addDocumentListener(object : javax.swing.event.DocumentListener {
            fun ch() { historyQuery = search.value; holder.removeAll(); holder.add(recent(200, historyQuery)); holder.revalidate(); page.revalidate(); page.repaint() }
            override fun insertUpdate(e: javax.swing.event.DocumentEvent) = ch()
            override fun removeUpdate(e: javax.swing.event.DocumentEvent) = ch()
            override fun changedUpdate(e: javax.swing.event.DocumentEvent) = ch()
        })
        page.add(search, 16)
        holder.add(recent(200, historyQuery))
        page.add(holder, 14)
        page.add(K.secondary("Clear history", "ui_trash") { Dlg.confirm("Clear history", "Delete everything in your history?", "Delete") { Stats.clearHistory(App); render() } }, 14)
    }

    init {
        // The hero card follows the bubble (listening / writing).
        Timer(500) { if (frame?.isVisible == true && screen == null && tab == "home") heroTick() }.start()
    }
    private var lastState: Engine.State? = null
    private fun heroTick() { if (Engine.state != lastState) { lastState = Engine.state; render(true) } }

    @Suppress("unused") private fun unused(f: File) = f
}

/** Old name, still used around the app. */
object Settings {
    fun open(tab: Int = -1) = MainWindow.open(when (tab) { 0 -> "models"; 1 -> "settings"; 3 -> "skins"; -1 -> null; else -> "settings" })
    fun browse(url: String) = MainWindow.browse(url)
}
