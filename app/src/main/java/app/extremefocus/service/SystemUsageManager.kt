package app.extremefocus.service

import android.Manifest
import android.app.AppOpsManager
import android.app.admin.DevicePolicyManager
import android.app.usage.UsageStatsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import android.util.Log
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

    /** Whether the system has stopped throttling this app, which is what keeps monitoring alive. */
    fun isBatteryUnrestricted(): Boolean {
        val pm = context.getSystemService(PowerManager::class.java) ?: return false
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    /** Xiaomi devices kill background work unless the user also grants autostart by hand. */
    fun isMiui(): Boolean = !getSystemProperty("ro.miui.ui.version.name").isNullOrBlank()

    private fun getSystemProperty(key: String): String? = try {
        @Suppress("PrivateApi")
        Class.forName("android.os.SystemProperties")
            .getMethod("get", String::class.java)
            .invoke(null, key) as? String
    } catch (e: Exception) {
        null
    }

    fun openUsageAccessSettings() = startSettings(
        Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    )

    fun openAccessibilitySettings() = startSettings(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))

    // The package URI lands directly on this app's switch instead of a list to search through.
    fun openOverlaySettings() = startSettings(
        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, appUri())
    )

    fun openNotificationListenerSettings() = startSettings(
        Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
    )

    fun openAppNotificationSettings() = startSettings(
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    )

    fun requestIgnoreBatteryOptimizations() = startSettings(
        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, appUri())
    )

    /** Opens Xiaomi's autostart list, falling back to this app's details page elsewhere. */
    fun openAutostartSettings() {
        val miuiAutostart = Intent().setClassName(
            "com.miui.securitycenter",
            "com.miui.permcenter.autostart.AutoStartManagementActivity"
        )
        if (miuiAutostart.resolveActivity(context.packageManager) != null) {
            startSettings(miuiAutostart)
        } else {
            openAppDetailsSettings()
        }
    }

    fun openAppDetailsSettings() = startSettings(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, appUri())
    )

    private fun appUri(): Uri = Uri.parse("package:${context.packageName}")

    private fun startSettings(intent: Intent) {
        try {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: Exception) {
            Log.e("SystemUsageManager", "No activity for ${intent.action}: ${e.message}")
            if (intent.action != Settings.ACTION_APPLICATION_DETAILS_SETTINGS) openAppDetailsSettings()
        }
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
