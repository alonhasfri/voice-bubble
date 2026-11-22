package com.voicebubble.desktop

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Insets
import java.awt.Window
import java.awt.event.KeyEvent
import java.awt.geom.RoundRectangle2D
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.KeyStroke

/** Dialogs in the app's look (a rounded card), like the phone's. */
object Dlg {
    class Btn(val label: String, val primary: Boolean = false, val action: () -> Boolean = { true })

    var owner: Window? = null

    fun show(title: String?, message: String? = null, content: V? = null, buttons: List<Btn> = listOf(Btn("OK", true)), width: Int = 400): JDialog {
        val p = Theme.p
        val d = JDialog(owner, java.awt.Dialog.ModalityType.APPLICATION_MODAL)
        d.isUndecorated = true
        val card = Col(Insets(22, 22, 18, 22), 0, p.card, 0, p.border)
        if (title != null) card.add(Txt(title, 19f, p.ink, true))
        if (message != null) card.add(Txt(message, 14f, p.muted, false, lineSpacing = 1.3f), if (title != null) 8 else 0)
        if (content != null) card.add(content, 14)
        if (buttons.isNotEmpty()) {
            val row = Row(gap = 8)
            row.add(Gap(1), 1f)
            buttons.forEach { b ->
                row.add(K.button(b.label, null, if (b.primary) p.accent else p.accentSoft, if (b.primary) Color.WHITE else p.accentInk, 42) {
                    if (b.action()) d.dispose()
                }, 0f)
            }
            card.add(row, 18)
        }
        val page = Page(Insets(0, 0, 0, 0)).apply { add(card) }
        val scroll = page.scroller().apply { viewport.background = p.card }
        d.contentPane.layout = BorderLayout()
        d.contentPane.add(scroll)
        val h = card.hfw(width).coerceAtMost(((owner?.height ?: 800) - 60).coerceAtLeast(300))
        d.setSize(width, h)
        d.shape = RoundRectangle2D.Float(0f, 0f, width.toFloat(), h.toFloat(), 28f, 28f)
        d.setLocationRelativeTo(owner)
        d.rootPane.registerKeyboardAction({ d.dispose() }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW)
        d.isVisible = true
        return d
    }

    fun message(title: String, message: String) { show(title, message) }

    fun confirm(title: String, message: String, yes: String, onYes: () -> Unit) {
        show(title, message, buttons = listOf(Btn("Cancel"), Btn(yes, true) { onYes(); true }))
    }

    /** A list to pick from (the current one has a check). */
    fun pick(title: String, items: List<String>, current: Int = -1, message: String? = null, onPick: (Int) -> Unit) {
        val p = Theme.p
        var dlg: JDialog? = null
        val list = Col()
        items.forEachIndexed { i, s ->
            val r = Row(Insets(11, 12, 11, 12), 10)
            r.add(Txt(s, 15f, if (i == current) p.accentInk else p.ink, i == current), 1f)
            if (i == current) r.add(Ico("ui_check", 18, p.accent), 0f)
            r.hoverRadius = 12
            r.onClick = { dlg?.dispose(); onPick(i) }
            list.add(r)
        }
        javax.swing.SwingUtilities.invokeLater { }
        dlg = showLater(title, message, list, listOf(Btn("Cancel")))
    }

    /** A text box. */
    fun input(title: String, message: String?, hint: String, initial: String = "", multi: Boolean = false,
              extra: Btn? = null, onSave: (String) -> Unit) {
        val f = Field(hint, multi, 6, initial)
        val body = Col().apply { add(f) }
        javax.swing.SwingUtilities.invokeLater { f.box.requestFocusInWindow() }
        show(title, message, body, listOfNotNull(extra, Btn("Cancel"), Btn("Save", true) { onSave(f.value); true }))
    }

    // pick() needs its dialog before showing (to close it from a row); modal show blocks, so build it this way.
    private fun showLater(title: String, message: String?, content: V, buttons: List<Btn>): JDialog? {
        var ref: JDialog? = null
        // Rows close the dialog through the window they're in.
        content.components.forEach { c ->
            (c as? V)?.let { v -> val old = v.onClick; v.onClick = { javax.swing.SwingUtilities.getWindowAncestor(v)?.dispose(); old?.invoke() } }
        }
        ref = show(title, message, content, buttons)
        return ref
    }
}
