package com.voicebubble.desktop

import java.awt.Robot
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.event.KeyEvent

/**
 * Types into the program you're using: the text goes through the clipboard and Ctrl+V (works with every
 * language and emoji), and whatever was on your clipboard is put back right after.
 */
object Typer {
    private val robot by lazy { Robot().apply { autoDelay = 8 } }
    private val clip get() = Toolkit.getDefaultToolkit().systemClipboard
    /** What was typed last, so it can be undone or swapped for a rewrite. */
    @Volatile var last: String? = null

    private fun releaseModifiers() {
        for (k in listOf(KeyEvent.VK_SHIFT, KeyEvent.VK_CONTROL, KeyEvent.VK_ALT, KeyEvent.VK_WINDOWS))
            runCatching { robot.keyRelease(k) }
    }

    private fun combo(vararg keys: Int) {
        keys.forEach { robot.keyPress(it) }
        keys.reversed().forEach { robot.keyRelease(it) }
    }

    private fun readClip(): String? = runCatching {
        if (clip.isDataFlavorAvailable(DataFlavor.stringFlavor)) clip.getData(DataFlavor.stringFlavor) as String else null
    }.getOrNull()

    /** Types the text where your cursor is. */
    @Synchronized fun type(text: String) {
        if (text.isEmpty()) return
        Win.focusTarget()
        releaseModifiers()
        val old = readClip()
        clip.setContents(StringSelection(text), null)
        Thread.sleep(40)
        combo(KeyEvent.VK_CONTROL, KeyEvent.VK_V)
        last = text
        // Your own clipboard back (once the program has pasted).
        Thread {
            Thread.sleep(700)
            runCatching { if (readClip() == text) clip.setContents(StringSelection(old ?: ""), null) }
        }.start()
    }

    /** Selects what was just typed (it's right before the cursor). */
    private fun selectLast(text: String) {
        val n = text.codePointCount(0, text.length)
        robot.keyPress(KeyEvent.VK_SHIFT)
        repeat(n) { robot.keyPress(KeyEvent.VK_LEFT); robot.keyRelease(KeyEvent.VK_LEFT) }
        robot.keyRelease(KeyEvent.VK_SHIFT)
    }

    /** Swaps the last typed text for a new one (rewrite). False when there's nothing to swap. */
    @Synchronized fun replaceLast(newText: String): Boolean {
        val old = last ?: return false
        Win.focusTarget(); releaseModifiers()
        selectLast(old)
        type(newText)
        return true
    }

    /** Takes back the last typed text ("scratch that", "undo"). */
    @Synchronized fun undo(): Boolean {
        val old = last ?: return false
        Win.focusTarget(); releaseModifiers()
        selectLast(old)
        combo(KeyEvent.VK_DELETE)
        last = null
        return true
    }

    /** The text you've selected in the program (for "reply" and "summarise"), or null. */
    fun copySelection(): String? {
        Win.focusTarget(); releaseModifiers()
        val old = readClip()
        clip.setContents(StringSelection(""), null)
        combo(KeyEvent.VK_CONTROL, KeyEvent.VK_C)
        Thread.sleep(250)
        val got = readClip()?.takeIf { it.isNotBlank() }
        runCatching { clip.setContents(StringSelection(old ?: ""), null) }
        return got
    }

    fun copy(text: String) = clip.setContents(StringSelection(text), null)
}
