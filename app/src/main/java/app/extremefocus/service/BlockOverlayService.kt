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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import app.extremefocus.data.local.AppDatabase
import app.extremefocus.data.local.BlockEventLog
import app.extremefocus.ui.screens.BlockInterceptionScreen
import app.extremefocus.ui.screens.ChallengeType
import app.extremefocus.ui.screens.FrictionChallengeScreen
import app.extremefocus.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Draws the block over the app that was just opened.
 *
 * Launching an activity from the background is routinely dropped by the system, which left the
 * user bounced to the home screen with no explanation and no way to earn their way back in. An
 * overlay is shown immediately and on top of the offending app, so the block is always seen.
 */
class BlockOverlayService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private var windowManager: WindowManager? = null
    private var overlayView: ComposeView? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var database: AppDatabase

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
        when (intent?.action) {
            ACTION_DISMISS -> {
                removeOverlay()
                stopSelf()
            }
            ACTION_SHOW -> {
                val pkg = intent.getStringExtra(EXTRA_PACKAGE_NAME).orEmpty()
                val name = intent.getStringExtra(EXTRA_APP_NAME) ?: "App"
                val minutes = intent.getIntExtra(EXTRA_MINUTES_SPENT, 0)
                if (pkg.isNotEmpty()) showOverlay(pkg, name, minutes)
            }
        }
        return START_NOT_STICKY
    }

    private fun showOverlay(targetPackage: String, targetAppName: String, minutesSpent: Int) {
        if (!Settings.canDrawOverlays(this)) {
            Log.e(TAG, "SYSTEM_ALERT_WINDOW missing, cannot show block overlay")
            stopSelf()
            return
        }
        removeOverlay()

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
            PixelFormat.OPAQUE
        ).apply {
            gravity = Gravity.CENTER
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        }

        val view = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@BlockOverlayService)
            setViewTreeViewModelStoreOwner(this@BlockOverlayService)
            setViewTreeSavedStateRegistryOwner(this@BlockOverlayService)

            setContent {
                MyApplicationTheme {
                    var challenge by remember { mutableStateOf<ChallengeType?>(null) }
                    val active = challenge

                    if (active == null) {
                        BlockInterceptionScreen(
                            packageName = targetPackage,
                            appName = targetAppName,
                            minutesSpent = minutesSpent,
                            onCloseToHome = { goHome() },
                            onStartChallenge = { challenge = it }
                        )
                    } else {
                        FrictionChallengeScreen(
                            packageName = targetPackage,
                            appName = targetAppName,
                            challengeType = active,
                            onChallengeCompleted = { minutes, declaredIntent ->
                                grantWindow(targetPackage, targetAppName, minutes, declaredIntent, active)
                            },
                            onCancel = { challenge = null }
                        )
                    }
                }
            }
        }

        try {
            windowManager?.addView(view, params)
            overlayView = view
            Log.d(TAG, "Block overlay attached over $targetPackage")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to attach block overlay: ${e.message}", e)
            stopSelf()
        }
    }

    private fun grantWindow(
        targetPackage: String,
        targetAppName: String,
        minutes: Int,
        declaredIntent: String?,
        challenge: ChallengeType
    ) {
        serviceScope.launch(Dispatchers.IO) {
            val expiresAt = System.currentTimeMillis() + minutes * 60 * 1000L
            database.monitoredAppDao().setTemporaryUnlock(targetPackage, true, expiresAt)
            database.monitoredAppDao().setDeclaredIntent(targetPackage, declaredIntent)
            database.blockEventDao().logEvent(
                BlockEventLog(
                    packageName = targetPackage,
                    appName = targetAppName,
                    toughQuote = declaredIntent?.let { "Declaró: \"$it\"" }
                        ?: "Superó la prueba de fricción. Ventana de $minutes min.",
                    challengeAttempted = challenge.name,
                    challengeSucceeded = true
                )
            )
            // The window is granted, so the next block after it expires must not be debounced away.
            BlockPresenter.release()
            launch(Dispatchers.Main) {
                removeOverlay()
                stopSelf()
            }
        }
    }

    private fun goHome() {
        removeOverlay()
        startActivity(
            Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        )
        stopSelf()
    }

    private fun removeOverlay() {
        try {
            overlayView?.takeIf { it.isAttachedToWindow }?.let { windowManager?.removeView(it) }
        } catch (e: Exception) {
            Log.w(TAG, "Error removing block overlay: ${e.message}")
        } finally {
            overlayView = null
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

    companion object {
        private const val TAG = "BlockOverlayService"
        const val ACTION_SHOW = "app.extremefocus.service.ACTION_SHOW_BLOCK_OVERLAY"
        const val ACTION_DISMISS = "app.extremefocus.service.ACTION_DISMISS_BLOCK_OVERLAY"
        const val EXTRA_PACKAGE_NAME = "EXTRA_OVERLAY_PACKAGE_NAME"
        const val EXTRA_APP_NAME = "EXTRA_OVERLAY_APP_NAME"
        const val EXTRA_MINUTES_SPENT = "EXTRA_OVERLAY_MINUTES_SPENT"

        fun show(context: Context, packageName: String, appName: String, minutesSpent: Int) {
            if (!Settings.canDrawOverlays(context)) return
            context.startService(
                Intent(context, BlockOverlayService::class.java).apply {
                    action = ACTION_SHOW
                    putExtra(EXTRA_PACKAGE_NAME, packageName)
                    putExtra(EXTRA_APP_NAME, appName)
                    putExtra(EXTRA_MINUTES_SPENT, minutesSpent)
                }
            )
        }
    }
}
