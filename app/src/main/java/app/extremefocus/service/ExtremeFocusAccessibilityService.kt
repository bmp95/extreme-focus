package app.extremefocus.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import app.extremefocus.MainActivity
import app.extremefocus.data.local.AppDatabase
import app.extremefocus.data.local.BlockEventLog
import app.extremefocus.domain.ToughLoveQuoteEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ExtremeFocusAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(applicationContext)
        Log.d(TAG, "ExtremeFocusAccessibilityService initialized")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val targetPackage = event.packageName?.toString() ?: return
        if (targetPackage == packageName || targetPackage == "com.android.systemui") {
            return
        }

        // 1. ANTI-TAMPER: Intercept attempts to tamper or disable within Settings / App info
        if (targetPackage == "com.android.settings" || targetPackage.contains("packageinstaller")) {
            val rootNode = rootInActiveWindow
            if (rootNode != null && isAttemptingToDisableExtremeFocus(rootNode)) {
                Log.w(TAG, "ANTI-TAMPER TRIGGERED: User is attempting to sabotage Extreme Focus!")
                performGlobalAction(GLOBAL_ACTION_HOME)
                serviceScope.launch {
                    database.blockEventDao().logEvent(
                        BlockEventLog(
                            packageName = targetPackage,
                            appName = "Ajustes del Sistema",
                            toughQuote = "¿Intentando desactivar la app en Ajustes? Qué predecible y qué débil. Vuelve al trabajo."
                        )
                    )
                }
                return
            }
        }

        // 2. SOCIAL MEDIA WEB BYPASS: Intercept browser tabs attempting to open social websites
        val isBrowser = targetPackage in BROWSER_PACKAGES
        if (isBrowser) {
            val rootNode = rootInActiveWindow
            val visitedSocialUrl = findSocialUrlInNode(rootNode)
            if (visitedSocialUrl != null) {
                Log.w(TAG, "INTERCEPTING BROWSER EVASION: $visitedSocialUrl in $targetPackage")
                performGlobalAction(GLOBAL_ACTION_HOME)
                triggerBlockScreen(
                    targetPackage = targetPackage,
                    appName = "Web: $visitedSocialUrl",
                    minutes = 30
                )
                return
            }
        }

        // 3. NATIVE APP INTERCEPTION
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }

        serviceScope.launch {
            val monitoredApp = database.monitoredAppDao().getAppByPackage(targetPackage)
            if (monitoredApp != null && monitoredApp.isBlocked) {
                val now = System.currentTimeMillis()
                val isTemporarilyFree = monitoredApp.isTemporaryUnlocked && monitoredApp.temporaryUnlockExpiresAt > now

                if (!isTemporarilyFree) {
                    val isExceeded = monitoredApp.currentUsageMinutes >= monitoredApp.dailyLimitMinutes
                    if (isExceeded) {
                        Log.w(TAG, "INTERCEPTING APP: $targetPackage - Usage exceeded limit!")
                        performGlobalAction(GLOBAL_ACTION_HOME)
                        triggerBlockScreen(
                            targetPackage = targetPackage,
                            appName = monitoredApp.appName,
                            minutes = monitoredApp.currentUsageMinutes
                        )
                    }
                }
            }
        }
    }

    private fun triggerBlockScreen(targetPackage: String, appName: String, minutes: Int) {
        serviceScope.launch {
            val quote = ToughLoveQuoteEngine.getQuoteForApp(appName, minutes)
            database.monitoredAppDao().incrementBlockedCount(targetPackage)
            database.blockEventDao().logEvent(
                BlockEventLog(
                    packageName = targetPackage,
                    appName = appName,
                    toughQuote = quote.body
                )
            )

            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra("EXTRA_TARGET_PACKAGE", targetPackage)
                putExtra("EXTRA_TARGET_NAME", appName)
                putExtra("EXTRA_MINUTES_SPENT", minutes)
                putExtra("EXTRA_TRIGGER_BLOCK_SCREEN", true)
            }
            startActivity(intent)
        }
    }

    private fun isAttemptingToDisableExtremeFocus(node: AccessibilityNodeInfo): Boolean {
        // Look for our app name and dangerous buttons like "Desinstalar", "Forzar detención", "Desactivar"
        val nodeText = node.text?.toString()?.lowercase() ?: ""
        val contentDesc = node.contentDescription?.toString()?.lowercase() ?: ""

        val mentionsOurApp = nodeText.contains("extreme focus") || contentDesc.contains("extreme focus")
        if (mentionsOurApp) {
            // Check for buttons in this tree
            val hasSabotageButton = findSabotageKeyword(node)
            if (hasSabotageButton) return true
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (isAttemptingToDisableExtremeFocus(child)) {
                return true
            }
        }
        return false
    }

    private fun findSabotageKeyword(node: AccessibilityNodeInfo): Boolean {
        val text = node.text?.toString()?.lowercase() ?: ""
        if (text.contains("desinstalar") || text.contains("uninstall") ||
            text.contains("forzar detenci") || text.contains("force stop") ||
            text.contains("desactivar") || text.contains("disable")) {
            return true
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (findSabotageKeyword(child)) return true
        }
        return false
    }

    private fun findSocialUrlInNode(node: AccessibilityNodeInfo?): String? {
        if (node == null) return null
        val text = node.text?.toString()?.lowercase() ?: ""
        for (domain in SOCIAL_DOMAINS) {
            if (text.contains(domain)) {
                return domain
            }
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findSocialUrlInNode(child)
            if (found != null) return found
        }
        return null
    }

    override fun onInterrupt() {
        Log.e(TAG, "Service interrupted")
    }

    companion object {
        private const val TAG = "ExtremeFocusService"

        private val BROWSER_PACKAGES = setOf(
            "com.android.chrome",
            "org.mozilla.firefox",
            "com.brave.browser",
            "com.microsoft.emmx",
            "com.opera.browser"
        )

        private val SOCIAL_DOMAINS = listOf(
            "instagram.com",
            "tiktok.com",
            "facebook.com",
            "twitter.com",
            "x.com"
        )
    }
}
