package app.extremefocus.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import app.extremefocus.domain.MonitorCadence
import app.extremefocus.MainActivity
import app.extremefocus.R
import app.extremefocus.data.local.AppDatabase
import app.extremefocus.data.local.BlockEventLog
import app.extremefocus.domain.ToughLoveQuoteEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Foreground Service that constantly monitors UsageStatsManager locally on-device.
 *
 * Responsibilities:
 * 1. Tracks exact time spent in target apps (YouTube, Instagram, TikTok, Brawl Stars, etc.).
 * 2. Runs independently in the background to ensure real-time threshold detection.
 * 3. When an app exceeds its dailyLimitMinutes, triggers immediate interception,
 *    updates the local database, and launches the block experience.
 * 4. Shows a persistent, transparent notification detailing current focus protection.
 */
class ExtremeFocusMonitorService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private var monitorJob: Job? = null

    private lateinit var database: AppDatabase
    private lateinit var usageManager: SystemUsageManager
    private lateinit var powerManager: PowerManager

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(applicationContext)
        usageManager = SystemUsageManager(applicationContext)
        powerManager = getSystemService(PowerManager::class.java)
        createNotificationChannel()
        Log.d(TAG, "ExtremeFocusMonitorService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_SERVICE -> {
                Log.d(TAG, "Stop command received")
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                startForeground(NOTIFICATION_ID, buildForegroundNotification("Protección activa", "Monitorizando límites de bienestar digital"))
                startUsageMonitoringLoop()
            }
        }
        return START_STICKY
    }

    private fun startUsageMonitoringLoop() {
        if (monitorJob?.isActive == true) return

        monitorJob = serviceScope.launch {
            Log.d(TAG, "Monitoring loop started on background IO")
            while (isActive) {
                val screenOn = powerManager.isInteractive
                var watchedAppInForeground = false
                if (screenOn) {
                    try {
                        watchedAppInForeground = checkAndSyncUsageThresholds()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in usage monitor loop: ${e.message}", e)
                    }
                }
                delay(MonitorCadence.intervalMs(screenOn, watchedAppInForeground))
            }
        }
    }

    /** Returns true when one of the monitored apps is currently on screen. */
    private suspend fun checkAndSyncUsageThresholds(): Boolean {
        if (!usageManager.hasUsageStatsPermission()) {
            return false
        }

        val monitoredApps = database.monitoredAppDao().getAllMonitoredApps()
        if (monitoredApps.isEmpty()) return false

        val packageList = monitoredApps.map { it.packageName }
        val actualUsageMap = usageManager.getTodayUsageMinutesBulk(packageList)
        val currentForegroundApp = usageManager.getCurrentForegroundPackage()
        val now = System.currentTimeMillis()

        var appsExceededCount = 0

        for (app in monitoredApps) {
            val realMinutes = actualUsageMap[app.packageName] ?: app.currentUsageMinutes

            // Update database if usage increased
            if (realMinutes > app.currentUsageMinutes) {
                database.monitoredAppDao().updateUsage(app.packageName, realMinutes)
            }

            val isTemporarilyFree = app.isTemporaryUnlocked && app.temporaryUnlockExpiresAt > now
            val isExceeded = realMinutes >= app.dailyLimitMinutes

            if (isExceeded) {
                appsExceededCount++
            }

            // If the user is currently using an app that just crossed or is over its limit
            if (app.isBlocked && isExceeded && !isTemporarilyFree) {
                if (currentForegroundApp == app.packageName) {
                    Log.w(TAG, "THRESHOLD EXCEEDED: ${app.appName} (${app.packageName}) reached $realMinutes / ${app.dailyLimitMinutes} min! Intercepting immediately.")
                    interceptForegroundApp(app.packageName, app.appName, realMinutes)
                }
            }
        }

        // Update notification with live status
        val totalUsage = actualUsageMap.values.sum()
        val notifTitle = if (appsExceededCount > 0) {
            "Límites superados ($appsExceededCount apps bloqueadas)"
        } else {
            "Protección de enfoque activa"
        }
        val notifContent = "$totalUsage min usados hoy • Control estricto local"
        updateNotification(notifTitle, notifContent)

        return monitoredApps.any { it.packageName == currentForegroundApp }
    }

    private fun interceptForegroundApp(packageName: String, appName: String, minutesSpent: Int) {
        serviceScope.launch {
            val quote = ToughLoveQuoteEngine.getQuoteForApp(appName, minutesSpent)
            database.monitoredAppDao().incrementBlockedCount(packageName)
            database.blockEventDao().logEvent(
                BlockEventLog(
                    packageName = packageName,
                    appName = appName,
                    toughQuote = quote.body
                )
            )

            // Launch Block Interception UI over the current app
            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP or
                            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                )
                putExtra("EXTRA_TARGET_PACKAGE", packageName)
                putExtra("EXTRA_TARGET_NAME", appName)
                putExtra("EXTRA_MINUTES_SPENT", minutesSpent)
                putExtra("EXTRA_TRIGGER_BLOCK_SCREEN", true)
            }
            startActivity(intent)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Monitor de Bienestar y Límites",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitorea continuamente el tiempo en pantalla de aplicaciones objetivo en segundo plano."
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(title: String, content: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification(title: String, content: String) {
        val manager = getSystemService(NotificationManager::class.java) ?: return
        manager.notify(NOTIFICATION_ID, buildForegroundNotification(title, content))
    }

    override fun onDestroy() {
        super.onDestroy()
        monitorJob?.cancel()
        serviceJob.cancel()
        Log.d(TAG, "ExtremeFocusMonitorService destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "ExtremeFocusMonitor"
        const val CHANNEL_ID = "extreme_focus_monitor_channel"
        const val NOTIFICATION_ID = 9901
        const val ACTION_START_SERVICE = "app.extremefocus.service.action.START_MONITOR"
        const val ACTION_STOP_SERVICE = "app.extremefocus.service.action.STOP_MONITOR"

        fun startService(context: Context) {
            val intent = Intent(context, ExtremeFocusMonitorService::class.java).apply {
                action = ACTION_START_SERVICE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, ExtremeFocusMonitorService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            context.startService(intent)
        }
    }
}
