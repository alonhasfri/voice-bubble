package com.voicebubble.desktop

import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.NativeInputEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import java.util.logging.Level
import java.util.logging.Logger

/**
 * The keyboard shortcut (default Ctrl+Shift+Space) that works in every program: press to talk, press again to type
 * it. Or "hold to talk": hold it while you speak, let go to type. Esc while listening cancels.
 */
object Hotkey {
    data class Combo(val ctrl: Boolean, val shift: Boolean, val alt: Boolean, val win: Boolean, val key: Int)

    private val names: Map<String, Int> by lazy {
        val m = HashMap<String, Int>()
        for (c in 'a'..'z') NativeKeyEvent::class.java.getField("VC_${c.uppercaseChar()}").getInt(null).let { m["$c"] = it }
        for (d in 0..9) m["$d"] = NativeKeyEvent::class.java.getField("VC_$d").getInt(null)
        for (f in 1..12) m["f$f"] = NativeKeyEvent::class.java.getField("VC_F$f").getInt(null)
        m["space"] = NativeKeyEvent.VC_SPACE; m["enter"] = NativeKeyEvent.VC_ENTER; m["tab"] = NativeKeyEvent.VC_TAB
        m["insert"] = NativeKeyEvent.VC_INSERT; m["pause"] = NativeKeyEvent.VC_PAUSE; m["scrolllock"] = NativeKeyEvent.VC_SCROLL_LOCK
        m["`"] = NativeKeyEvent.VC_BACKQUOTE
        m
    }

    fun parse(s: String): Combo? {
        val parts = s.lowercase().split('+').map { it.trim() }.filter { it.isNotEmpty() }
        val key = parts.lastOrNull()?.let { names[it] } ?: return null
        return Combo("ctrl" in parts, "shift" in parts, "alt" in parts, "win" in parts, key)
    }

    /** "Ctrl+Shift+Space" for showing. */
    fun pretty(s: String) = s.split('+').joinToString("+") { p -> if (p.length <= 1) p.uppercase() else p.replaceFirstChar { it.uppercaseChar() } }

    private var combo: Combo? = null
    @Volatile private var down = false
    /** While Settings is listening for a new shortcut, the next key press goes there. */
    @Volatile var capture: ((String) -> Unit)? = null

    fun start() {
        Logger.getLogger(GlobalScreen::class.java.`package`.name).apply { level = Level.WARNING; useParentHandlers = false }
        combo = parse(App.hotkey) ?: parse("ctrl+shift+space")
        runCatching { GlobalScreen.registerNativeHook() }.onFailure { Toast.show("Keyboard shortcut unavailable: ${it.message}"); return }
        GlobalScreen.addNativeKeyListener(object : NativeKeyListener {
            override fun nativeKeyPressed(e: NativeKeyEvent) {
                val m = e.modifiers
                val ctrl = m and NativeInputEvent.CTRL_MASK != 0
                val shift = m and NativeInputEvent.SHIFT_MASK != 0
                val alt = m and NativeInputEvent.ALT_MASK != 0
                val win = m and NativeInputEvent.META_MASK != 0
                capture?.let { cb ->
                    val name = names.entries.firstOrNull { it.value == e.keyCode }?.key ?: return
                    capture = null
                    cb(listOfNotNull("ctrl".takeIf { ctrl }, "shift".takeIf { shift }, "alt".takeIf { alt }, "win".takeIf { win }, name).joinToString("+"))
                    return
                }
                if (e.keyCode == NativeKeyEvent.VC_ESCAPE && Engine.state != Engine.State.IDLE) { Engine.cancel(); return }
                val c = combo ?: return
                if (e.keyCode != c.key || ctrl != c.ctrl || shift != c.shift || alt != c.alt || win != c.win) return
                if (down) return   // held: the key repeats
                down = true
                Thread { if (App.holdToTalk) Engine.start() else Engine.toggle() }.start()
            }
            override fun nativeKeyReleased(e: NativeKeyEvent) {
                val c = combo ?: return
                if (e.keyCode != c.key) return
                down = false
                if (App.holdToTalk) Thread { Engine.stop() }.start()
            }
        })
    }

    fun reload() { combo = parse(App.hotkey) ?: combo }

    fun stop() = runCatching { GlobalScreen.unregisterNativeHook() }
}
