package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.example.data.local.AppDatabase
import com.example.ui.screens.AppScreen
import com.example.ui.screens.AppSelectorScreen
import com.example.ui.screens.AuditLogsScreen
import com.example.ui.screens.BlockInterceptionScreen
import com.example.ui.screens.ChallengeType
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FrictionChallengeScreen
import com.example.ui.screens.MainViewModel
import com.example.ui.screens.MainViewModelFactory
import com.example.ui.screens.MonotonyTaskScreen
import com.example.ui.screens.PermissionsGuideScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(
            context = applicationContext,
            database = AppDatabase.getDatabase(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
                                    onRequestUsage = { viewModel.requestUsagePermission() },
                                    onRequestAccessibility = { viewModel.requestAccessibilityPermission() },
                                    onRequestOverlay = { viewModel.requestOverlayPermission() },
                                    onRequestNotifications = { viewModel.requestNotificationPermission() },
                                    onRequestDeviceAdmin = { viewModel.requestDeviceAdmin(this@MainActivity) },
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
