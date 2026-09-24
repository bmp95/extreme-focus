package app.extremefocus.service

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.provider.Settings
import app.extremefocus.MainActivity

/**
 * The single place a block is raised.
 *
 * Both the accessibility service and the usage monitor detect the same app opening, so without
 * one shared gate the user gets two block screens: one dismissed by the challenge and another
 * left behind on top of it.
 */
object BlockPresenter {

    private const val DEBOUNCE_MS = 5_000L

    private var lastPackage: String? = null
    private var lastAt = 0L

    @Synchronized
    fun claim(packageName: String): Boolean {
        val now = SystemClock.elapsedRealtime()
        if (packageName == lastPackage && now - lastAt < DEBOUNCE_MS) return false
        lastPackage = packageName
        lastAt = now
        return true
    }

    @Synchronized
    fun release() {
        lastPackage = null
        lastAt = 0L
    }

    /**
     * Returns true when the block was drawn over the offending app. Falling back to an activity
     * means the system may drop it, so the caller sends the user home as well.
     */
    fun show(context: Context, packageName: String, appName: String, minutesSpent: Int): Boolean {
        if (Settings.canDrawOverlays(context)) {
            BlockOverlayService.show(context, packageName, appName, minutesSpent)
            return true
        }

        context.startActivity(
            Intent(context, MainActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
                putExtra("EXTRA_TARGET_PACKAGE", packageName)
                putExtra("EXTRA_TARGET_NAME", appName)
                putExtra("EXTRA_MINUTES_SPENT", minutesSpent)
                putExtra("EXTRA_TRIGGER_BLOCK_SCREEN", true)
            }
        )
        return false
    }
}
