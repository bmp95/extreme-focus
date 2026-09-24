package app.extremefocus

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import app.extremefocus.data.local.AppDatabase
import app.extremefocus.domain.SetupStep
import app.extremefocus.ui.screens.AppScreen
import app.extremefocus.ui.screens.AppSelectorScreen
import app.extremefocus.ui.screens.AuditLogsScreen
import app.extremefocus.ui.screens.BlockInterceptionScreen
import app.extremefocus.ui.screens.ChallengeType
import app.extremefocus.ui.screens.DashboardScreen
import app.extremefocus.ui.screens.FrictionChallengeScreen
import app.extremefocus.ui.screens.MainViewModel
import app.extremefocus.ui.screens.MainViewModelFactory
import app.extremefocus.ui.screens.MonotonyTaskScreen
import app.extremefocus.ui.screens.PermissionsGuideScreen
import app.extremefocus.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(
            context = applicationContext,
            database = AppDatabase.getDatabase(applicationContext)
        )
    }

    private val requestPostNotifications =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            viewModel.checkPermissions()
        }

    /**
     * Without this permission the monitor's status notification is silently dropped on
     * Android 13+, leaving no visible sign that protection is still running.
     */
    private fun requestPostNotificationsPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) return
        requestPostNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestPostNotificationsPermission()

        handleIntent(intent)

        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                ) { innerPadding ->
                    val currentScreen by viewModel.currentScreen.collectAsState()
                    val monitoredApps by viewModel.monitoredApps.collectAsState()
                    val permissionsState by viewModel.permissionsState.collectAsState()
                    val installedApps by viewModel.installedApps.collectAsState()
                    val auditLogs by viewModel.recentLogs.collectAsState()

                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            if (targetState is AppScreen.BlockScreen) {
                                (fadeIn(animationSpec = tween(220)) +
                                        slideInHorizontally(animationSpec = tween(250)) { it / 4 })
                                    .togetherWith(fadeOut(animationSpec = tween(180)))
                            } else {
                                (fadeIn(animationSpec = tween(220)) +
                                        slideInHorizontally(animationSpec = tween(240)) { it / 6 })
                                    .togetherWith(
                                        fadeOut(animationSpec = tween(180)) +
                                                slideOutHorizontally(animationSpec = tween(200)) { -it / 6 }
                                    )
                            }
                        },
                        label = "screen_navigation_transition"
                    ) { screen ->
                        when (screen) {
                            is AppScreen.Dashboard -> {
                                DashboardScreen(
                                    monitoredApps = monitoredApps,
                                    permissionsState = permissionsState,
                                    onNavigate = { viewModel.navigateTo(it) },
                                    onToggleBlock = { viewModel.toggleAppBlock(it) },
                                    onToggleNotificationBlock = { pkg, isBlocked ->
                                        viewModel.toggleNotificationBlock(pkg, isBlocked)
                                    },
                                    onUpdateLimit = { pkg, limit -> viewModel.updateDailyLimit(pkg, limit) },
                                    onApplyRestrictionTier = { tier -> viewModel.applyRestrictionTier(tier) },
                                    onSimulateIntercept = { pkg -> viewModel.simulateIntercept(pkg) },
                                    onRefreshStats = { viewModel.refreshUsageStats() }
                                )
                            }
                            is AppScreen.AppSelector -> {
                                AppSelectorScreen(
                                    installedApps = installedApps,
                                    monitoredApps = monitoredApps,
                                    onAddApp = { pkg, name ->
                                        viewModel.addAppToMonitor(pkg, name)
                                        viewModel.navigateTo(AppScreen.Dashboard)
                                    },
                                    onBack = { viewModel.navigateTo(AppScreen.Dashboard) }
                                )
                            }
                            is AppScreen.BlockScreen -> {
                                BlockInterceptionScreen(
                                    packageName = screen.packageName,
                                    appName = screen.appName,
                                    minutesSpent = screen.minutesSpent,
                                    onCloseToHome = { viewModel.navigateTo(AppScreen.Dashboard) },
                                    onStartChallenge = { challengeType ->
                                        if (challengeType == ChallengeType.MONOTONY_TASK) {
                                            viewModel.navigateTo(
                                                AppScreen.MonotonyTask(
                                                    packageName = screen.packageName,
                                                    appName = screen.appName
                                                )
                                            )
                                        } else {
                                            viewModel.navigateTo(
                                                AppScreen.FrictionChallenge(
                                                    packageName = screen.packageName,
                                                    appName = screen.appName,
                                                    challengeType = challengeType
                                                )
                                            )
                                        }
                                    },
                                    onStartMonotonyTask = {
                                        if (permissionsState.hasOverlay) {
                                            viewModel.launchMonotonyOverlay(
                                                packageName = screen.packageName,
                                                appName = screen.appName
                                            )
                                        } else {
                                            viewModel.navigateTo(
                                                AppScreen.MonotonyTask(
                                                    packageName = screen.packageName,
                                                    appName = screen.appName
                                                )
                                            )
                                        }
                                    }
                                )
                            }
                            is AppScreen.MonotonyTask -> {
                                MonotonyTaskScreen(
                                    packageName = screen.packageName,
                                    appName = screen.appName,
                                    onCompletedWhitelist15Min = {
                                        viewModel.unlockTemporarily(screen.packageName, 15)
                                        viewModel.navigateTo(AppScreen.Dashboard)
                                    },
                                    onCancel = { viewModel.navigateTo(AppScreen.Dashboard) }
                                )
                            }
                            is AppScreen.FrictionChallenge -> {
                                FrictionChallengeScreen(
                                    packageName = screen.packageName,
                                    appName = screen.appName,
                                    challengeType = screen.challengeType,
                                    onChallengeCompleted = { minutesUnlocked ->
                                        viewModel.unlockTemporarily(screen.packageName, minutesUnlocked)
                                        viewModel.navigateTo(AppScreen.Dashboard)
                                    },
                                    onCancel = { viewModel.navigateTo(AppScreen.Dashboard) }
                                )
                            }
                            is AppScreen.AuditLogs -> {
                                AuditLogsScreen(
                                    logs = auditLogs,
                                    onBack = { viewModel.navigateTo(AppScreen.Dashboard) }
                                )
                            }
                            is AppScreen.PermissionsGuide -> {
                                PermissionsGuideScreen(
                                    state = permissionsState,
                                    includeAutostart = viewModel.needsAutostartStep,
                                    onRequestStep = { step ->
                                        when (step) {
                                            SetupStep.USAGE_ACCESS -> viewModel.requestUsagePermission()
                                            SetupStep.ACCESSIBILITY -> viewModel.requestAccessibilityPermission()
                                            SetupStep.BATTERY_UNRESTRICTED -> viewModel.requestBatteryUnrestricted()
                                            SetupStep.AUTOSTART -> viewModel.requestAutostart()
                                            SetupStep.POST_NOTIFICATIONS -> requestPostNotificationsPermission()
                                            SetupStep.OVERLAY -> viewModel.requestOverlayPermission()
                                            SetupStep.NOTIFICATION_LISTENER -> viewModel.requestNotificationPermission()
                                            SetupStep.DEVICE_ADMIN -> viewModel.requestDeviceAdmin(this@MainActivity)
                                        }
                                    },
                                    onBack = { viewModel.navigateTo(AppScreen.Dashboard) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkPermissions()
        viewModel.refreshUsageStats()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent != null && intent.getBooleanExtra("EXTRA_TRIGGER_BLOCK_SCREEN", false)) {
            val pkg = intent.getStringExtra("EXTRA_TARGET_PACKAGE") ?: ""
            val name = intent.getStringExtra("EXTRA_TARGET_NAME") ?: "App Bloqueada"
            val minutes = intent.getIntExtra("EXTRA_MINUTES_SPENT", 0)
            if (pkg.isNotEmpty()) {
                viewModel.navigateTo(
                    AppScreen.BlockScreen(
                        packageName = pkg,
                        appName = name,
                        minutesSpent = minutes
                    )
                )
            }
        }
    }
}
