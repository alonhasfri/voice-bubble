package com.voicebubble

/** Whisper writes things like "[BLANK_AUDIO]" or "(music)" for non-speech; drop those. */
fun clean(s: String): String =
    collapseRepeats(s.replace(Regex("\\[[^\\]]*\\]|\\([^)]*(music|applause|silence|noise|laughs?)[^)]*\\)|♪+", RegexOption.IGNORE_CASE), " ")
        .replace(Regex("\\s+"), " ")
        .trim())

/**
 * Whisper sometimes gets stuck and writes the same word or phrase over and over ("and san and san and san").
 * Three or more in a row become one.
 */
fun collapseRepeats(s: String): String =
    Regex("(?i)\\b((?:[\\p{L}\\p{N}']+[\\s,]+){0,3}[\\p{L}\\p{N}']+)(?:[\\s,]+\\1\\b){2,}").replace(s) { it.groupValues[1] }
