package app.extremefocus.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import app.extremefocus.service.ExtremeFocusMonitorService

/**
 * The system kills the monitor whenever it decides the app has been idle too long, and nothing
 * brings it back on its own. This alarm fires periodically and starts it again, so enforcement
 * resumes instead of quietly staying dead.
 */
class WatchdogReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Watchdog fired, ensuring monitor is alive")
        ExtremeFocusMonitorService.startService(context)
        schedule(context)
    }

    companion object {
        private const val TAG = "ExtremeFocusWatchdog"
        private const val REQUEST_CODE = 9902
        private const val INTERVAL_MS = 15 * 60 * 1000L

        fun schedule(context: Context) {
            val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
            val triggerAt = SystemClock.elapsedRealtime() + INTERVAL_MS
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pendingIntent(context))
        }

        private fun pendingIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            Intent(context, WatchdogReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}
