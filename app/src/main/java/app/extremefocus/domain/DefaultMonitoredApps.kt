package app.extremefocus.domain

import app.extremefocus.data.local.MonitoredAppEntity

/**
 * The apps offered on first launch. Only those actually installed are seeded, and always
 * with zero usage: showing minutes the user has not spent would misrepresent their day.
 */
object DefaultMonitoredApps {

    data class Suggestion(
        val packageName: String,
        val appName: String,
        val dailyLimitMinutes: Int,
        val iconCategory: String
    )

    val suggestions: List<Suggestion> = listOf(
        Suggestion("com.google.android.youtube", "YouTube", 30, "VIDEO"),
        Suggestion("com.instagram.android", "Instagram", 15, "SOCIAL"),
        Suggestion("com.zhiliaoapp.musically", "TikTok", 10, "VIDEO"),
        Suggestion("com.twitter.android", "X (Twitter)", 15, "SOCIAL"),
        Suggestion("com.reddit.frontpage", "Reddit", 20, "SOCIAL"),
        Suggestion("tv.twitch.android.app", "Twitch", 25, "VIDEO"),
        Suggestion("com.supercell.brawlstars", "Brawl Stars", 15, "GAME"),
        Suggestion("com.supercell.clashroyale", "Clash Royale", 15, "GAME"),
        Suggestion("com.roblox.client", "Roblox", 20, "GAME"),
        Suggestion("com.facebook.katana", "Facebook", 15, "SOCIAL")
    )

    fun seedFor(installedPackages: Set<String>): List<MonitoredAppEntity> =
        suggestions
            .filter { it.packageName in installedPackages }
            .map {
                MonitoredAppEntity(
                    packageName = it.packageName,
                    appName = it.appName,
                    dailyLimitMinutes = it.dailyLimitMinutes,
                    isBlocked = true,
                    currentUsageMinutes = 0,
                    iconCategory = it.iconCategory,
                    blockNotifications = true
                )
            }
}
