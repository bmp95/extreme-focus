package app.extremefocus.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import app.extremefocus.MainActivity
import app.extremefocus.data.local.AppDatabase
import app.extremefocus.data.local.BlockEventLog
import app.extremefocus.ui.screens.MonotonyTaskScreen
import app.extremefocus.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * System Overlay Service using SYSTEM_ALERT_WINDOW permission.
 * Displays the Monotony Task UI as a persistent system-level overlay (TYPE_APPLICATION_OVERLAY)
 * on top of any blocked app, preventing users from bypassing the restriction window.
 */
class MonotonyOverlayService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    companion object {
        private const val TAG = "MonotonyOverlayService"
        const val EXTRA_PACKAGE_NAME = "EXTRA_OVERLAY_PACKAGE_NAME"
        const val EXTRA_APP_NAME = "EXTRA_OVERLAY_APP_NAME"
        const val ACTION_SHOW = "app.extremefocus.service.ACTION_SHOW_MONOTONY_OVERLAY"
        const val ACTION_DISMISS = "app.extremefocus.service.ACTION_DISMISS_MONOTONY_OVERLAY"

        fun showOverlay(context: Context, packageName: String, appName: String) {
            if (!Settings.canDrawOverlays(context)) {
                Log.w(TAG, "Cannot show overlay: SYSTEM_ALERT_WINDOW permission not granted")
                return
            }
            val intent = Intent(context, MonotonyOverlayService::class.java).apply {
                action = ACTION_SHOW
                putExtra(EXTRA_PACKAGE_NAME, packageName)
                putExtra(EXTRA_APP_NAME, appName)
            }
            context.startService(intent)
        }

        fun dismissOverlay(context: Context) {
            val intent = Intent(context, MonotonyOverlayService::class.java).apply {
                action = ACTION_DISMISS
            }
            context.startService(intent)
        }
    }

    private var windowManager: WindowManager? = null
    private var composeOverlayView: ComposeView? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var database: AppDatabase

    // Architecture components to host Jetpack Compose in a Service
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(applicationContext)
        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_NOT_STICKY

        when (intent.action) {
            ACTION_DISMISS -> {
                removeOverlay()
                stopSelf()
            }
            ACTION_SHOW -> {
                val pkg = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: ""
                val name = intent.getStringExtra(EXTRA_APP_NAME) ?: "App"
                if (pkg.isNotEmpty()) {
                    showOverlayView(pkg, name)
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun showOverlayView(targetPackage: String, targetAppName: String) {
        if (!Settings.canDrawOverlays(this)) {
            Log.e(TAG, "SYSTEM_ALERT_WINDOW permission missing. Overlay cannot be shown.")
            stopSelf()
            return
        }

        // If overlay is already active, remove old view first
        if (composeOverlayView != null) {
            removeOverlay()
        }

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        // Layout params: Match parent, intercept touches and keyboard input so user cannot bypass
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
            // Allow soft input for typing 50 items
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        }

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@MonotonyOverlayService)
            setViewTreeViewModelStoreOwner(this@MonotonyOverlayService)
            setViewTreeSavedStateRegistryOwner(this@MonotonyOverlayService)

            setContent {
                MyApplicationTheme {
                    MonotonyTaskScreen(
                        packageName = targetPackage,
                        appName = targetAppName,
                        onCompletedWhitelist15Min = {
                            handleChallengeCompleted(targetPackage, targetAppName)
                        },
                        onCancel = {
                            handleCancelAndGoHome()
                        }
                    )
                }
            }
        }

        try {
            windowManager?.addView(composeView, params)
            composeOverlayView = composeView
            Log.d(TAG, "Monotony Task overlay successfully attached over $targetPackage")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add system alert window overlay: ${e.message}", e)
            stopSelf()
        }
    }

    private fun handleChallengeCompleted(targetPackage: String, targetAppName: String) {
        serviceScope.launch(Dispatchers.IO) {
            val expiresAt = System.currentTimeMillis() + (15 * 60 * 1000L)
            database.monitoredAppDao().setTemporaryUnlock(targetPackage, true, expiresAt)
            database.blockEventDao().logEvent(
                BlockEventLog(
                    packageName = targetPackage,
                    appName = targetAppName,
                    toughQuote = "Superó la Tarea de Monotonía desde Overlay. Ventana de 15 minutos concedida.",
                    challengeAttempted = "Tarea de Monotonía (Overlay)",
                    challengeSucceeded = true
                )
            )
            launch(Dispatchers.Main) {
                removeOverlay()
                stopSelf()
            }
        }
    }

    private fun handleCancelAndGoHome() {
        removeOverlay()
        // Force navigate to Android Home launcher to prevent returning to the blocked app
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        stopSelf()
    }

    private fun removeOverlay() {
        try {
            if (composeOverlayView != null && composeOverlayView?.isAttachedToWindow == true) {
                windowManager?.removeView(composeOverlayView)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error removing overlay view: ${e.message}")
        } finally {
            composeOverlayView = null
        }
    }

    override fun onDestroy() {
        removeOverlay()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
