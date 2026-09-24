package app.extremefocus.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import app.extremefocus.data.local.AppDatabase
import app.extremefocus.service.ExtremeFocusMonitorService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootAndResetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("BootAndResetReceiver", "Broadcast received: $action")

        if (Intent.ACTION_BOOT_COMPLETED == action || Intent.ACTION_DATE_CHANGED == action || Intent.ACTION_TIME_CHANGED == action) {
            val db = AppDatabase.getDatabase(context)
            CoroutineScope(Dispatchers.IO).launch {
                // Clear any expired temporary unlocks
                val apps = db.monitoredAppDao().getAllMonitoredApps()
                val now = System.currentTimeMillis()
                apps.forEach { app ->
                    if (app.isTemporaryUnlocked && app.temporaryUnlockExpiresAt < now) {
                        db.monitoredAppDao().setTemporaryUnlock(app.packageName, false, 0L)
                    }
                }
            }

            // Start foreground monitoring service
            ExtremeFocusMonitorService.startService(context)
        }
    }
}
