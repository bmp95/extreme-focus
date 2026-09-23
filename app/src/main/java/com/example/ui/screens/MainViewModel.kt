package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.BlockEventLog
import com.example.data.local.MonitoredAppEntity
import com.example.service.MonotonyOverlayService
import com.example.service.SystemUsageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean
)

data class PermissionsState(
    val hasUsageStats: Boolean = false,
    val isAccessibilityEnabled: Boolean = false,
    val hasOverlay: Boolean = false,
    val hasNotificationAccess: Boolean = false,
    val isDeviceAdminActive: Boolean = false
)

class MainViewModel(
    private val context: Context,
    private val database: AppDatabase
) : ViewModel() {

    private val usageManager = SystemUsageManager(context)

    val monitoredApps: StateFlow<List<MonitoredAppEntity>> =
        database.monitoredAppDao().getAllMonitoredAppsFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentLogs: StateFlow<List<BlockEventLog>> =
        database.blockEventDao().getRecentBlockEventsFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _permissionsState = MutableStateFlow(PermissionsState())
    val permissionsState: StateFlow<PermissionsState> = _permissionsState.asStateFlow()

    private val _installedApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppInfo>> = _installedApps.asStateFlow()

    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.Dashboard)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    init {
        checkPermissions()
        seedDefaultsIfEmpty()
        loadInstalledApps()
        refreshUsageStats()
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun checkPermissions() {
        val hasUsage = usageManager.hasUsageStatsPermission()
        val isAccess = usageManager.isAccessibilityServiceEnabled()
        val hasOverlay = usageManager.hasOverlayPermission()
        val hasNotif = usageManager.isNotificationAccessGranted()
        val isAdmin = usageManager.isDeviceAdminActive()

        _permissionsState.value = PermissionsState(
            hasUsageStats = hasUsage,
            isAccessibilityEnabled = isAccess,
            hasOverlay = hasOverlay,
            hasNotificationAccess = hasNotif,
            isDeviceAdminActive = isAdmin
        )

        if (hasUsage) {
            try {
                com.example.service.ExtremeFocusMonitorService.startService(context)
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Failed to start monitor service: ${e.message}")
            }
        }
    }

    fun requestUsagePermission() = usageManager.openUsageAccessSettings()
    fun requestAccessibilityPermission() = usageManager.openAccessibilitySettings()
    fun requestOverlayPermission() = usageManager.openOverlaySettings()
    fun requestNotificationPermission() = usageManager.openNotificationListenerSettings()
    fun requestDeviceAdmin(activity: Activity) = usageManager.requestDeviceAdmin(activity)

    private fun seedDefaultsIfEmpty() {
        viewModelScope.launch(Dispatchers.IO) {
            val count = database.monitoredAppDao().getCount()
            if (count == 0) {
                val defaults = listOf(
                    MonitoredAppEntity(
                        packageName = "com.google.android.youtube",
                        appName = "YouTube",
                        dailyLimitMinutes = 30,
                        isBlocked = true,
                        currentUsageMinutes = 28,
                        iconCategory = "VIDEO",
                        blockNotifications = true
                    ),
                    MonitoredAppEntity(
                        packageName = "com.instagram.android",
                        appName = "Instagram",
                        dailyLimitMinutes = 15,
                        isBlocked = true,
                        currentUsageMinutes = 24,
                        iconCategory = "SOCIAL",
                        blockNotifications = true
                    ),
                    MonitoredAppEntity(
                        packageName = "com.zhiliaoapp.musically",
                        appName = "TikTok",
                        dailyLimitMinutes = 10,
                        isBlocked = true,
                        currentUsageMinutes = 48,
                        iconCategory = "VIDEO",
                        blockNotifications = true
                    ),
                    MonitoredAppEntity(
                        packageName = "com.twitter.android",
                        appName = "X (Twitter)",
                        dailyLimitMinutes = 15,
                        isBlocked = true,
                        currentUsageMinutes = 19,
                        iconCategory = "SOCIAL",
                        blockNotifications = true
                    ),
                    MonitoredAppEntity(
                        packageName = "com.reddit.frontpage",
                        appName = "Reddit",
                        dailyLimitMinutes = 20,
                        isBlocked = true,
                        currentUsageMinutes = 14,
                        iconCategory = "SOCIAL",
                        blockNotifications = true
                    ),
                    MonitoredAppEntity(
                        packageName = "tv.twitch.android.app",
                        appName = "Twitch",
                        dailyLimitMinutes = 25,
                        isBlocked = true,
                        currentUsageMinutes = 0,
                        iconCategory = "VIDEO",
                        blockNotifications = true
                    ),
                    MonitoredAppEntity(
                        packageName = "com.supercell.brawlstars",
                        appName = "Brawl Stars",
                        dailyLimitMinutes = 15,
                        isBlocked = true,
                        currentUsageMinutes = 22,
                        iconCategory = "GAME",
                        blockNotifications = true
                    ),
                    MonitoredAppEntity(
                        packageName = "com.supercell.clashroyale",
                        appName = "Clash Royale",
                        dailyLimitMinutes = 15,
                        isBlocked = true,
                        currentUsageMinutes = 0,
                        iconCategory = "GAME",
                        blockNotifications = true
                    ),
                    MonitoredAppEntity(
                        packageName = "com.roblox.client",
                        appName = "Roblox",
                        dailyLimitMinutes = 20,
                        isBlocked = true,
                        currentUsageMinutes = 0,
                        iconCategory = "GAME",
                        blockNotifications = true
                    ),
                    MonitoredAppEntity(
                        packageName = "com.facebook.katana",
                        appName = "Facebook",
                        dailyLimitMinutes = 15,
                        isBlocked = true,
                        currentUsageMinutes = 8,
                        iconCategory = "SOCIAL",
                        blockNotifications = true
                    )
                )
                database.monitoredAppDao().insertAll(defaults)
            }
        }
    }

    fun refreshUsageStats() {
        viewModelScope.launch(Dispatchers.IO) {
            if (usageManager.hasUsageStatsPermission()) {
                val apps = database.monitoredAppDao().getAllMonitoredApps()
                if (apps.isNotEmpty()) {
                    val packageList = apps.map { it.packageName }
                    val usageMap = usageManager.getTodayUsageMinutesBulk(packageList)
                    apps.forEach { app ->
                        val actualMinutes = usageMap[app.packageName] ?: 0
                        if (actualMinutes != app.currentUsageMinutes) {
                            database.monitoredAppDao().updateUsage(app.packageName, actualMinutes)
                        }
                    }
                }
            }
        }
    }

    fun toggleAppBlock(app: MonitoredAppEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            database.monitoredAppDao().insertOrUpdateApp(app.copy(isBlocked = !app.isBlocked))
        }
    }

    fun toggleNotificationBlock(packageName: String, currentBlockValue: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            database.monitoredAppDao().updateNotificationBlock(packageName, !currentBlockValue)
        }
    }

    fun updateDailyLimit(packageName: String, newMinutes: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val app = database.monitoredAppDao().getAppByPackage(packageName) ?: return@launch
            database.monitoredAppDao().insertOrUpdateApp(app.copy(dailyLimitMinutes = newMinutes))
        }
    }

    fun applyRestrictionTier(tier: com.example.domain.RestrictionTier) {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = database.monitoredAppDao().getAllMonitoredApps()
            apps.forEach { app ->
                val recommended = com.example.domain.PlatformRecommendationEngine.getRecommendedMinutes(app.packageName, tier)
                database.monitoredAppDao().insertOrUpdateApp(app.copy(dailyLimitMinutes = recommended))
            }
        }
    }

    fun addAppToMonitor(packageName: String, appName: String, category: String = "SOCIAL") {
        viewModelScope.launch(Dispatchers.IO) {
            val newApp = MonitoredAppEntity(
                packageName = packageName,
                appName = appName,
                dailyLimitMinutes = 15,
                isBlocked = true,
                currentUsageMinutes = 0,
                iconCategory = category,
                blockNotifications = true
            )
            database.monitoredAppDao().insertOrUpdateApp(newApp)
        }
    }

    fun removeMonitoredApp(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            database.monitoredAppDao().deleteApp(packageName)
        }
    }

    fun unlockTemporarily(packageName: String, minutes: Int = 3) {
        viewModelScope.launch(Dispatchers.IO) {
            val expiresAt = System.currentTimeMillis() + (minutes * 60 * 1000L)
            database.monitoredAppDao().setTemporaryUnlock(packageName, true, expiresAt)
            val app = database.monitoredAppDao().getAppByPackage(packageName)
            database.blockEventDao().logEvent(
                BlockEventLog(
                    packageName = packageName,
                    appName = app?.appName ?: packageName,
                    toughQuote = "Superó la prueba de fricción. Desbloqueo temporal de $minutes min otorgado.",
                    challengeAttempted = "Fricción Superada",
                    challengeSucceeded = true
                )
            )
        }
    }

    fun simulateIntercept(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val app = database.monitoredAppDao().getAppByPackage(packageName)
            if (app != null) {
                database.monitoredAppDao().incrementBlockedCount(packageName)
                _currentScreen.value = AppScreen.BlockScreen(
                    packageName = app.packageName,
                    appName = app.appName,
                    minutesSpent = app.currentUsageMinutes
                )
            }
        }
    }

    fun launchMonotonyOverlay(packageName: String, appName: String) {
        if (usageManager.hasOverlayPermission()) {
            MonotonyOverlayService.showOverlay(context, packageName, appName)
        } else {
            // If overlay permission is not granted yet, navigate within app
            _currentScreen.value = AppScreen.MonotonyTask(packageName, appName)
        }
    }

    fun dismissMonotonyOverlay() {
        MonotonyOverlayService.dismissOverlay(context)
    }

    private fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val pm = context.packageManager
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            val list = packages.mapNotNull { appInfo ->
                val label = pm.getApplicationLabel(appInfo).toString()
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                InstalledAppInfo(
                    packageName = appInfo.packageName,
                    appName = label,
                    isSystemApp = isSystem
                )
            }.sortedBy { it.appName.lowercase() }
            withContext(Dispatchers.Main) {
                _installedApps.value = list
            }
        }
    }
}

class MainViewModelFactory(
    private val context: Context,
    private val database: AppDatabase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(context, database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
