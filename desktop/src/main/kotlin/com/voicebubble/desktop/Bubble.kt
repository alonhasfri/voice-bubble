package com.voicebubble.desktop

import com.voicebubble.Characters
import com.voicebubble.Royal
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GraphicsEnvironment
import java.awt.Point
import java.awt.RenderingHints
import java.awt.Toolkit
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.geom.Arc2D
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JMenu
import javax.swing.JMenuItem
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.JWindow
import javax.swing.SwingUtilities
import javax.swing.Timer

/** The floating mic bubble: always on top, drag it anywhere, click to talk, right-click for the menu. */
object Bubble {
    private const val PAD = 10
    private lateinit var win: JWindow
    private lateinit var panel: JPanel
    private var art: BufferedImage? = null
    private var artKey = ""
    private var phase = 0.0
    private val timer = Timer(40) { phase += 0.12; if (Engine.state != Engine.State.IDLE) panel.repaint() }

    private val size get() = App.bubblePx.coerceIn(40, 140)

    fun create() {
        win = JWindow()
        win.isAlwaysOnTop = true
        win.focusableWindowState = false          // clicking it doesn't take the focus from what you're typing in
        win.type = java.awt.Window.Type.UTILITY   // no taskbar button
        win.background = Color(0, 0, 0, 0)
        panel = object : JPanel() {
            override fun paintComponent(g0: Graphics) { paintBubble(g0 as Graphics2D) }
        }
        panel.isOpaque = false
        win.contentPane = panel
        val drag = object : MouseAdapter() {
            var press: Point? = null; var moved = false; var start: Point? = null
            override fun mousePressed(e: MouseEvent) { press = e.locationOnScreen; start = win.location; moved = false }
            override fun mouseDragged(e: MouseEvent) {
                val p = press ?: return; val s = start ?: return
                val dx = e.locationOnScreen.x - p.x; val dy = e.locationOnScreen.y - p.y
                if (Math.abs(dx) + Math.abs(dy) > 4) moved = true
                if (moved) win.setLocation(s.x + dx, s.y + dy)
            }
            override fun mouseReleased(e: MouseEvent) {
                if (moved) { App.bubbleX = win.x; App.bubbleY = win.y; return }
                if (SwingUtilities.isRightMouseButton(e)) menu(e) else if (SwingUtilities.isLeftMouseButton(e)) Thread { Engine.toggle() }.start()
            }
        }
        panel.addMouseListener(drag); panel.addMouseMotionListener(drag)
        layout()
        timer.start()
        if (App.showBubble) win.isVisible = true
    }

    fun layout() {
        val s = size + PAD * 2
        win.size = Dimension(s, s)
        val screen = GraphicsEnvironment.getLocalGraphicsEnvironment().maximumWindowBounds
        var x = App.bubbleX; var y = App.bubbleY
        if (x < 0 || y < 0 || x > screen.x + screen.width - 20 || y > screen.y + screen.height - 20) {
            x = screen.x + screen.width - s - 24; y = screen.y + screen.height * 2 / 3
        }
        win.setLocation(x, y)
        artKey = ""
        panel.repaint()
    }

    fun setVisible(on: Boolean) { App.showBubble = on; SwingUtilities.invokeLater { win.isVisible = on } }
    fun refresh() = SwingUtilities.invokeLater { artKey = ""; layout() }
    fun onState() = SwingUtilities.invokeLater {
        if (!win.isVisible && Engine.state != Engine.State.IDLE) win.isVisible = true
        if (win.isVisible && !App.showBubble && Engine.state == Engine.State.IDLE) win.isVisible = false
        panel.repaint()
    }

    /** What you're saying, next to the bubble (Live text). */
    private var liveWin: JWindow? = null
    private var liveTxt: Txt? = null
    fun showLive(text: String?) = SwingUtilities.invokeLater {
        if (text.isNullOrBlank() || !App.prefs.liveText) { liveWin?.dispose(); liveWin = null; return@invokeLater }
        val pal = Theme.p
        val w = liveWin ?: JWindow().also { lw ->
            lw.isAlwaysOnTop = true; lw.focusableWindowState = false; lw.type = java.awt.Window.Type.UTILITY
            lw.background = Color(0, 0, 0, 0)
            val t = Txt("", 14f, pal.ink, false, maxLines = 4)
            lw.contentPane = Col(java.awt.Insets(10, 14, 10, 14), bg = pal.card, radius = 16, stroke = pal.border).apply { add(t) }
            liveTxt = t; liveWin = lw
        }
        liveTxt?.text = text.takeLast(220)
        val c = w.contentPane as Col
        w.setSize(320, c.hfw(320))
        val sc = Toolkit.getDefaultToolkit().screenSize
        val left = win.x - 330 >= 0
        w.setLocation(if (left) win.x - 326 else win.x + win.width + 6, (win.y + win.height / 2 - w.height / 2).coerceIn(4, sc.height - w.height - 4))
        if (!w.isVisible) w.isVisible = true
        c.revalidate(); c.repaint()
    }

    private fun skinArt(): BufferedImage {
        val skin = DeskSkins.byId(App.prefs.skin)
        val key = "${skin.id}/$size/${App.prefs.micOnAll}/${App.prefs.skinDetail}"
        art?.takeIf { artKey == key }?.let { return it }
        val img = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
        val g = img.createGraphics()
        DeskSkins.paint(g, skin, size)
        g.dispose()
        art = img; artKey = key
        return img
    }

    private fun paintBubble(g: Graphics2D) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        val s = size.toDouble(); val p = PAD.toDouble()
        // Soft shadow.
        g.color = Color(0, 0, 0, 50); g.fill(Ellipse2D.Double(p - 2, p + 2, s + 4, s + 4))
        when (Engine.state) {
            Engine.State.IDLE -> {
                DeskSkins.byId(App.prefs.skin).spec?.opt("glow")?.let { gl ->
                    if (gl is String) { val c = SkinPainter.color(gl, Color.WHITE)
                        for (k in 1..4) { g.color = Color(c.red, c.green, c.blue, 34 - k * 7); g.fill(Ellipse2D.Double(p - k * 1.6, p - k * 1.6, s + k * 3.2, s + k * 3.2)) } }
                }
                g.drawImage(skinArt(), PAD, PAD, null)
            }
            Engine.State.LISTENING -> {
                val pulse = (Engine.level * 6).coerceIn(0f, 1f)
                g.color = Color(229, 57, 53, (90 + 120 * pulse).toInt())
                val grow = 3 + 6 * pulse
                g.fill(Ellipse2D.Double(p - grow, p - grow, s + 2 * grow, s + 2 * grow))
                g.color = Color(229, 57, 53); g.fill(Ellipse2D.Double(p, p, s, s))
                SkinPainter.mic(g.create(PAD, PAD, size, size) as Graphics2D, size, Color.WHITE)
                val secs = Engine.seconds.toInt()
                if (secs >= 1) {
                    g.font = Font(Font.SANS_SERIF, Font.BOLD, (size / 6).coerceAtLeast(9))
                    val t = "%d:%02d".format(secs / 60, secs % 60)
                    g.color = Color.WHITE
                    g.drawString(t, (p + s / 2 - g.fontMetrics.stringWidth(t) / 2).toFloat(), (p + s * 0.93).toFloat())
                }
            }
            Engine.State.WORKING -> {
                g.color = Color(58, 91, 217); g.fill(Ellipse2D.Double(p, p, s, s))
                g.color = Color.WHITE
                g.stroke = BasicStroke((s / 12).toFloat(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
                g.draw(Arc2D.Double(p + s * 0.25, p + s * 0.25, s * 0.5, s * 0.5, -phase * 60, 260.0, Arc2D.OPEN))
            }
        }
        // A small badge when a character or translation is on.
        val badge = when {
            App.prefs.persona != null -> "✦"
            App.prefs.translateTo != null -> "→"
            else -> null
        }
        if (badge != null) {
            val r = s * 0.3
            g.color = Color.WHITE; g.fill(Ellipse2D.Double(p + s - r * 0.9, p - r * 0.1, r, r))
            g.color = Color(58, 91, 217)
            g.font = Font(Font.SANS_SERIF, Font.BOLD, (r * 0.75).toInt())
            g.drawString(badge, (p + s - r * 0.9 + r / 2 - g.fontMetrics.stringWidth(badge) / 2).toFloat(), (p - r * 0.1 + r * 0.75).toFloat())
        }
    }

    // ---------------- right-click menu ----------------

    private fun menu(e: MouseEvent) {
        val m = JPopupMenu()
        fun item(t: String, f: () -> Unit) = JMenuItem(t).apply { addActionListener { f() } }
        m.add(item(if (Engine.state == Engine.State.LISTENING) "■  Stop and type" else "🎤  Talk") { Thread { Engine.toggle() }.start() })
        if (Engine.state != Engine.State.IDLE) m.add(item("✕  Cancel") { Engine.cancel() })
        m.addSeparator()
        val talk = JMenu("Talk like…")
        talk.add(item("✕ Normal (my own voice)") { App.prefs.persona = null; refresh(); Toast.show("Back to your normal voice") })
        talk.add(item("👑 Royal English (1500s)") { App.prefs.persona = Royal.NAME; refresh(); Toast.show("Royal English on") })
        talk.addSeparator()
        (App.prefs.recentPersonas + Characters.presets).distinct().take(30).forEach { who ->
            talk.add(item(who) { App.prefs.persona = who; refresh(); Toast.show("Talking like $who") })
        }
        talk.add(item("More…") { MainWindow.pickPersona() })
        m.add(talk)
        val tr = JMenu("Translate into…")
        tr.add(item("✕ Off") { App.prefs.translateTo = null; refresh(); Toast.show("Translation off") })
        listOf("English", "Hebrew", "Spanish", "French", "German", "Italian", "Portuguese", "Arabic", "Russian", "Japanese", "Korean", "Chinese")
            .forEach { l -> tr.add(item(l) { App.prefs.translateTo = l; refresh(); Toast.show("Translating into $l") }) }
        m.add(tr)
        if (App.prefs.quickPhrases.isNotEmpty()) {
            val qp = JMenu("Quick phrases")
            App.prefs.quickPhrases.forEach { ph -> qp.add(item(ph) { Engine.typeReply(ph) }) }
            m.add(qp)
        }
        m.addSeparator()
        m.add(item("Open Voice Bubble") { MainWindow.open("home") })
        m.add(item("⚙  Settings") { MainWindow.open("settings") })
        m.add(item("Hide the bubble (shortcut still works)") { setVisible(false); MainWindow.refresh(); Toast.show("Bubble hidden. Bring it back from the tray icon") })
        m.add(item("Quit Voice Bubble") { Main.quit() })
        m.show(e.component, e.x, e.y)
    }

    /** Where messages and replies appear: next to the bubble, or bottom-right when it's hidden. */
    fun anchor(): Point {
        if (::win.isInitialized && win.isVisible) return Point(win.x + win.width / 2, win.y)
        val sc = Toolkit.getDefaultToolkit().screenSize
        return Point(sc.width - 200, sc.height - 140)
    }
}

/** Little message next to the bubble, gone after a few seconds. */
object Toast {
    private var win: JWindow? = null
    private var hide: Timer? = null
    fun show(msg: String) = SwingUtilities.invokeLater {
        win?.dispose(); hide?.stop()
        val w = JWindow(); w.isAlwaysOnTop = true; w.focusableWindowState = false; w.type = java.awt.Window.Type.UTILITY
        val pal = Theme.p
        w.background = Color(0, 0, 0, 0)
        val card = Row(java.awt.Insets(10, 14, 10, 16), 10, if (Theme.dark) pal.card else Color(0x1A1F33), 16)
        card.add(Ico("ui_mic", 18, Color(0xA9BBFF)), 0f)
        val t = Txt(msg, 14f, Color.WHITE, false)
        card.add(t, 1f)
        val w0 = (t.naturalWidth() + 60).coerceIn(160, 360)
        w.contentPane = card
        w.setSize(w0, card.hfw(w0))
        val a = Bubble.anchor()
        val sc = Toolkit.getDefaultToolkit().screenSize
        w.setLocation((a.x - w.width / 2).coerceIn(4, sc.width - w.width - 4), (a.y - w.height - 6).coerceAtLeast(4))
        w.isVisible = true; win = w
        hide = Timer(3200 + msg.length * 25) { w.dispose() }.apply { isRepeats = false; start() }
    }
}

/** Three replies (or a summary) to click. */
object Choices {
    private var win: JWindow? = null
    fun show(options: List<String>, title: String) = SwingUtilities.invokeLater {
        win?.dispose()
        val w = JWindow(); w.isAlwaysOnTop = true; w.focusableWindowState = false
        val pal = Theme.p
        w.background = Color(0, 0, 0, 0)
        val card = Col(java.awt.Insets(14, 14, 14, 14), 8, pal.card, 18, pal.border)
        card.add(Txt(title, 13f, pal.muted, true))
        options.forEach { o ->
            card.add(Col(java.awt.Insets(10, 12, 10, 12), bg = pal.accentSoft, radius = 14).apply {
                add(Txt(o, 14f, pal.ink, false)); hoverRadius = 14
                onClick = { w.dispose(); Engine.typeReply(o) }
            })
        }
        card.add(Row(gap = 0).apply { add(Gap(1), 1f); add(K.button("Close", "ui_x", pal.track, pal.ink, 36) { w.dispose() }, 0f) })
        w.contentPane = card
        w.setSize(360, card.hfw(360))
        val a = Bubble.anchor()
        val sc = Toolkit.getDefaultToolkit().screenSize
        w.setLocation((a.x - w.width / 2).coerceIn(4, sc.width - w.width - 4), (a.y - w.height - 6).coerceAtLeast(4))
        w.isVisible = true; win = w
    }
}
