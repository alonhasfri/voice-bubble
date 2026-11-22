package com.voicebubble

import android.content.Intent
import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/**
 * The skin menu: switches at the top (Simple / Detailed, mic on every skin, animated), the skins of one category
 * in a grid, and the categories as tabs along the bottom (Characters, Themes, Fun, Basic, Mine).
 */
class SkinsActivity : Screen() {
    companion object {
        private const val PICK_SKIN = 41
        private var tab = "characters"
        private val tabs = listOf("characters" to "Characters", "themes" to "Themes", "fun" to "Fun", "basic" to "Basic", "mine" to "Mine")
    }

    private fun refreshBubble() {
        if (BubbleService.running) startForegroundService(Intent(this, BubbleService::class.java))
    }

    override fun render() {
        Ui.systemBars(this)
        val prefs = Prefs(this)
        val page = Ui.page(this)
        page.addView(Ui.backHeader(this, "Bubble skin"))

        // The switches, right here.
        page.addView(Ui.segmented(this, listOf("Simple", "Detailed"), if (prefs.skinDetail) 1 else 0) { i ->
            prefs.skinDetail = i == 1; refreshBubble(); render()
        }, Ui.lp(this, 14))
        val chips = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        fun chip(label: String, on: Boolean, set: (Boolean) -> Unit) = chips.addView(TextView(this).apply {
            text = (if (on) "✓ " else "") + label; textSize = 13f; gravity = Gravity.CENTER
            setTextColor(if (on) p.accentInk else p.muted)
            setPadding(dp(12), dp(9), dp(12), dp(9))
            background = Ui.rounded(if (on) p.accentSoft else p.card, dp(12).toFloat(), p.border, dp(1))
            setOnClickListener { set(!on); refreshBubble(); render() }
        }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { if (chips.childCount > 0) marginStart = dp(8) })
        chip("Mic on every skin", prefs.micOnAll) { prefs.micOnAll = it }
        chip("Animated", prefs.animSkins) { prefs.animSkins = it }
        page.addView(chips, Ui.lp(this, 10))
        if (tab == "characters") page.addView(Ui.note(this, if (prefs.skinDetail) "Detailed: the characters themselves. Simple: their theme (colours and items)."
            else "Simple: each character's theme. Switch to Detailed to see the characters.").apply { setPaddingRelative(dp(4), dp(10), dp(4), 0) })
        if (tab == "mine") page.addView(Ui.note(this, "Hold one of your own skins to share or delete it.").apply { setPaddingRelative(dp(4), dp(10), dp(4), 0) })

        // The grid.
        val grid = GridLayout(this).apply { columnCount = 4 }
        fun cell(preview: View, title: String, chosen: Boolean, onLong: (() -> Unit)? = null, onTap: () -> Unit) {
            val v = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER_HORIZONTAL
                setPaddingRelative(dp(2), dp(8), dp(2), dp(8))
                if (chosen) background = Ui.rounded(p.accentSoft, dp(14).toFloat())
                addView(preview, LinearLayout.LayoutParams(dp(64), dp(64)))
                addView(Ui.text(this@SkinsActivity, title, 12f, if (chosen) p.accentInk else p.ink, bold = chosen).apply {
                    gravity = Gravity.CENTER; maxLines = 2
                })
                isClickable = true
                Ui.ripple(this@SkinsActivity, this)
                setOnClickListener { onTap() }
                onLong?.let { l -> setOnLongClickListener { l(); true } }
            }
            grid.addView(v, GridLayout.LayoutParams().apply { width = 0; columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f) })
        }
        fun actionTile(res: Int, tone: String) = android.widget.FrameLayout(this).apply {
            addView(Ui.tile(this@SkinsActivity, res, tone, 56, 28), android.widget.FrameLayout.LayoutParams(dp(56), dp(56), Gravity.CENTER))
        }
        if (tab == "mine") {
            cell(actionTile(R.drawable.ui_upload, "blue"), "Upload picture", false) {
                @Suppress("DEPRECATION")
                startActivityForResult(Intent(Intent.ACTION_GET_CONTENT).setType("image/*"), PICK_SKIN)
            }
            cell(actionTile(R.drawable.ui_wand, "purple"), "Make with AI", false) { designSkin() }
            cell(actionTile(R.drawable.ui_download, "green"), "Paste a code", false) { pasteSkinCode() }
        }
        Skins.all(this).filter { it.category == tab }.forEach { sk ->
            val manage: (() -> Unit)? = if (sk.custom) ({
                Ui.dialog(this).setTitle(sk.title).setItems(arrayOf("Share with a friend", "Delete")) { _, which ->
                    if (which == 0) shareSkin(sk.id) else {
                        SkinArt.delete(this, sk.id)
                        if (prefs.skin == sk.id) prefs.skin = "classic"
                        refreshBubble(); render()
                    }
                }.setNegativeButton("Cancel", null).show()
                Unit
            }) else null
            cell(BubbleView(this).apply { skin = sk }, sk.title, sk.id == prefs.skin, manage) {
                prefs.skin = sk.id; refreshBubble(); render()
                toast("${sk.title} ✓")
            }
        }
        page.addView(grid, Ui.lp(this, 12))

        // Categories along the bottom.
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(p.card)
            setPaddingRelative(dp(6), dp(8), dp(6), dp(12))
        }
        tabs.forEach { (key, label) ->
            val on = key == tab
            bar.addView(TextView(this).apply {
                text = label; gravity = Gravity.CENTER; textSize = 12.5f
                setTextColor(if (on) p.accentInk else p.muted)
                if (on) { typeface = android.graphics.Typeface.DEFAULT_BOLD; background = Ui.rounded(p.accentSoft, dp(14).toFloat()) }
                setPadding(0, dp(10), 0, dp(10))
                setOnClickListener { tab = key; render() }
            }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginStart = dp(3); marginEnd = dp(3) })
        }
        val outer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(p.bg) }
        outer.addView(ScrollView(this).apply { setBackgroundColor(p.bg); isFillViewport = true; addView(page) }, LinearLayout.LayoutParams(Ui.MATCH, 0, 1f))
        outer.addView(View(this).apply { setBackgroundColor(p.border) }, LinearLayout.LayoutParams(Ui.MATCH, dp(1)))
        outer.addView(bar, LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP))
        setContentView(outer)
    }

    /** Your skin as a code: copied, and offered to share (WhatsApp, Discord…). */
    private fun shareSkin(id: String) {
        val code = SkinArt.shareCode(this, id) ?: run { toast("Couldn't make a code for that skin"); return }
        getSystemService(android.content.ClipboardManager::class.java)
            .setPrimaryClip(android.content.ClipData.newPlainText("Voice Bubble skin", code))
        toast("Skin code copied")
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT,
            "My Voice Bubble skin! In Voice Bubble: Settings → Bubble skin → Mine → Paste a code\n\n$code"), "Share skin"))
    }

    /** A friend's skin code: paste it (or it's taken from what you copied). */
    private fun pasteSkinCode() {
        val f = Ui.field(this, "Paste the skin code (starts with VBSKIN:)", multiLine = true)
        Ui.dialog(this).setTitle("Paste a skin code").setView(Ui.padded(this, f)).setPositiveButton("Add") { _, _ ->
            val text = f.text.toString()
            val code = Regex("VBSKIN:[A-Za-z0-9+/=\\s]+").find(text)?.value ?: text
            val id = runCatching { SkinArt.importCode(this, code) }.getOrNull()
            if (id == null) { toast("That code doesn't work. Copy the whole thing and try again"); return@setPositiveButton }
            Prefs(this).skin = id
            refreshBubble(); render()
            toast("Added \"${Skins.byId(this, id).title}\" and put it on your bubble")
        }.setNegativeButton("Cancel", null).show()
    }

    /** Describe a skin; the AI draws it, you see it first, then keep it or try again. */
    private fun designSkin(again: String? = null) {
        if (!Ai.hasKey(this)) { toast("Making skins with AI needs an AI key (Settings → AI)"); return }
        val f = Ui.field(this, "e.g. a pizza with sunglasses, a purple galaxy cat, a red soul heart with flames").apply { again?.let { setText(it) } }
        Ui.dialog(this).setTitle("Describe your skin").setView(Ui.padded(this, f)).setPositiveButton("Make it") { _, _ ->
            val what = f.text.toString().trim()
            if (what.isNotEmpty()) makeSkin(what)
        }.setNegativeButton("Cancel", null).show()
    }

    private fun makeSkin(what: String) {
        toast("Designing your skin…")
        val app = applicationContext
        Thread {
            var spec: org.json.JSONObject? = null
            var problem: String? = null
            // Free AIs sometimes send broken or bare drawings: up to three tries for a proper one.
            for (attempt in 0 until 3) {
                try {
                    spec = SkinArt.parseSpec(Ai.designSkin(app, what), minShapes = 6)
                    if (spec != null) break
                } catch (e: Ai.Failure) { problem = e.message; break }
            }
            runOnUiThread {
                if (isFinishing) return@runOnUiThread
                val s = spec
                if (s == null) { toast(problem ?: "The AI's drawing didn't work. Try again or describe it differently"); return@runOnUiThread }
                previewSkin(what, s)
            }
        }.start()
    }

    /** Shows the AI's skin before it's kept. */
    private fun previewSkin(what: String, spec: org.json.JSONObject) {
        val name = spec.optString("name").trim().ifEmpty { "AI skin" }
        val box = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER_HORIZONTAL
            setPadding(0, dp(12), 0, 0)
            addView(BubbleView(this@SkinsActivity).apply { skin = Skins.fromSpec("preview", name, spec, true) },
                android.widget.LinearLayout.LayoutParams(dp(140), dp(140)))
            addView(Ui.text(this@SkinsActivity, name, 15f, p.ink).apply { gravity = android.view.Gravity.CENTER; setPadding(0, dp(8), 0, 0) })
        }
        Ui.dialog(this).setTitle("Your new skin").setView(box)
            .setPositiveButton("Use it") { _, _ ->
                val id = SkinArt.addDesign(this, spec)
                Prefs(this).skin = id
                refreshBubble(); render()
                toast("Made \"$name\" and put it on your bubble")
            }
            .setNeutralButton("Try again") { _, _ -> makeSkin(what) }
            .setNegativeButton("Cancel", null).show()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != PICK_SKIN || resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        val id = runCatching { SkinArt.addPicture(this, uri) }.getOrNull()
        if (id == null) { toast("Couldn't open that picture"); return }
        Prefs(this).skin = id
        refreshBubble(); render()
        toast("Your picture is your bubble now")
    }

}
