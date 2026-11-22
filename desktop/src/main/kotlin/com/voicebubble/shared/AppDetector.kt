package com.voicebubble

import android.content.Context
import com.voicebubble.desktop.Win

/**
 * Which program you're typing in (Windows), named like the phone apps so per-app styles, limits and
 * Roblox-safe words work the same: RobloxPlayerBeta.exe → com.roblox.client, Discord.exe → com.discord …
 */
object AppDetector {
    private val names = mapOf(
        "robloxplayerbeta" to Limit.ROBLOX, "windows10universal" to Limit.ROBLOX,
        "discord" to "com.discord", "whatsapp" to "com.whatsapp", "telegram" to "org.telegram.messenger",
        "signal" to "org.thoughtcrime.securesms", "slack" to "com.Slack", "ms-teams" to "com.microsoft.teams",
        "teams" to "com.microsoft.teams", "zoom" to "us.zoom.videomeetings", "outlook" to "com.microsoft.office.outlook",
        "olk" to "com.microsoft.office.outlook", "thunderbird" to "net.thunderbird.android",
        "messenger" to "com.facebook.orca", "viber" to "com.viber.voip",
    )

    fun current(c: Context): String? {
        val exe = Win.targetExe() ?: return null
        names[exe]?.let { return it }
        // In a browser: the tab's title tells the site.
        val title = Win.targetTitle()?.lowercase() ?: ""
        return when {
            "gmail" in title -> "com.google.android.gm"
            "outlook" in title -> "com.microsoft.office.outlook"
            "whatsapp" in title -> "com.whatsapp"
            "discord" in title -> "com.discord"
            "roblox" in title && exe != "robloxstudiobeta" -> Limit.ROBLOX
            "slack" in title -> "com.Slack"
            else -> "win.$exe"
        }
    }
}
