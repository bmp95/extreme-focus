package app.extremefocus.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SetupPlanTest {

    private val nothingGranted = PermissionsState()

    private val essentialsGranted = PermissionsState(
        hasUsageStats = true,
        isAccessibilityEnabled = true
    )

    @Test
    fun `blocking needs usage access and accessibility, nothing more`() {
        assertTrue(SetupPlan.canBlock(essentialsGranted))
    }

    @Test
    fun `blocking is impossible with usage access alone`() {
        assertFalse(SetupPlan.canBlock(PermissionsState(hasUsageStats = true)))
    }

    @Test
    fun `blocking is impossible with accessibility alone`() {
        assertFalse(SetupPlan.canBlock(PermissionsState(isAccessibilityEnabled = true)))
    }

    @Test
    fun `the wizard asks for an essential step before any reinforcement`() {
        val next = SetupPlan.nextStep(nothingGranted, includeAutostart = true)

        assertEquals(SetupTier.ESSENTIAL, next?.tier)
    }

    @Test
    fun `reinforcement is only offered once the essentials are done`() {
        val next = SetupPlan.nextStep(essentialsGranted, includeAutostart = true)

        assertEquals(SetupTier.SHIELD, next?.tier)
    }

    @Test
    fun `no step is proposed when everything is granted`() {
        val everything = PermissionsState(
            hasUsageStats = true,
            isAccessibilityEnabled = true,
            hasOverlay = true,
            hasNotificationAccess = true,
            canPostNotifications = true,
            isDeviceAdminActive = true,
            isBatteryUnrestricted = true
        )

        assertNull(SetupPlan.nextStep(everything, includeAutostart = false))
    }

    @Test
    fun `autostart is hidden on devices that do not have it`() {
        val steps = SetupPlan.steps(includeAutostart = false)

        assertFalse(steps.contains(SetupStep.AUTOSTART))
    }

    @Test
    fun `only two steps are essential, so setup can finish in two trips`() {
        assertEquals(2, SetupPlan.totalCount(SetupTier.ESSENTIAL, includeAutostart = true))
    }

    @Test
    fun `progress reflects what the user has actually granted`() {
        assertEquals(0, SetupPlan.grantedCount(nothingGranted, SetupTier.ESSENTIAL, true))
        assertEquals(2, SetupPlan.grantedCount(essentialsGranted, SetupTier.ESSENTIAL, true))
    }
}
