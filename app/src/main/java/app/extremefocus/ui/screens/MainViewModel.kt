package app.extremefocus.ui.screens

import android.app.Activity
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.extremefocus.data.local.AppDatabase
import app.extremefocus.data.local.BlockEventLog
import app.extremefocus.data.local.MonitoredAppEntity
import app.extremefocus.domain.DefaultMonitoredApps
import app.extremefocus.domain.PlatformRecommendationEngine
import app.extremefocus.domain.RestrictionTier
import app.extremefocus.service.ExtremeFocusMonitorService
import app.extremefocus.service.MonotonyOverlayService
import app.extremefocus.service.SystemUsageManager
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

typealias PermissionsState = app.extremefocus.domain.PermissionsState

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
        val canPost = usageManager.canPostNotifications()
        val isAdmin = usageManager.isDeviceAdminActive()
        val batteryFree = usageManager.isBatteryUnrestricted()

        _permissionsState.value = PermissionsState(
            hasUsageStats = hasUsage,
            isAccessibilityEnabled = isAccess,
            hasOverlay = hasOverlay,
            hasNotificationAccess = hasNotif,
            canPostNotifications = canPost,
            isDeviceAdminActive = isAdmin,
            isBatteryUnrestricted = batteryFree
        )

        if (hasUsage) {
            try {
                ExtremeFocusMonitorService.startService(context)
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Failed to start monitor service: ${e.message}")
            }
        }
    }

    /** Xiaomi needs an extra, unreadable autostart grant, so the step is only shown there. */
    val needsAutostartStep: Boolean = usageManager.isMiui()

    fun requestUsagePermission() = usageManager.openUsageAccessSettings()
    fun requestAccessibilityPermission() = usageManager.openAccessibilitySettings()
    fun requestOverlayPermission() = usageManager.openOverlaySettings()
    fun requestNotificationPermission() = usageManager.openNotificationListenerSettings()
    fun requestDeviceAdmin(activity: Activity) = usageManager.requestDeviceAdmin(activity)
    fun requestBatteryUnrestricted() = usageManager.requestIgnoreBatteryOptimizations()
    fun requestAutostart() = usageManager.openAutostartSettings()

    private fun seedDefaultsIfEmpty() {
        viewModelScope.launch(Dispatchers.IO) {
            if (database.monitoredAppDao().getCount() > 0) return@launch
            val installed = context.packageManager
                .getInstalledApplications(0)
                .mapTo(mutableSetOf()) { it.packageName }
            database.monitoredAppDao().insertAll(DefaultMonitoredApps.seedFor(installed))
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

    fun applyRestrictionTier(tier: RestrictionTier) {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = database.monitoredAppDao().getAllMonitoredApps()
            apps.forEach { app ->
                val recommended = PlatformRecommendationEngine.getRecommendedMinutes(app.packageName, tier)
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
