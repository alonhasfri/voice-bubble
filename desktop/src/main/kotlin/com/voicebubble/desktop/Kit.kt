package com.voicebubble.desktop

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Component
import java.awt.Cursor
import java.awt.Dimension
import java.awt.Font
import java.awt.GradientPaint
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Insets
import java.awt.Rectangle
import java.awt.RenderingHints
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.font.LineBreakMeasurer
import java.awt.font.TextAttribute
import java.awt.font.TextLayout
import java.awt.geom.Ellipse2D
import java.awt.geom.RoundRectangle2D
import java.text.AttributedString
import javax.swing.JComponent
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.JTextField
import javax.swing.Scrollable
import javax.swing.SwingConstants
import javax.swing.border.EmptyBorder
import javax.swing.text.JTextComponent

/**
 * A small view kit that works like the phone app's (Ui.kt): columns and rows that size to the width they get,
 * rounded cards, icon tiles, switches, segmented tabs. Everything is painted, so it looks the same as the phone
 * and not like a system dialog.
 */
open class V : JComponent() {
    /** Space above this view in a column. */
    var top = 0
    /** Clickable: hover highlight + hand cursor. */
    var onClick: (() -> Unit)? = null
        set(f) {
            field = f
            cursor = if (f != null) Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) else null
            if (f != null && !listening) { listening = true; addMouseListener(clickListener) }
        }
    var hoverRadius = 0
    private var hover = false
    private var listening = false
    private val clickListener = object : MouseAdapter() {
        override fun mouseEntered(e: MouseEvent) { hover = true; repaint() }
        override fun mouseExited(e: MouseEvent) { hover = false; repaint() }
        override fun mouseReleased(e: MouseEvent) {
            if (e.button == MouseEvent.BUTTON1 && contains(e.point)) onClick?.invoke()
        }
    }

    open fun hfw(w: Int): Int = 0
    open fun naturalWidth(): Int = 0

    override fun getPreferredSize(): Dimension {
        val w = if (width > 0) width else naturalWidth().coerceAtLeast(1)
        return Dimension(naturalWidth(), hfw(w))
    }

    override fun paint(g: Graphics) {
        super.paint(g)
        if (hover && onClick != null && isEnabled) {
            val g2 = g as Graphics2D
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2.color = Theme.alpha(Theme.p.ink, if (Theme.dark) 18 else 10)
            g2.fill(RoundRectangle2D.Float(0f, 0f, width.toFloat(), height.toFloat(), hoverRadius * 2f, hoverRadius * 2f))
        }
    }

    companion object {
        fun heightOf(c: Component, w: Int) = if (c is V) c.hfw(w) else c.preferredSize.height
        fun widthOf(c: Component) = if (c is V) c.naturalWidth() else c.preferredSize.width
        fun aa(g: Graphics): Graphics2D = (g as Graphics2D).apply {
            setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB)
            setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
        }
    }
}

/** Children one under the other, full width. Optional rounded background (a card). */
open class Col(
    var pad: Insets = Insets(0, 0, 0, 0), var gap: Int = 0,
    var bg: Color? = null, var radius: Int = 0, var stroke: Color? = null,
    var gradient: Pair<Color, Color>? = null,
) : V() {
    init { layout = null; isOpaque = false }

    /** Adds with space above (the second number is a top margin here, not a position). */
    override fun add(c: Component, top: Int): Component { if (c is V) c.top = top; return super.add(c) }
    private fun topOf(c: Component) = if (c is V) c.top else 0

    override fun hfw(w: Int): Int {
        val iw = w - pad.left - pad.right
        var h = pad.top + pad.bottom
        components.filter { it.isVisible }.forEachIndexed { i, c -> h += heightOf(c, iw) + topOf(c) + if (i > 0) gap else 0 }
        return h
    }

    override fun naturalWidth() = (components.maxOfOrNull { widthOf(it) } ?: 0) + pad.left + pad.right

    override fun doLayout() {
        val iw = width - pad.left - pad.right
        var y = pad.top
        components.filter { it.isVisible }.forEachIndexed { i, c ->
            y += topOf(c) + if (i > 0) gap else 0
            val h = heightOf(c, iw)
            c.setBounds(pad.left, y, iw, h)
            c.doLayout()
            y += h
        }
    }

    private fun shape() = RoundRectangle2D.Float(0f, 0f, width.toFloat(), height.toFloat(), radius * 2f, radius * 2f)

    override fun paintComponent(g: Graphics) {
        val g2 = aa(g)
        gradient?.let { (a, b) -> g2.paint = GradientPaint(0f, 0f, a, width.toFloat(), height.toFloat(), b); g2.fill(shape()) }
        bg?.let { g2.color = it; g2.fill(shape()) }
    }

    override fun paintChildren(g: Graphics) {
        if (radius > 0) { val g2 = g.create() as Graphics2D; g2.clip(shape()); super.paintChildren(g2); g2.dispose() } else super.paintChildren(g)
        stroke?.let { s ->
            val g2 = aa(g.create())
            g2.color = s; g2.stroke = BasicStroke(1f)
            g2.draw(RoundRectangle2D.Float(0.5f, 0.5f, width - 1f, height - 1f, radius * 2f, radius * 2f)); g2.dispose()
        }
    }
}

/** Children side by side, vertically centred. Weight 0 = its own width; the rest share what's left. */
open class Row(var pad: Insets = Insets(0, 0, 0, 0), var gap: Int = 0, var bg: Color? = null, var radius: Int = 0,
               var stroke: Color? = null, var minH: Int = 0) : V() {
    private val weights = HashMap<Component, Float>()
    init { layout = null; isOpaque = false }

    fun add(c: Component, weight: Float): Component { weights[c] = weight; return add(c) }

    private fun widths(w: Int): Map<Component, Int> {
        val vis = components.filter { it.isVisible }
        val iw = w - pad.left - pad.right - gap * (vis.size - 1).coerceAtLeast(0)
        val fixed = vis.filter { (weights[it] ?: 0f) == 0f }.associateWith { widthOf(it) }
        val left = (iw - fixed.values.sum()).coerceAtLeast(0)
        val total = vis.sumOf { (weights[it] ?: 0f).toDouble() }.toFloat()
        return vis.associateWith { c -> fixed[c] ?: ((weights[c] ?: 0f) / total * left).toInt() }
    }

    override fun hfw(w: Int): Int {
        val ws = widths(w)
        return maxOf(minH, (ws.maxOfOrNull { (c, cw) -> heightOf(c, cw) } ?: 0) + pad.top + pad.bottom)
    }

    override fun naturalWidth() = components.filter { it.isVisible }.sumOf { widthOf(it) } + pad.left + pad.right +
        gap * (components.count { it.isVisible } - 1).coerceAtLeast(0)

    override fun doLayout() {
        val ws = widths(width)
        var x = pad.left
        val ih = height - pad.top - pad.bottom
        components.filter { it.isVisible }.forEach { c ->
            val cw = ws[c] ?: 0
            val h = heightOf(c, cw).coerceAtMost(ih)
            c.setBounds(x, pad.top + (ih - h) / 2, cw, h)
            c.doLayout()
            x += cw + gap
        }
    }

    override fun paintComponent(g: Graphics) {
        val g2 = aa(g)
        bg?.let { g2.color = it; g2.fill(RoundRectangle2D.Float(0f, 0f, width.toFloat(), height.toFloat(), radius * 2f, radius * 2f)) }
        stroke?.let { g2.color = it; g2.draw(RoundRectangle2D.Float(0.5f, 0.5f, width - 1f, height - 1f, radius * 2f, radius * 2f)) }
    }
}

/** Text that wraps to its width (emoji drawn with the emoji font). */
class Txt(text: String, var size: Float, var color: Color, var bold: Boolean = true, var maxLines: Int = 0,
          var center: Boolean = false, var lineSpacing: Float = 1.15f) : V() {
    var text = text; set(v) { field = v; revalidate(); repaint() }
    private val tfont get() = Theme.font(size, bold)
    init { isOpaque = false }

    private fun attributed(): AttributedString? {
        if (text.isEmpty()) return null
        val a = AttributedString(text)
        val f = tfont
        a.addAttribute(TextAttribute.FONT, f)
        val emoji = Theme.emojiFont(size)
        var i = 0
        while (i < text.length) {
            val cp = text.codePointAt(i); val n = Character.charCount(cp)
            if (!f.canDisplay(cp)) a.addAttribute(TextAttribute.FONT, emoji?.takeIf { it.canDisplay(cp) } ?: Font(Font.DIALOG, if (bold) Font.BOLD else Font.PLAIN, size.toInt()), i, i + n)
            i += n
        }
        return a
    }

    private fun lines(w: Int): List<TextLayout> {
        val out = ArrayList<TextLayout>()
        val frc = java.awt.font.FontRenderContext(null, true, true)
        for (para in text.split('\n')) {
            if (para.isEmpty()) { out += TextLayout(" ", tfont, frc); continue }
            val a = AttributedString(para).also { s -> attributedPara(s, para) }
            val m = LineBreakMeasurer(a.iterator, frc)
            while (m.position < para.length) {
                if (maxLines > 0 && out.size == maxLines - 1) {
                    // Last line: the rest, cut with …
                    val start = m.position
                    val rest = para.substring(start)
                    val whole = TextLayout(AttributedString(rest).also { s -> attributedPara(s, rest) }.iterator, frc)
                    if (whole.advance <= w) { out += whole; return out }
                    var cut = rest
                    while (cut.length > 1 && TextLayout(cut + "…", tfont, frc).advance > w) cut = cut.dropLast(1)
                    out += TextLayout(cut.trimEnd() + "…", tfont, frc)
                    return out
                }
                out += m.nextLayout(w.coerceAtLeast(10).toFloat())
            }
        }
        return out
    }

    private fun attributedPara(a: AttributedString, para: String) {
        val f = tfont
        a.addAttribute(TextAttribute.FONT, f)
        val emoji = Theme.emojiFont(size)
        var i = 0
        while (i < para.length) {
            val cp = para.codePointAt(i); val n = Character.charCount(cp)
            if (!f.canDisplay(cp)) a.addAttribute(TextAttribute.FONT, emoji?.takeIf { it.canDisplay(cp) } ?: Font(Font.DIALOG, if (bold) Font.BOLD else Font.PLAIN, size.toInt()), i, i + n)
            i += n
        }
    }

    private fun lineH(l: TextLayout) = (l.ascent + l.descent) * lineSpacing

    override fun hfw(w: Int): Int = if (text.isEmpty()) 0 else Math.ceil(lines(w).sumOf { lineH(it).toDouble() }).toInt() + 1

    override fun naturalWidth(): Int {
        if (text.isEmpty()) return 0
        val frc = java.awt.font.FontRenderContext(null, true, true)
        val a = attributed() ?: return 0
        return text.split('\n').maxOf { p -> if (p.isEmpty()) 0 else TextLayout(AttributedString(p).also { attributedPara(it, p) }.iterator, frc).advance.toInt() + 2 }
    }

    override fun paintComponent(g: Graphics) {
        val g2 = aa(g)
        g2.color = color
        var y = 0f
        for (l in lines(width)) {
            y += l.ascent
            val x = if (center) (width - l.advance) / 2 else 0f
            l.draw(g2, x, y)
            y += l.descent + (lineH(l) - l.ascent - l.descent)
        }
    }
}

class Gap(val h: Int) : V() { override fun hfw(w: Int) = h }

/** One of the phone's line icons. */
class Ico(var icon: String, val size: Int, var color: Color) : V() {
    init { isOpaque = false }
    override fun hfw(w: Int) = size
    override fun naturalWidth() = size
    override fun paintComponent(g: Graphics) = Icons.paint(aa(g), icon, (width - size) / 2, (height - size) / 2, size, color)
}

/** The coloured rounded square behind a row's icon. */
class TileV(val icon: String, val tone: String, val box: Int = 34, val iconSize: Int = 19) : V() {
    override fun hfw(w: Int) = box
    override fun naturalWidth() = box
    override fun paintComponent(g: Graphics) {
        val g2 = aa(g)
        val (bg, ink) = Theme.tone(tone)
        g2.color = bg; g2.fill(RoundRectangle2D.Float(0f, 0f, box.toFloat(), box.toFloat(), 20f, 20f))
        Icons.paint(g2, icon, (box - iconSize) / 2, (box - iconSize) / 2, iconSize, ink)
    }
}

/** An on/off switch like the phone's. */
class Switch(on: Boolean, val onChange: (Boolean) -> Unit) : V() {
    var on = on; set(v) { field = v; repaint() }
    init { onClick = { this.on = !this.on; onChange(this.on) }; hoverRadius = 12 }
    override fun hfw(w: Int) = 26
    override fun naturalWidth() = 46
    override fun paintComponent(g: Graphics) {
        val g2 = aa(g); val p = Theme.p
        val y = (height - 20) / 2f
        g2.color = if (on) p.accentSoft else p.track
        g2.fill(RoundRectangle2D.Float(2f, y, 42f, 20f, 20f, 20f))
        val x = if (on) 24f else 2f
        g2.color = Color(0, 0, 0, 40); g2.fill(Ellipse2D.Float(x, y - 1f, 22f, 22f))
        g2.color = if (on) p.accent else if (Theme.dark) Color(0xC3, 0xC9, 0xDA) else Color.WHITE
        g2.fill(Ellipse2D.Float(x, y - 2f, 22f, 22f))
    }
}

/** A text box in the app's style. */
class Field(hint: String, val multi: Boolean = false, rows: Int = 4, text: String = "", val password: Boolean = false) : V() {
    val box: JTextComponent = when {
        multi -> JTextArea(text, rows, 10).apply { lineWrap = true; wrapStyleWord = true }
        password -> javax.swing.JPasswordField(text)
        else -> JTextField(text)
    }
    private val scroll = if (multi) JScrollPane(box).apply { border = null; isOpaque = false; viewport.isOpaque = false } else null
    private val rowsH = rows
    val hintText = hint
    var value: String get() = box.text; set(v) { box.text = v }

    init {
        layout = null; isOpaque = false
        val p = Theme.p
        box.isOpaque = false
        box.border = EmptyBorder(0, 0, 0, 0)
        box.font = Theme.font(15f, false)
        box.foreground = p.ink; box.caretColor = p.ink
        box.selectionColor = p.accentSoft; box.selectedTextColor = p.ink
        add(scroll ?: box)
        box.addFocusListener(object : java.awt.event.FocusAdapter() {
            override fun focusGained(e: java.awt.event.FocusEvent) = repaint()
            override fun focusLost(e: java.awt.event.FocusEvent) = repaint()
        })
        box.document.addDocumentListener(object : javax.swing.event.DocumentListener {
            override fun insertUpdate(e: javax.swing.event.DocumentEvent) = repaint()
            override fun removeUpdate(e: javax.swing.event.DocumentEvent) = repaint()
            override fun changedUpdate(e: javax.swing.event.DocumentEvent) = repaint()
        })
    }

    override fun hfw(w: Int) = if (multi) 24 + rowsH * 21 else 46
    override fun naturalWidth() = 120
    override fun doLayout() { (scroll ?: box).setBounds(14, if (multi) 12 else 0, width - 28, if (multi) height - 24 else height) }

    override fun paintComponent(g: Graphics) {
        val g2 = aa(g); val p = Theme.p
        g2.color = p.field; g2.fill(RoundRectangle2D.Float(0f, 0f, width.toFloat(), height.toFloat(), 24f, 24f))
        g2.color = if (box.isFocusOwner) p.accent else p.border
        g2.stroke = BasicStroke(if (box.isFocusOwner) 1.6f else 1f)
        g2.draw(RoundRectangle2D.Float(0.5f, 0.5f, width - 1f, height - 1f, 24f, 24f))
    }

    override fun paintChildren(g: Graphics) {
        super.paintChildren(g)
        if (box.text.isEmpty()) {
            val g2 = aa(g); g2.color = Theme.p.hint; g2.font = Theme.font(15f, false)
            val fm = g2.fontMetrics
            g2.drawString(hintText, 14, if (multi) 12 + fm.ascent else (height + fm.ascent - fm.descent) / 2)
        }
    }
}

/** Scrolling page: as wide as the window, scrolls down. */
class Page(pad: Insets = Insets(20, 20, 40, 20)) : Col(pad), Scrollable {
    override fun getPreferredSize(): Dimension {
        val w = (parent?.width ?: 440).coerceAtLeast(200)
        return Dimension(w, hfw(w))
    }
    override fun getPreferredScrollableViewportSize() = preferredSize
    override fun getScrollableUnitIncrement(r: Rectangle, o: Int, d: Int) = 24
    override fun getScrollableBlockIncrement(r: Rectangle, o: Int, d: Int) = r.height - 40
    override fun getScrollableTracksViewportWidth() = true
    override fun getScrollableTracksViewportHeight() = false
    fun scroller() = JScrollPane(this, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER).apply {
        border = null; viewport.background = Theme.p.bg; background = Theme.p.bg
        verticalScrollBar.unitIncrement = 24
        verticalScrollBar.ui = SlimScroll()
        verticalScrollBar.preferredSize = Dimension(10, 10)
        verticalScrollBar.isOpaque = false
    }
}

/** A thin rounded scroll bar instead of the system one. */
class SlimScroll : javax.swing.plaf.basic.BasicScrollBarUI() {
    private fun none() = javax.swing.JButton().apply { preferredSize = Dimension(0, 0); minimumSize = Dimension(0, 0); maximumSize = Dimension(0, 0) }
    override fun createDecreaseButton(o: Int) = none()
    override fun createIncreaseButton(o: Int) = none()
    override fun paintTrack(g: Graphics, c: JComponent, r: Rectangle) { g.color = Theme.p.bg; g.fillRect(r.x, r.y, r.width, r.height) }
    override fun paintThumb(g: Graphics, c: JComponent, r: Rectangle) {
        val g2 = V.aa(g); g2.color = Theme.alpha(Theme.p.muted, 110)
        g2.fill(RoundRectangle2D.Float(r.x + 2f, r.y + 2f, r.width - 4f, r.height - 4f, 8f, 8f))
    }
}

// ---------------- the phone's building blocks ----------------

object K {
    val p get() = Theme.p

    fun text(s: String, size: Float, color: Color, bold: Boolean = true, maxLines: Int = 0) = Txt(s, size, color, bold, maxLines)
    fun note(s: String) = Txt(s, 13f, p.muted, false, lineSpacing = 1.3f)

    fun card(pad: Int = 0) = Col(Insets(pad, pad, pad, pad), bg = p.card, radius = 20, stroke = p.border)

    fun sectionTitle(s: String) = Col(Insets(32, 6, 10, 6)).apply { add(Txt(s.uppercase(), 13f, p.muted, true)) }

    /** Settings row: icon tile, label (+ second line), value, chevron. */
    fun row(icon: String, tone: String, label: String, value: String? = null, sub: String? = null,
            valueColor: Color? = null, chevron: Boolean = true, danger: Boolean = false, onClick: (() -> Unit)? = null): Row {
        val r = Row(Insets(14, 16, 14, 16), 14, minH = 66)
        r.add(TileV(icon, if (danger) "red" else tone), 0f)
        val col = Col(gap = 3)
        col.add(Txt(label, 15f, if (danger) p.danger else p.ink, true))
        if (!sub.isNullOrEmpty()) col.add(Txt(sub, 13f, p.muted, false, lineSpacing = 1.2f))
        r.add(col, 1f)
        if (!value.isNullOrEmpty()) r.add(object : V() {
            val t = Txt(value, 14f, valueColor ?: p.muted, true, maxLines = 1)
            init { layout = null; add(t) }
            override fun hfw(w: Int) = t.hfw(w)
            override fun naturalWidth() = t.naturalWidth().coerceAtMost(160)
            override fun doLayout() { t.setBounds(0, 0, width, height) }
        }, 0f)
        if (chevron) r.add(Ico("ui_chevron", 18, p.muted), 0f)
        if (onClick != null) r.onClick = onClick
        return r
    }

    fun switchRow(icon: String, tone: String, label: String, sub: String?, checked: Boolean, onChange: (Boolean) -> Unit): Row {
        val r = Row(Insets(14, 16, 14, 16), 14, minH = 66)
        r.add(TileV(icon, tone), 0f)
        val col = Col(gap = 3)
        col.add(Txt(label, 15f, p.ink, true))
        if (!sub.isNullOrEmpty()) col.add(Txt(sub, 13f, p.muted, false, lineSpacing = 1.2f))
        r.add(col, 1f)
        val sw = Switch(checked, onChange)
        r.add(sw, 0f)
        r.onClick = { sw.on = !sw.on; onChange(sw.on) }
        return r
    }

    fun divider(indent: Int = 64) = object : V() {
        override fun hfw(w: Int) = 1
        override fun paintComponent(g: Graphics) { g.color = Theme.p.divider; g.fillRect(indent, 0, width - indent, 1) }
    }

    /** A card of rows with thin lines between them. */
    fun group(rows: List<V>): Col = card().apply {
        rows.forEachIndexed { i, r -> if (i > 0) add(divider()); add(r) }
    }

    fun button(label: String, icon: String?, bg: Color, fg: Color, height: Int = 50, stroke: Color? = null, onClick: () -> Unit): Row {
        val r = object : Row(Insets(0, 14, 0, 14), 7, bg, 15, stroke, minH = height) {
            override fun doLayout() {
                // Centre the icon + label.
                val vis = components.toList()
                val total = vis.sumOf { widthOf(it) } + 7 * (vis.size - 1)
                var x = (width - total) / 2
                vis.forEach { c -> val w = widthOf(c); val h = heightOf(c, w); c.setBounds(x, (height - h) / 2, w, h); x += w + 7 }
            }
        }
        if (icon != null) r.add(Ico(icon, 19, fg), 0f)
        r.add(Txt(label, 15f, fg, true, maxLines = 1), 0f)
        r.onClick = onClick; r.hoverRadius = 15
        return r
    }

    fun primary(label: String, icon: String? = null, onClick: () -> Unit) = button(label, icon, p.accent, Color.WHITE, onClick = onClick)
    fun secondary(label: String, icon: String? = null, onClick: () -> Unit) = button(label, icon, p.accentSoft, p.accentInk, onClick = onClick)

    fun iconButton(icon: String, onClick: () -> Unit): V = object : V() {
        init { this.onClick = onClick; hoverRadius = 14; toolTipText = null }
        override fun hfw(w: Int) = 46
        override fun naturalWidth() = 46
        override fun paintComponent(g: Graphics) {
            val g2 = aa(g)
            g2.color = p.card; g2.fill(RoundRectangle2D.Float(0f, 0f, 46f, 46f, 28f, 28f))
            g2.color = p.border; g2.draw(RoundRectangle2D.Float(0.5f, 0.5f, 45f, 45f, 28f, 28f))
            Icons.paint(g2, icon, 12, 12, 22, p.ink)
        }
    }

    /** Big title with an optional gear in the corner. */
    fun header(title: String, sub: String?, gear: (() -> Unit)?): Row {
        val r = Row(gap = 12)
        val col = Col(gap = 2)
        col.add(Txt(title, 28f, p.ink, true))
        if (sub != null) col.add(Txt(sub, 14f, p.muted, false))
        r.add(col, 1f)
        if (gear != null) r.add(iconButton("ui_gear", gear), 0f)
        return r
    }

    fun backHeader(title: String, back: () -> Unit): Row {
        val r = Row(gap = 14)
        r.add(iconButton("ui_back", back), 0f)
        r.add(Txt(title, 24f, p.ink, true), 1f)
        return r
    }

    fun segmented(options: List<String>, selected: Int, onPick: (Int) -> Unit): Row {
        val r = Row(Insets(4, 4, 4, 4), 0, p.track, 14)
        options.forEachIndexed { i, label ->
            val on = i == selected
            val cell = object : V() {
                val t = Txt(label, 14f, if (on) p.ink else p.muted, true, maxLines = 1, center = true)
                init { layout = null; add(t); if (!on) { onClick = { onPick(i) }; hoverRadius = 11 } }
                override fun hfw(w: Int) = 38
                override fun naturalWidth() = t.naturalWidth() + 8
                override fun doLayout() { val h = t.hfw(width - 8); t.setBounds(4, (height - h) / 2, width - 8, h) }
                override fun paintComponent(g: Graphics) {
                    if (on) { val g2 = aa(g); g2.color = p.card; g2.fill(RoundRectangle2D.Float(0f, 0f, width.toFloat(), height.toFloat(), 22f, 22f)) }
                }
            }
            r.add(cell, 1f)
        }
        return r
    }

    /** A rounded progress bar. */
    fun bar(fraction: Float, fill: Color, track: Color) = object : V() {
        var f = fraction
        override fun hfw(w: Int) = 10
        override fun paintComponent(g: Graphics) {
            val g2 = aa(g)
            g2.color = track; g2.fill(RoundRectangle2D.Float(0f, 0f, width.toFloat(), 10f, 10f, 10f))
            g2.color = fill; g2.fill(RoundRectangle2D.Float(0f, 0f, width * f.coerceIn(0f, 1f), 10f, 10f, 10f))
        }
    }

    /** A small pill: icon + text (clickable). */
    fun chip(icon: String?, label: String, on: Boolean, onClick: () -> Unit): Row {
        val r = Row(Insets(7, 11, 7, 13), 6, if (on) p.accentSoft else p.card, 999, if (on) p.accent else p.border)
        if (icon != null) r.add(Ico(icon, 17, if (on) p.accentInk else p.muted), 0f)
        r.add(Txt(label, 14f, if (on) p.accentInk else p.ink, true, maxLines = 1), 0f)
        r.onClick = onClick; r.hoverRadius = 18
        return r
    }

    /** A fixed-size holder (e.g. a 2-column grid cell). */
    fun grid(cols: Int, gap: Int, items: List<V>): Col = Col(gap = gap).apply {
        items.chunked(cols).forEach { chunk ->
            val row = Row(gap = gap)
            chunk.forEach { row.add(it, 1f) }
            repeat(cols - chunk.size) { row.add(Gap(1), 1f) }
            add(row)
        }
    }

    fun centered(c: V) = object : V() {
        init { layout = null; add(c) }
        override fun hfw(w: Int) = c.hfw(minOf(w, c.naturalWidth()))
        override fun doLayout() { val cw = minOf(width, c.naturalWidth()); c.setBounds((width - cw) / 2, 0, cw, height); c.doLayout() }
    }

    @Suppress("unused") val left = SwingConstants.LEFT
}
