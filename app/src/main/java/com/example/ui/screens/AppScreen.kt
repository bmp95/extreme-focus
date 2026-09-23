package com.example.ui.screens

sealed class AppScreen {
    data object Dashboard : AppScreen()
    data object AppSelector : AppScreen()
    data class BlockScreen(val packageName: String, val appName: String, val minutesSpent: Int) : AppScreen()
    data class FrictionChallenge(val packageName: String, val appName: String, val challengeType: ChallengeType) : AppScreen()
    data class MonotonyTask(val packageName: String, val appName: String) : AppScreen()
    data object AuditLogs : AppScreen()
    data object PermissionsGuide : AppScreen()
}

enum class ChallengeType {
    MANIFESTO_TRANSCRIPTION, // Transcription without paste or backspace errors
    ABYSS_TOUCH,             // Hold touch button for 60 seconds without releasing
    MONOTONY_GRID,           // Monotonous sequence finder
    MONOTONY_TASK            // Manual 50 items input or tedious logic puzzle for 15-minute whitelist
}
