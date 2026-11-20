package com.voicebubble

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.voicebubble.Commands.Feature

/**
 * Swipe down on the bubble: everything you can switch or choose, in four tabs, right there
 * (no app, no voice command): Talk (character, length, translation), Speech (engine, model),
 * Writing (features and word packs) and Bubble (skin, sounds…).
 */
class QuickMenu(
    private val c: Context,
    private val maxHeight: Int,
    /** Something changed: [msg] is a short note to show (or null). */
    private val changed: (msg: String?) -> Unit,
    private val close: () -> Unit,
) {
    companion object { var lastTab = 0 }

    private val prefs = Prefs(c)
    private val d = c.resources.displayMetrics.density
    private fun dp(v: Int) = (v * d).toInt()
    private val purple = Color.rgb(156, 92, 255)
    private val chipOff = Color.rgb(44, 52, 82)
    private val muted = Color.rgb(169, 187, 255)
    private val body = LinearLayout(c).apply { orientation = LinearLayout.VERTICAL }
    private val tabsRow = LinearLayout(c).apply { orientation = LinearLayout.HORIZONTAL }
    private val tabs = listOf("Talk", "Speech", "Writing", "Bubble")

    fun view(): View {
        val panel = LinearLayout(c).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), dp(10), dp(10), dp(10))
            background = GradientDrawable().apply { cornerRadius = dp(20).toFloat(); setColor(Color.argb(248, 22, 26, 44)) }
        }
        val head = LinearLayout(c).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        head.addView(TextView(c).apply {
            text = "Quick menu"; setTextColor(Color.WHITE); textSize = 15f; typeface = Typeface.DEFAULT_BOLD
            setPadding(dp(6), 0, 0, 0)
        }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        head.addView(TextView(c).apply {
            text = "✕"; setTextColor(Color.WHITE); textSize = 18f; setPadding(dp(14), dp(2), dp(8), dp(2))
            setOnClickListener { close() }
        })
        panel.addView(head)
        panel.addView(tabsRow, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(8) })
        panel.addView(ScrollView(c).apply { addView(body) }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, maxHeight))
        render()
        return panel
    }

    private fun render() {
        tabsRow.removeAllViews()
        tabs.forEachIndexed { i, t ->
            val on = i == lastTab
            tabsRow.addView(TextView(c).apply {
                text = t; gravity = Gravity.CENTER; textSize = 13f; typeface = Typeface.DEFAULT_BOLD
                setTextColor(if (on) Color.WHITE else muted)
                setPadding(0, dp(9), 0, dp(9))
                background = GradientDrawable().apply { cornerRadius = dp(12).toFloat(); setColor(if (on) purple else chipOff) }
                setOnClickListener { lastTab = i; render() }
            }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { if (i > 0) marginStart = dp(5) })
        }
        body.removeAllViews()
        when (lastTab) {
            0 -> talk()
            1 -> speech()
            2 -> writing()
            else -> bubble()
        }
    }

    // ---------------- building blocks ----------------

    private fun title(t: String) = body.addView(TextView(c).apply {
        text = t.uppercase(); setTextColor(muted); textSize = 11.5f; typeface = Typeface.DEFAULT_BOLD; letterSpacing = 0.05f
        setPadding(dp(6), dp(14), 0, dp(4))
    })

    /** Chips, three to a row; the chosen one is purple. */
    private fun <T> choices(items: List<Pair<T, String>>, chosen: T, pick: (T) -> Unit) {
        items.chunked(3).forEach { row ->
            val line = LinearLayout(c).apply { orientation = LinearLayout.HORIZONTAL }
            row.forEachIndexed { i, (value, label) ->
                val on = value == chosen
                line.addView(TextView(c).apply {
                    text = label; gravity = Gravity.CENTER; textSize = 13f; maxLines = 2
                    setTextColor(Color.WHITE)
                    if (on) typeface = Typeface.DEFAULT_BOLD
                    setPadding(dp(6), dp(9), dp(6), dp(9))
                    background = GradientDrawable().apply { cornerRadius = dp(12).toFloat(); setColor(if (on) purple else chipOff) }
                    setOnClickListener { pick(value); render() }
                }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { if (i > 0) marginStart = dp(5) })
            }
            repeat(3 - row.size) { line.addView(View(c), LinearLayout.LayoutParams(0, 1, 1f)) }
            body.addView(line, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(5) })
        }
    }

    /** An on/off row. */
    private fun toggle(label: String, on: Boolean, set: (Boolean) -> Unit) {
        val row = LinearLayout(c).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(10), dp(10), dp(10))
            background = GradientDrawable().apply { cornerRadius = dp(12).toFloat(); setColor(chipOff) }
            setOnClickListener { set(!on); changed("$label ${if (!on) "on" else "off"}"); render() }
        }
        row.addView(TextView(c).apply { text = label; setTextColor(Color.WHITE); textSize = 14f },
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        row.addView(TextView(c).apply {
            text = if (on) "ON" else "OFF"; textSize = 12f; typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER
            setTextColor(if (on) Color.WHITE else muted)
            setPadding(dp(12), dp(4), dp(12), dp(4))
            background = GradientDrawable().apply { cornerRadius = dp(10).toFloat(); setColor(if (on) purple else Color.rgb(30, 36, 60)) }
        })
        body.addView(row, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(5) })
    }

    private fun feature(f: Feature, label: String = f.title) = toggle(label, Features.isOn(c, f)) { Features.set(c, f, it) }

    // ---------------- tabs ----------------

    private fun talk() {
        title("Talk like")
        val mine = Characters.custom(c).keys.sorted()
        val recent = prefs.recentPersonas
        val all = (recent + mine + Characters.presets).distinct()
        choices(listOf<Pair<String?, String>>(null to "Normal") + all.map { it to (if (it in mine) "★ $it" else it) }, prefs.persona) {
            prefs.persona = it
            changed(if (it == null) "Normal voice" else "Talking like $it")
        }
        title("AI text length")
        choices(Ai.lengths.map { it.first to it.second.replace(" (AI decides)", "") }, prefs.aiLength) {
            prefs.aiLength = it; changed("Length: ${Ai.lengthName(it)}")
        }
        title("Translate into")
        val langs = listOf(null, "English", "Hebrew", "Spanish", "French", "German", "Arabic", "Russian", "Portuguese", "Italian", "Japanese")
        choices(langs.map { it to (it ?: "Off") }, prefs.translateTo) {
            prefs.translateTo = it; changed(if (it == null) "Translation off" else "Translating into $it")
        }
        title("Options")
        toggle("Instant characters (no AI)", prefs.instantChars) { prefs.instantChars = it }
        toggle("Instant Royal English (no AI)", prefs.instantRoyal) { prefs.instantRoyal = it }
        toggle("AI fixes mistakes", prefs.aiFix) { prefs.aiFix = it }
    }

    private fun speech() {
        val whisper = Models.shown().filter { Models.isDownloaded(c, it) }
        val live = Live.models.filter { Live.isDownloaded(c, it) }
        if (live.isNotEmpty()) {
            title("Engine")
            choices(listOf("whisper" to "Whisper", "live" to "Live (types as you talk)"), prefs.engine) {
                prefs.engine = it; changed(if (it == "live") "Live engine" else "Whisper")
            }
        }
        title("Whisper model")
        if (whisper.isEmpty()) body.addView(TextView(c).apply { text = "None downloaded yet (open the app → Speech model)"; setTextColor(muted); setPadding(dp(6), 0, 0, 0) })
        else choices(whisper.map { it.id to it.title.substringBefore(" (") }, prefs.modelId) {
            prefs.modelId = it; changed("Model: ${Models.byId(it).title.substringBefore(" (")}")
        }
        if (live.isNotEmpty()) {
            title("Live model")
            choices(live.map { it.id to it.title }, prefs.liveModelId) { prefs.liveModelId = it; prefs.engine = "live"; changed("Live: ${Live.byId(it).title}") }
        }
        if (!Models.byId(prefs.modelId).englishOnly) {
            title("Language you speak")
            choices(Langs.all.take(9), prefs.lang) { prefs.lang = it; changed(null) }
        }
        title("Options")
        if (live.isNotEmpty()) {
            toggle("Whisper checks the final text", prefs.liveFinalWhisper) { prefs.liveFinalWhisper = it }
            toggle("Type while I talk (Live)", prefs.typeWhileTalking) { prefs.typeWhileTalking = it }
        }
        feature(Feature.EXTRA, "Extra accuracy (slower)")
    }

    private fun writing() {
        title("Writing helpers")
        feature(Feature.COMMANDS, "Voice commands (new line, comma…)")
        feature(Feature.EMOJI)
        feature(Feature.FILLER)
        feature(Feature.NUMBERS)
        feature(Feature.CAPS, "Caps lock")
        title("Word packs")
        feature(Feature.PACK_JJK)
        feature(Feature.PACK_TOBY)
        feature(Feature.PACK_GENZ)
        title("Chat")
        toggle("Roblox-safe words", prefs.robloxSafe) { prefs.robloxSafe = it }
        title("Length limit (all apps)")
        choices(listOf(0 to "Off", 100 to "100", 150 to "150", 200 to "200", 280 to "280", 500 to "500"), prefs.maxChars) {
            prefs.maxChars = it; changed(if (it == 0) "No length limit" else "Longest message: $it characters")
        }
    }

    private fun bubble() {
        title("Skin")
        choices(Skins.all(c).map { it.id to it.title }, Skins.byId(c, prefs.skin).id) { prefs.skin = it; changed("Skin: ${Skins.byId(c, it).title}") }
        title("Options")
        feature(Feature.SOUNDS)
        feature(Feature.VIBRATION)
        toggle("Live text next to the bubble", prefs.liveText) { prefs.liveText = it }
        toggle("Animated skins", prefs.animSkins) { prefs.animSkins = it }
        feature(Feature.ONLY_TYPING, "Only while typing")
        toggle("Sit right above the keyboard", prefs.dockAboveKeyboard) { prefs.dockAboveKeyboard = it; prefs.dockLift = 0 }
    }
}
