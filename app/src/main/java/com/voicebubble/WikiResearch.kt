package com.voicebubble

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Looks a character or thing up on the fan wikis (Deltarune, Undertale, Jujutsu Kaisen, The Binding of Isaac) and
 * returns what its "Appearance" section says, so the AI skin maker draws it from a real description.
 * Free, no key needed. Blocking: run it off the main thread.
 */
object WikiResearch {
    private class Wiki(val name: String, val api: String, val words: List<String>)

    private val wikis = listOf(
        Wiki("Deltarune", "https://deltarune.wiki/api.php", listOf("deltarune", "kris", "susie", "ralsei", "lancer", "jevil", "spamton", "rouxls", "queen", "tenna", "noelle", "berdly")),
        Wiki("Undertale", "https://undertale.wiki/api.php", listOf("undertale", "sans", "papyrus", "toriel", "flowey", "undyne", "alphys", "mettaton", "asgore", "temmie", "napstablook", "asriel", "chara", "frisk")),
        Wiki("Jujutsu Kaisen", "https://jujutsu-kaisen.fandom.com/api.php", listOf("jjk", "jujutsu", "gojo", "sukuna", "yuji", "itadori", "megumi", "nobara", "nanami", "toji", "geto", "mahito", "todo", "maki", "yuta")),
        Wiki("The Binding of Isaac", "https://bindingofisaacrebirth.wiki.gg/api.php", listOf("isaac", "binding", "azazel", "judas", "monstro", "lilith", "cain", "magdalene", "eden", "lazarus", "apollyon")),
    )

    private val filler = setOf("a", "an", "the", "skin", "bubble", "with", "and", "of", "in", "on", "from", "style", "like", "cute",
        "theme", "themed", "character", "make", "me", "my", "his", "her", "their", "face", "icon", "deltarune", "undertale", "jjk",
        "jujutsu", "kaisen", "binding", "game")

    private fun get(url: String): JSONObject? = runCatching {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 7000; conn.readTimeout = 9000
        conn.setRequestProperty("User-Agent", "VoiceBubble/1.0 (Android app)")
        if (conn.responseCode !in 200..299) return null
        JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
    }.getOrNull()

    private fun q(s: String) = URLEncoder.encode(s, "UTF-8")

    /** Wiki markup to plain text. */
    fun clean(wikitext: String): String = wikitext
        .replace(Regex("\\[\\[(?:File|Image):[^\\]]*\\]\\]"), "")
        .replace(Regex("\\{\\{[^{}]*\\}\\}"), "")
        .replace(Regex("(?s)<ref[^>]*>.*?</ref>|<ref[^>]*/>"), "")
        .replace(Regex("\\[\\[(?:[^|\\]]*\\|)?([^\\]]*)\\]\\]"), "$1")
        .replace(Regex("'{2,}"), "")
        .replace(Regex("<[^>]+>"), "")
        .replace(Regex("=+[^=\\n]+=+"), "")
        .replace(Regex("\\s+"), " ")
        .trim()

    /** "From the Deltarune wiki, about Jevil: Jevil is a short, pudgy imp…", or null when nothing fits. */
    fun lookup(description: String): String? {
        val words = description.lowercase().split(Regex("[^a-z']+")).filter { it.length >= 3 && it !in filler }
        if (words.isEmpty()) return null
        // The wikis your words point to first (a name or the game's name); otherwise all of them.
        val lower = description.lowercase()
        val order = wikis.sortedByDescending { w -> w.words.count { it in lower } }
        for (w in order.take(if (order.first().words.any { it in lower }) 2 else 4)) {
            val search = get("${w.api}?action=query&list=search&srlimit=1&format=json&srsearch=${q(words.joinToString(" "))}") ?: continue
            val title = search.optJSONObject("query")?.optJSONArray("search")?.optJSONObject(0)?.optString("title") ?: continue
            // Only a page that's really what you asked for (its name is in your description).
            val titleWords = title.lowercase().split(Regex("[^a-z']+")).filter { it.length >= 3 }
            if (titleWords.none { it in words }) continue
            val sections = get("${w.api}?action=parse&prop=sections&redirects=1&format=json&page=${q(title)}")
                ?.optJSONObject("parse")?.optJSONArray("sections")
            var idx = "0"
            if (sections != null) for (i in 0 until sections.length()) {
                val s = sections.getJSONObject(i)
                if (s.optString("line").contains("ppearance")) { idx = s.optString("index"); break }
            }
            val text = get("${w.api}?action=parse&prop=wikitext&redirects=1&format=json&section=$idx&page=${q(title)}")
                ?.optJSONObject("parse")?.optJSONObject("wikitext")?.optString("*") ?: continue
            val plain = clean(text).take(900)
            if (plain.length > 60) return "From the ${w.name} wiki, about $title: $plain"
        }
        return null
    }
}
