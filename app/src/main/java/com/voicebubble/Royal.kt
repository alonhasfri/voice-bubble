package com.voicebubble

/**
 * Royal English: you talk like a sophisticated English royal of the 1500s
 * (the royal "We", thou/thee/thy, "Verily…"). [apply] is the instant version, on the phone with
 * no AI; [AI] is what the AI is told to do.
 */
object Royal {
    /** How the mode is named in the app ("Talking like a 1500s royal"). */
    const val NAME = "a 1500s royal"

    /** The instruction for the AI version. (No length words in it: those come from the length setting.) */
    const val AI = "Make me sound like a grand English monarch of the 1500s, fully and richly: every sentence in " +
        "ornate Early Modern English, never a plain modern sentence. The royal 'We', 'Us' and 'Our' for myself; " +
        "'thou', 'thee', 'thy' and 'thine' for the listener, with the old verb forms (thou art, thou hast, thou dost, " +
        "thou wilt, thou canst, thou knowest, thou wouldst; he hath, she doth, it pleaseth, he saith, she cometh). " +
        "Old words throughout: methinks, prithee, forsooth, verily, 'tis, 'twas, anon, ere, hither, thither, whence, " +
        "wherefore, aught, naught, mayhap, perchance, betwixt, nay, aye, hark. Stately word order (\"Much doth it " +
        "please Us\", \"Fain would We know\"), a royal opening and, where it fits, a closing flourish such as " +
        "\"So We decree.\" or \"Let it be known.\" Majestic, proud and gracious, not quotes from plays. " +
        "Don't call anyone a peasant or a subject, or talk down to them. Keep what I meant and keep any names."

    // Order matters: longer phrases first, "I"/"me"/"my" last.
    private val swaps: List<Pair<String, String>> = listOf(
        // The royal "We".
        "I am" to "We are", "I'm" to "We are", "am I" to "are We", "I was" to "We were", "was I" to "were We",
        "I've" to "We have", "I'll" to "We shall", "I will" to "We shall", "I'd" to "We would",
        "I want" to "We desire", "I need" to "We require", "I think" to "Methinks", "I know" to "We know full well",
        "you know" to "thou knowest", "do you know" to "knowest thou", "I hope" to "We fain would hope",
        "I guess" to "We suppose", "I love" to "We do dearly love",
        "give me" to "bring unto Us", "tell me" to "inform Us", "let me know" to "inform Us", "let me" to "permit Us to",
        "my house" to "Our castle", "your house" to "thy dwelling", "my friend" to "Our dear friend",
        // To the listener.
        "are you" to "art thou", "you are" to "thou art", "you're" to "thou art", "you were" to "thou wert",
        "do you" to "dost thou", "did you" to "didst thou", "have you" to "hast thou", "you have" to "thou hast",
        "can you" to "canst thou", "will you" to "wilt thou", "would you" to "wouldst thou", "you will" to "thou wilt",
        "it is" to "'tis", "it's" to "'tis", "it was" to "'twas", "don't" to "do not", "doesn't" to "doth not",
        "can't" to "cannot", "won't" to "shall not", "isn't" to "is not", "aren't" to "art not",
        "thank you" to "We thank thee", "thanks" to "We thank thee", "goodbye" to "fare thee well",
        "good morning" to "good morrow", "right now" to "at once", "of course" to "naturally, as We decree",
        "shut up" to "hold thy tongue", "come here" to "approach Us", "go away" to "begone",
        "your" to "thy", "yours" to "thine", "yourself" to "thyself", "you" to "thee",
        "does" to "doth", "has" to "hath", "yes" to "aye", "yeah" to "aye", "hello" to "good morrow",
        "hi" to "good morrow", "hey" to "hark", "bye" to "fare thee well", "please" to "pray",
        "sorry" to "pardon Us", "maybe" to "mayhap", "perhaps" to "mayhap", "really" to "verily", "truly" to "verily",
        "very" to "most", "before" to "ere", "often" to "oft", "why" to "wherefore", "nothing" to "naught",
        "anything" to "aught", "okay" to "very well", "ok" to "very well", "lol" to "how droll", "lmao" to "how droll",
        "want" to "desire", "wants" to "desires", "need" to "require", "needs" to "requires", "think" to "deem",
        "help" to "aid", "buy" to "purchase", "hate" to "abhor", "kid" to "young one", "kids" to "young ones",
        "everyone" to "all assembled", "everybody" to "all assembled", "guys" to "good people",
        "guy" to "good sir", "dude" to "good sir", "bro" to "good sir", "boss" to "liege",
        "friend" to "companion", "friends" to "companions", "girl" to "maiden", "girls" to "maidens",
        "mom" to "Our royal mother", "dad" to "Our royal father", "food" to "victuals", "money" to "gold",
        "car" to "carriage", "phone" to "talking-glass", "cool" to "most splendid", "awesome" to "most splendid",
        "amazing" to "most splendid", "bad" to "most foul", "stupid" to "witless", "idiot" to "knave",
        "soon" to "anon", "listen" to "hark", "kill" to "slay", "fight" to "duel",
        "says" to "saith", "goes" to "goeth", "comes" to "cometh", "makes" to "maketh", "takes" to "taketh",
        "knows" to "knoweth", "seems" to "seemeth", "looks" to "looketh", "gives" to "giveth", "tells" to "telleth",
        "between" to "betwixt", "today" to "this day", "tonight" to "this night",
        "tomorrow" to "on the morrow", "now" to "anon", "never" to "ne'er", "over" to "o'er", "even" to "e'en",
        "something" to "somewhat", "whatever" to "whatsoever", "let's" to "let us", "lots of" to "a great many",
        "myself" to "Ourselves", "my" to "Our", "mine" to "Ours", "me" to "Us", "I" to "We",
    )
    private val rules = swaps.map { (from, to) ->
        Regex("(?<![\\p{L}'])" + from.split(" ").joinToString("\\s+") { Regex.escape(it) } + "(?![\\p{L}'])", RegexOption.IGNORE_CASE) to to
    }
    private val opener = Regex("^(?i)(hark|pray|nay|aye|good morrow|fare thee well|we thank thee|hear ye|forsooth|verily|hold thy tongue|begone|approach)\\b")

    /** [level]: 0 low (only the old words), 1 medium, 2 intense, 3 extreme. */
    fun apply(text: String, level: Int = 1): String {
        val lv = level.coerceIn(0, 3)
        var t = text.trim()
        if (t.isEmpty()) return t
        for ((re, to) in rules) {
            t = re.replace(t) { m ->
                // Keep a capital letter where there was one.
                if (m.value[0].isUpperCase()) to.replaceFirstChar { it.uppercaseChar() } else to
            }
        }
        // "No" as an answer (at the start of a sentence) is "Nay"; "no gold" stays.
        t = Regex("(^|[.!?]\\s+)[Nn]o\\b(?=[,.!?]|\\s*$)").replace(t) { it.groupValues[1] + "Nay" }
        // "you" as the one doing something is "thou": at the start of a sentence, "thee" becomes "thou".
        t = Regex("(^|[.!?]\\s+)Thee\\b").replace(t) { it.groupValues[1] + "Thou" }
        // "thou" takes the old verb ending: "thou want" → "thou wantest".
        t = Regex("(?i)\\b(thou)\\s+(want|need|desire|require|know|think|like|love|see|say|go|come|make|take|get|give|feel|play|run|tell|call|hope|wish|mean|bring|send|speak)\\b")
            .replace(t) { m -> m.groupValues[1] + " " + m.groupValues[2] + if (m.groupValues[2].endsWith("e")) "st" else "est" }
        if (lv == 0) return t
        // Intense and up: every later sentence starts royally too.
        if (lv >= 2) {
            var k = 0
            t = Regex("([.!])\\s+(\\p{Lu})").replace(t) { m ->
                k++
                m.groupValues[1] + " " + listOf("Forsooth, ", "Verily, ", "Mark Us, ")[k % 3] + m.groupValues[2].lowercase()
            }
        }
        // A royal opening (not when it already starts royally), the same one for the same words.
        val pick = (t.hashCode() and 0x7fffffff)
        if (!opener.containsMatchIn(t)) {
            val end = t.indexOfFirst { it in ".!?" }
            val keepCap = Regex("^(We|Us|Our|Ours|I)\\b").containsMatchIn(t)
            val rest = if (keepCap) t else t.replaceFirstChar { it.lowercaseChar() }
            val open = listOf("Verily, ", "Hark! ", "Hear Us, ", "Forsooth, ")[pick % 4]
            t = if (end >= 0 && t[end] == '?') "Pray tell, $rest"
            else open + if (open.endsWith("! ")) rest.replaceFirstChar { it.uppercaseChar() } else rest
        }
        // And a royal closing, when it's a statement.
        if (!t.contains('?')) {
            t = t.trimEnd().let { if (it.last() in ".!") it else "$it." } +
                if (lv == 3) " So We decree, and so shall it be, forevermore!"
                else listOf(" So We decree.", " Let it be known.", " Thus speaketh the Crown.", " So be it.")[(pick / 4) % 4]
        }
        if (lv == 3) t = "Hear ye, hear ye! $t"
        return t
    }

    /** Asked for this style ("make me sound like a royal", the old "Shakespeare", "medieval English"…). */
    fun wanted(instructionOrPersona: String?) = instructionOrPersona != null &&
        Regex("(?i)shakespear|royal|medieval|elizabethan|old english|1500|fifteen hundred|monarch").containsMatchIn(instructionOrPersona)

    /** What the AI is told for "make me sound like …". */
    fun instruction(who: String, c: android.content.Context? = null) =
        if (wanted(who) && !Characters.isMix(who)) AI else Characters.instruction(who, c)
}
