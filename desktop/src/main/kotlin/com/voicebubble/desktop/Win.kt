package com.voicebubble.desktop

import com.sun.jna.Native
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.ptr.IntByReference

/**
 * The window you're typing in. Voice Bubble keeps track of the last window that isn't its own, so
 * clicking the bubble (which takes the focus for a moment) still types into the right place.
 */
object Win {
    private val ourPid = ProcessHandle.current().pid().toInt()
    @Volatile private var target: HWND? = null

    /** Watches which window is in front (Windows only). */
    fun startTracking() {
        if (!App.isWindows) return
        Thread({
            while (true) {
                runCatching {
                    val h = User32.INSTANCE.GetForegroundWindow()
                    if (h != null && pidOf(h) != ourPid) target = h
                }
                Thread.sleep(200)
            }
        }, "window-tracker").apply { isDaemon = true }.start()
    }

    private fun pidOf(h: HWND): Int {
        val pid = IntByReference()
        User32.INSTANCE.GetWindowThreadProcessId(h, pid)
        return pid.value
    }

    /** Puts the window you were typing in back in front (after you clicked the bubble). */
    fun focusTarget() {
        if (!App.isWindows) return
        val t = target ?: return
        runCatching {
            if (User32.INSTANCE.GetForegroundWindow() != t) {
                User32.INSTANCE.SetForegroundWindow(t)
                Thread.sleep(120)
            }
        }
    }

    /** The program's name without ".exe", lower case (e.g. "discord"), or null. */
    fun targetExe(): String? {
        if (!App.isWindows) return null
        val t = target ?: return null
        return runCatching {
            val h = Kernel32.INSTANCE.OpenProcess(0x1000, false, pidOf(t)) ?: return null
            try {
                val buf = CharArray(1024)
                val len = IntByReference(buf.size)
                if (!Kernel32.INSTANCE.QueryFullProcessImageName(h, 0, buf, len)) return null
                String(buf, 0, len.value).substringAfterLast('\\').removeSuffix(".exe").removeSuffix(".EXE").lowercase()
            } finally { Kernel32.INSTANCE.CloseHandle(h) }
        }.getOrNull()
    }

    fun targetTitle(): String? {
        if (!App.isWindows) return null
        val t = target ?: return null
        return runCatching {
            val buf = CharArray(512)
            val n = User32.INSTANCE.GetWindowText(t, buf, buf.size)
            String(buf, 0, n)
        }.getOrNull()
    }

    @Suppress("unused") private val keepJna = Native::class
}
