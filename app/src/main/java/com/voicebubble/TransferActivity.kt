package com.voicebubble

import android.content.Intent
import android.graphics.Typeface
import android.view.Gravity
import android.widget.TextView

/** Settings → Move to your computer: shows a short code to type into Voice Bubble for Windows. */
class TransferActivity : Screen() {
    @Volatile private var running = false
    @Volatile private var sent = false
    private var code: Transfer.Code? = null
    private var status: TextView? = null

    override fun onResume() {
        super.onResume()
        start()
    }

    override fun onPause() {
        super.onPause()
        running = false
    }

    private fun start() {
        if (running || sent) return
        val ip = Transfer.localIp()
        code = ip?.let { Transfer.newCode(it) }
        render()
        val c = code ?: return
        running = true
        Thread {
            try {
                Transfer.serve(applicationContext, c, stop = { !running }) {
                    sent = true
                    runOnUiThread { render(); toast("Sent to your computer ✓") }
                }
            } catch (e: Exception) {
                runOnUiThread { status?.text = "Couldn't start: ${e.message}" }
            } finally { running = false }
        }.start()
    }

    override fun render() {
        val page = Ui.page(this)
        page.addView(Ui.backHeader(this, "Move to your computer"))
        page.addView(Ui.note(this, "Moves your settings, AI key, characters, dictionary, shortcuts, word packs, styles, " +
            "skins, history and what it learned to Voice Bubble for Windows.").apply { setPaddingRelative(dp(4), dp(12), dp(4), 0) })

        val card = Ui.paddedCard(this)
        val c = code
        when {
            sent -> {
                card.addView(Ui.text(this, "Done ✓", 26f, p.good).apply { gravity = Gravity.CENTER })
                card.addView(Ui.note(this, "Everything is on your computer now. You can close this.").apply { gravity = Gravity.CENTER; setPaddingRelative(0, dp(6), 0, 0) })
            }
            c == null -> {
                card.addView(Ui.text(this, "Connect to Wi-Fi", 20f, p.ink))
                card.addView(Ui.note(this, "Your phone and computer need to be on the same Wi-Fi (or the phone's hotspot). " +
                    "Or use the long code below.").apply { setPaddingRelative(0, dp(6), 0, 0) })
                card.addView(Ui.secondary(this, "Try again", R.drawable.ui_reset) { start() }, Ui.lp(this, 14))
            }
            else -> {
                card.addView(Ui.text(this, "On your computer: Voice Bubble → gear → Move from your phone, and type:", 14f, p.muted, bold = false))
                card.addView(Ui.text(this, c.text, 30f, p.ink).apply {
                    typeface = Typeface.MONOSPACE; setTypeface(typeface, Typeface.BOLD)
                    gravity = Gravity.CENTER; letterSpacing = 0.04f
                    setPaddingRelative(0, dp(16), 0, dp(16))
                    setTextIsSelectable(true)
                }, Ui.lp(this, 10))
                status = Ui.text(this, "Waiting for your computer… keep this screen open", 13f, p.muted, bold = false).apply { gravity = Gravity.CENTER }
                card.addView(status)
            }
        }
        page.addView(card, Ui.lp(this, 16))

        page.addView(Ui.sectionTitle(this, "Not on the same Wi-Fi?"))
        page.addView(Ui.group(this, listOf(
            Ui.row(this, R.drawable.ui_copy, "blue", "Copy a long code instead",
                sub = "Send it to yourself (e.g. email) and paste it on the computer") {
                copy(Transfer.longCode(this)); toast("Long code copied")
            },
            Ui.row(this, R.drawable.ui_share, "purple", "Share the long code", sub = "To an app that opens on your computer too") {
                startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"; putExtra(Intent.EXTRA_TEXT, Transfer.longCode(this@TransferActivity))
                }, "Send the long code"))
            },
        )))
        page.addView(Ui.note(this, "The codes include your AI key: only use them on your own computer.").apply { setPaddingRelative(dp(6), dp(12), dp(6), 0) })
        Ui.plainScreen(this, page)
    }
}
