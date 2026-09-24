package app.extremefocus.service

import android.Manifest
import android.app.AppOpsManager
import android.app.admin.DevicePolicyManager
import android.app.usage.UsageStatsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import app.extremefocus.receiver.ExtremeFocusDeviceAdminReceiver
import java.util.Calendar

class SystemUsageManager(private val context: Context) {

    fun hasUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun hasOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun isAccessibilityServiceEnabled(): Boolean {
        val expectedServiceName = "${context.packageName}/${ExtremeFocusAccessibilityService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        return enabledServices.contains(expectedServiceName)
    }

    /** Whether the app may read and cancel other apps' notifications. */
    fun isNotificationAccessGranted(): Boolean {
        return NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
    }

    /** Whether the app may show its own notifications, including the monitor's status one. */
    fun canPostNotifications(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun isDeviceAdminActive(): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
        val adminComponent = ComponentName(context, ExtremeFocusDeviceAdminReceiver::class.java)
        return dpm?.isAdminActive(adminComponent) == true
    }

    fun openUsageAccessSettings() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun openOverlaySettings() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun openNotificationListenerSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun requestDeviceAdmin(activity: android.app.Activity) {
        val adminComponent = ComponentName(context, ExtremeFocusDeviceAdminReceiver::class.java)
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Extreme Focus requiere estatus de Administrador para impedir la desinstalación compulsiva en momentos de debilidad."
            )
        }
        activity.startActivity(intent)
    }

    /**
     * Reads today's foreground minutes for a package from midnight until now.
     */
    fun getTodayUsageMinutes(packageName: String): Int {
        val map = getTodayUsageMinutesBulk(listOf(packageName))
        return map[packageName] ?: 0
    }

    /**
     * Bulk reads today's foreground minutes from midnight until now using UsageStatsManager.
     * Efficiently scans the daily usage bucket in a single local query.
     */
    fun getTodayUsageMinutesBulk(packageNames: List<String>): Map<String, Int> {
        if (!hasUsageStatsPermission()) return emptyMap()

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return emptyMap()

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        ) ?: return emptyMap()

        val targetSet = packageNames.toSet()
        val result = mutableMapOf<String, Int>()

        for (appStat in stats) {
            if (targetSet.contains(appStat.packageName)) {
                val existing = result[appStat.packageName] ?: 0
                val minutes = (appStat.totalTimeInForeground / (1000 * 60)).toInt()
                result[appStat.packageName] = maxOf(existing, minutes)
            }
        }
        return result
    }

    /**
     * Detects which app is currently in the foreground using UsageEvents.
     * Works locally without requiring Accessibility Service if Accessibility is temporarily stopped.
     */
    fun getCurrentForegroundPackage(): String? {
        if (!hasUsageStatsPermission()) return null

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return null

        val endTime = System.currentTimeMillis()
        val startTime = endTime - 1000 * 60 * 5 // Last 5 minutes window

        val usageEvents = usageStatsManager.queryEvents(startTime, endTime) ?: return null
        val event = android.app.usage.UsageEvents.Event()

        var lastForegroundApp: String? = null
        var lastEventTimestamp = 0L

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.eventType == android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED ||
                event.eventType == android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND) {
                if (event.timeStamp >= lastEventTimestamp) {
                    lastEventTimestamp = event.timeStamp
                    lastForegroundApp = event.packageName
                }
            }
        }

        return lastForegroundApp
    }
}
