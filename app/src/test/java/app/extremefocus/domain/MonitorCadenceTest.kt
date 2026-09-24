package app.extremefocus.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MonitorCadenceTest {

    @Test
    fun `polls tightly while a watched app is on screen`() {
        val interval = MonitorCadence.intervalMs(isScreenOn = true, isWatchedAppInForeground = true)

        assertEquals(MonitorCadence.WATCHING_MS, interval)
    }

    @Test
    fun `backs off when the screen is on but no watched app is in use`() {
        val interval = MonitorCadence.intervalMs(isScreenOn = true, isWatchedAppInForeground = false)

        assertEquals(MonitorCadence.IDLE_MS, interval)
    }

    @Test
    fun `barely polls with the screen off, where there is nothing to intercept`() {
        val interval = MonitorCadence.intervalMs(isScreenOn = false, isWatchedAppInForeground = false)

        assertEquals(MonitorCadence.SCREEN_OFF_MS, interval)
    }

    @Test
    fun `screen off wins even if a watched app was last in the foreground`() {
        val interval = MonitorCadence.intervalMs(isScreenOn = false, isWatchedAppInForeground = true)

        assertEquals(MonitorCadence.SCREEN_OFF_MS, interval)
    }

    @Test
    fun `each step back is a real reduction in polling`() {
        assertTrue(MonitorCadence.WATCHING_MS < MonitorCadence.IDLE_MS)
        assertTrue(MonitorCadence.IDLE_MS < MonitorCadence.SCREEN_OFF_MS)
    }
}
