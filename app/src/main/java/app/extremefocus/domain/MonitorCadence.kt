package app.extremefocus.domain

/**
 * How often the monitor re-checks usage. Tight polling only pays off while a watched app is
 * actually on screen; with the screen off there is nothing to intercept at all.
 */
object MonitorCadence {

    const val WATCHING_MS = 3_000L
    const val IDLE_MS = 15_000L
    const val SCREEN_OFF_MS = 60_000L

    fun intervalMs(isScreenOn: Boolean, isWatchedAppInForeground: Boolean): Long = when {
        !isScreenOn -> SCREEN_OFF_MS
        isWatchedAppInForeground -> WATCHING_MS
        else -> IDLE_MS
    }
}
