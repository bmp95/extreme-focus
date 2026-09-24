package app.extremefocus.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import app.extremefocus.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ExtremeFocusNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(applicationContext)
        Log.i(TAG, "NotificationListenerService initialized")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val pkg = sbn.packageName ?: return

        // Never cancel notifications from ourselves or system criticals
        if (pkg == packageName || pkg == "android" || pkg == "com.android.systemui" || pkg == "com.android.dialer") {
            return
        }

        serviceScope.launch {
            val monitoredApp = database.monitoredAppDao().getAppByPackage(pkg)
            if (monitoredApp != null) {
                // If notifications are blocked for this app
                if (monitoredApp.blockNotifications) {
                    Log.w(TAG, "SILENCING/CANCELING Notification from dopamine source: $pkg")
                    cancelNotification(sbn.key)
                }
            }
        }
    }

    companion object {
        private const val TAG = "NotificationFilter"
    }
}
