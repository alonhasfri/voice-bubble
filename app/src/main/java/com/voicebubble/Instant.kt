package com.voicebubble

import kotlin.random.Random

/**
 * Characters on the phone with no AI: instant, but simpler than the AI versions. Each one changes your words
 * the way that character talks (Spamton's [[brackets]] and CAPS, Papyrus's capitals, Sans's lowercase…).
 */
object Instant {
    /** Characters with an instant version. */
    val supported = listOf("Spamton", "Rouxls Kaard", "Papyrus", "Sans", "Temmie", "Jevil", "Lancer", "Undyne", "Flowey")

    private fun key(who: String): String? {
        val n = who.lowercase()
        return when {
            "spamton" in n -> "spamton"
            "rouxls" in n || "rules card" in n -> "rouxls"
            "papyrus" in n -> "papyrus"
            n.trim() == "sans" || n.startsWith("sans ") -> "sans"
            "temmie" in n -> "temmie"
            "jevil" in n -> "jevil"
            "lancer" in n -> "lancer"
            "undyne" in n -> "undyne"
            "flowey" in n || "flowery" in n -> "flowey"
            else -> null
        }
    }

    fun has(who: String?) = who != null && key(who) != null

    private val small = setOf("the", "a", "an", "and", "or", "but", "to", "of", "in", "on", "at", "for", "is", "it",
        "i", "you", "me", "my", "we", "be", "so", "do", "if", "up", "can", "are", "was", "this", "that", "with")

    private val ads = listOf("[[Special Deal]]", "[[Limited Time Offer]]", "[[BIG SHOT]]", "[[Hyperlink Blocked]]",
        "[[Number One Rated Salesman]]", "[[Click Here]]", "[[Free Trial]]", "[[Cheap]]")

    /** Your words as [who] would say them, or null when there's no instant version. */
    /**
     * [level]: 0 low (a light touch), 1 medium, 2 intense, 3 extreme (as much of them as possible).
     * Your words always stay; only how they're said changes.
     */
    fun apply(who: String, text: String, level: Int = 1): String? {
        val t = text.trim()
        if (t.isEmpty()) return t
        val rnd = Random(t.hashCode())   // the same message always comes out the same
        val lv = level.coerceIn(0, 3)
        val bare = t.trimEnd('.', '!')
        /** Their catchphrases around the message: none at low, more the higher it goes. */
        fun wrap(body: String, before: List<String>, after: List<String>) =
            if (lv == 0) body else (before.take(lv).joinToString(" ") + " " + body + " " + after.take(lv).joinToString(" ")).trim()
        return when (key(who)) {
            "spamton" -> spamton(t, rnd, lv)
            "rouxls" -> rouxls(t, rnd, lv)
            "papyrus" -> t.uppercase().replace(Regex("\\bI AM\\b|\\bI'M\\b"), "THE GREAT PAPYRUS IS").let { up ->
                when (lv) {
                    0 -> up
                    1 -> "$up NYEH HEH HEH!"
                    2 -> "BEHOLD! $up NYEH HEH HEH!"
                    else -> "BEHOLD, IT IS I, THE GREAT PAPYRUS!! $up NYEH HEH HEH!! NYEH!!"
                }
            }
            "sans" -> if (lv == 0) t.lowercase() else
                listOf("heh.", "heh heh.", "heh heh heh.")[lv - 1] + " " + t.lowercase().trimEnd('.', '!') + listOf(", kid.", ", kiddo.", "... kid. heh.")[lv - 1]
            "temmie" -> temmie(t, lv)
            "jevil" -> wrap(if (lv >= 1) t.uppercase() else t, listOf("UEE HEE HEE!", "CHAOS, CHAOS!", "I CAN DO ANYTHING!"),
                listOf("CHAOS, CHAOS!", "UEE HEE HEE!", "LET'S PLAY A GAME!"))
            "lancer" -> wrap(t, listOf("Ho ho ho!", "Lancer here!", "The bad guy has arrived!"), listOf("Ho ho!", "Bad guy style!", "HO HO HO!"))
            "undyne" -> wrap(if (lv >= 1) bare.uppercase() + "!!" else t, listOf("NGAHHH!!", "LISTEN UP!!", "FUHUHU!!"), listOf("PUNK!!", "GOT IT, PUNK?!", "NGAHHHHH!!!"))
            "flowey" -> flowey(t, lv)
            else -> null
        }
    }

    private fun words(t: String) = Regex("[\\p{L}']+").findAll(t).toList()

    private fun spamton(t: String, rnd: Random, lv: Int = 1): String {
        // Words become [[Ad Phrases]], some go CAPS, and a sales pitch around it (more of it, the higher the level).
        val ws = words(t).filter { it.value.length >= 4 && it.value.lowercase() !in small }
        val bracket = ws.shuffled(rnd).take(lv + 1).map { it.range.first }.toSet()
        val sb = StringBuilder()
        var last = 0
        for (m in words(t)) {
            sb.append(t, last, m.range.first)
            val w = m.value
            sb.append(when {
                m.range.first in bracket -> "[[" + w.replaceFirstChar { it.uppercaseChar() } + "]]"
                lv == 3 -> w.uppercase()
                lv == 2 && rnd.nextInt(2) == 0 -> w.uppercase()
                lv == 1 && rnd.nextInt(3) == 0 -> w.uppercase()
                else -> w
            })
            last = m.range.last + 1
        }
        sb.append(t.substring(last))
        val body = sb.toString().trimEnd('.', '!')
        if (lv == 0) return "$body!!"
        val pitch = ads.shuffled(rnd).take(lv).joinToString(" ")
        return when (lv) {
            1 -> "HEY   EVERY  BUDDY!! $body!! $pitch"
            2 -> "HEY   EVERY  BUDDY!! IT'S ME, SPAMTON G. SPAMTON!! $body!! $pitch"
            else -> "HEY      EVERY    BUDDY!!! IT'S ME, SPAMTON G. SPAMTON!! $body!!! NOW'S YOUR CHANCE TO BE A $pitch \$\$\$"
        }
    }

    private fun rouxls(t: String, rnd: Random, lv: Int = 1): String {
        // "you" → thou/thee/thy, "-eth" stuck on some words, Random Capitals, and "Worm".
        var s = t.replace(Regex("(?i)\\byour\\b"), "thy").replace(Regex("(?i)\\byou're\\b"), "thou art")
            .replace(Regex("(?i)\\byou\\b"), "thou")
        val sb = StringBuilder()
        var last = 0
        var n = 0
        for (m in words(s)) {
            sb.append(s, last, m.range.first)
            var w = m.value
            if (w.length >= 4 && w.lowercase() !in small && w.all { it.isLetter() }) {
                n++
                // How often "-eth" goes on: never at low, every third word, every other, every word.
                if (lv > 0 && (n - 1) % listOf(1, 3, 2, 1)[lv] == 0) w = w + "eth"
                if (lv == 3 || (lv > 0 && rnd.nextInt(2) == 0)) w = w.replaceFirstChar { it.uppercaseChar() }
            }
            sb.append(w)
            last = m.range.last + 1
        }
        sb.append(s.substring(last))
        s = sb.toString().trimEnd('.', '!')
        return when (lv) {
            0 -> "$s, Worm."
            1 -> "Hark, Worm!! $s!!"
            2 -> "Hark, Worm!! 'Tis I, the Duke of Puzzles!! $s!!"
            else -> "HARK, Worm!! Behold-eth, 'Tis I, Rouxls Kaard, Duke of Puzzles!! $s!! Ehe-heh-heh!!"
        }
    }

    private fun temmie(t: String, lv: Int = 1): String {
        var s = t.replace(Regex("(?i)\\bI'm\\b|\\bI am\\b"), "tem is").replace(Regex("\\bI\\b"), "tem")
            .replace(Regex("(?i)\\bmy\\b"), "tem's")
        // Every third longer word gets its second half in capitals ("coLLEGE").
        var n = 0
        s = Regex("[\\p{L}]{5,}").replace(s) { m ->
            n++
            if (lv > 0 && (n - 1) % listOf(1, 3, 2, 1)[lv] == 0) m.value.substring(0, m.value.length / 2) + m.value.substring(m.value.length / 2).uppercase() else m.value
        }
        return when (lv) {
            0 -> s
            1 -> "hOI!! " + s.trimEnd('.', '!') + "!!"
            2 -> "hOI!! i'm tEMMIE!! " + s.trimEnd('.', '!') + "!!"
            else -> "hOI!!! i'm tEMMIE!!! " + s.trimEnd('.', '!') + "!!! tem go to coLLEGE!!!"
        }
    }

    private fun flowey(t: String, lv: Int = 1): String {
        // One word STRESSED, a friendly start and a creepy giggle.
        val ws = words(t).filter { it.value.length >= 4 && it.value.lowercase() !in small }
        var s = t
        // Stressed words: the longest one, or more of them the higher the level.
        ws.sortedByDescending { it.value.length }.take(maxOf(1, lv)).sortedByDescending { it.range.first }
            .forEach { m -> s = s.replaceRange(m.range, m.value.uppercase()) }
        return when (lv) {
            0 -> s
            1 -> "Howdy! " + s.trimEnd('.') + ". Hee hee hee."
            2 -> "Howdy! Golly, " + s.trimEnd('.').replaceFirstChar { it.lowercaseChar() } + ". Hee hee hee."
            else -> "Howdy, pal! Golly gee... " + s.trimEnd('.').replaceFirstChar { it.lowercaseChar() } + ". Hee hee hee. HEE HEE HEE."
        }
    }
}
