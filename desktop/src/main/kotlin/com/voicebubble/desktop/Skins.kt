package com.voicebubble.desktop

import com.voicebubble.SkinArt
import org.json.JSONArray
import org.json.JSONObject

/** The skins you can pick on Windows: plain colours, every drawn skin from the phone, and your AI-made ones. */
object DeskSkins {
    class Skin(val id: String, val title: String, val category: String, val spec: JSONObject?, val theme: JSONObject? = null)

    private fun plain(id: String, title: String, c1: String, c2: String? = null) =
        Skin(id, title, "basic", JSONObject().put("background", JSONArray().put(c1).apply { c2?.let { put(it) } }).put("mic", "#FFFFFF"))

    private val basics = listOf(
        plain("classic", "Classic blue", "#3A5BD9", "#5B7CF2"), plain("midnight", "Midnight", "#121633", "#2A2F66"),
        plain("sunset", "Sunset", "#FF7A45", "#E0457B"), plain("mint", "Mint", "#1FB58F", "#7BE0C3"),
        plain("lava", "Lava", "#E53935", "#FF8F00"), plain("bubblegum", "Bubblegum", "#FF6FB5", "#B56BFF"),
        plain("gold", "Gold", "#C99A2E", "#FFE08A"), plain("ocean", "Ocean", "#0277BD", "#26C6DA"),
        plain("matrix", "Matrix", "#06140A", "#0E3B1A"), plain("ghost", "Ghost", "#ECEFF1", "#B0BEC5"),
    )

    private var cache: List<Skin>? = null
    private var cacheKey = 0
    fun all(): List<Skin> {
        val key = SkinArt.list(App).size
        cache?.takeIf { cacheKey == key }?.let { return it }
        return load().also { cache = it; cacheKey = key }
    }
    private fun load(): List<Skin> = basics +
        SkinArt.builtin(App).mapNotNull { cs -> cs.spec?.let { Skin(cs.id, cs.title, cs.category, it, cs.theme) } } +
        SkinArt.list(App).filter { it.spec != null }.map { Skin(it.id, it.title, "mine", it.spec) }

    fun byId(id: String?): Skin = all().firstOrNull { it.id == id } ?: basics[0]

    /** Whether the mic is drawn on top (plain skins, skins that ask for one, or "Mic on every skin"). */
    fun showsMic(s: Skin) = s.category == "basic" || s.spec?.opt("mic") is String || App.prefs.micOnAll

    /**
     * Draws a skin like the phone: "Detailed" = the full drawing; "Simple" = the character's theme (or the drawing's
     * big shapes), with the mic on top when wanted.
     */
    fun paint(g: java.awt.Graphics2D, s: Skin, px: Int) {
        val spec = when {
            s.spec == null -> null
            App.prefs.skinDetail || s.category == "basic" -> s.spec
            s.theme != null -> s.theme
            else -> SkinArt.simple(s.spec)
        }
        SkinPainter.paint(g, spec, px)
        if (showsMic(s)) SkinPainter.mic(g, px, SkinPainter.color(s.spec?.opt("mic"), java.awt.Color.WHITE))
    }
}
