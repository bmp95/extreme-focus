package app.extremefocus.domain

/**
 * What the app is currently allowed to do. Mirrors the system state, re-read on every resume.
 */
data class PermissionsState(
    val hasUsageStats: Boolean = false,
    val isAccessibilityEnabled: Boolean = false,
    val hasOverlay: Boolean = false,
    val hasNotificationAccess: Boolean = false,
    val canPostNotifications: Boolean = false,
    val isDeviceAdminActive: Boolean = false,
    val isBatteryUnrestricted: Boolean = false
)

enum class SetupTier { ESSENTIAL, SHIELD }

/**
 * Android grants none of these in bulk: each one is a separate trip into Settings. Asking for
 * all of them up front is what makes people abandon setup, so only the two that blocking
 * genuinely requires are ESSENTIAL; everything else is offered afterwards, as reinforcement.
 */
enum class SetupStep(val tier: SetupTier) {
    USAGE_ACCESS(SetupTier.ESSENTIAL),
    ACCESSIBILITY(SetupTier.ESSENTIAL),

    BATTERY_UNRESTRICTED(SetupTier.SHIELD),
    AUTOSTART(SetupTier.SHIELD),
    POST_NOTIFICATIONS(SetupTier.SHIELD),
    OVERLAY(SetupTier.SHIELD),
    NOTIFICATION_LISTENER(SetupTier.SHIELD),
    DEVICE_ADMIN(SetupTier.SHIELD)
}

object SetupPlan {

    /** Blocking works with these two alone; the app is usable the moment they are granted. */
    fun canBlock(state: PermissionsState): Boolean =
        state.hasUsageStats && state.isAccessibilityEnabled

    fun isGranted(step: SetupStep, state: PermissionsState): Boolean = when (step) {
        SetupStep.USAGE_ACCESS -> state.hasUsageStats
        SetupStep.ACCESSIBILITY -> state.isAccessibilityEnabled
        SetupStep.BATTERY_UNRESTRICTED -> state.isBatteryUnrestricted
        SetupStep.OVERLAY -> state.hasOverlay
        SetupStep.POST_NOTIFICATIONS -> state.canPostNotifications
        SetupStep.NOTIFICATION_LISTENER -> state.hasNotificationAccess
        SetupStep.DEVICE_ADMIN -> state.isDeviceAdminActive
        // Xiaomi exposes no API to read it, so it is never reported as done automatically.
        SetupStep.AUTOSTART -> false
    }

    fun steps(includeAutostart: Boolean): List<SetupStep> =
        SetupStep.entries.filter { includeAutostart || it != SetupStep.AUTOSTART }

    fun pending(state: PermissionsState, includeAutostart: Boolean): List<SetupStep> =
        steps(includeAutostart).filterNot { isGranted(it, state) }

    /**
     * The single step the wizard should put in front of the user next: essentials first, and
     * only then reinforcement, so nobody is asked to harden an app they haven't tried yet.
     */
    fun nextStep(state: PermissionsState, includeAutostart: Boolean): SetupStep? =
        pending(state, includeAutostart).minByOrNull { it.tier.ordinal }

    fun grantedCount(state: PermissionsState, tier: SetupTier, includeAutostart: Boolean): Int =
        steps(includeAutostart).filter { it.tier == tier }.count { isGranted(it, state) }

    fun totalCount(tier: SetupTier, includeAutostart: Boolean): Int =
        steps(includeAutostart).count { it.tier == tier }
}
