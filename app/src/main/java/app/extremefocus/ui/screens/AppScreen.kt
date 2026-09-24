package app.extremefocus.ui.screens

sealed class AppScreen {
    data object Dashboard : AppScreen()
    data object AppSelector : AppScreen()
    data class BlockScreen(val packageName: String, val appName: String, val minutesSpent: Int) : AppScreen()
    data class FrictionChallenge(val packageName: String, val appName: String, val challengeType: ChallengeType) : AppScreen()
    data object AuditLogs : AppScreen()
    data object PermissionsGuide : AppScreen()
}

/** Every challenge grants the same short window: three minutes, never more. */
enum class ChallengeType {
    MANIFESTO_TRANSCRIPTION, // Transcription without paste or backspace errors
    ABYSS_TOUCH,             // Hold touch button for 60 seconds without releasing
    MONOTONY_GRID,           // Monotonous sequence finder
    DECLARE_INTENT           // State what you came to do; it is quoted back when time runs out
}
