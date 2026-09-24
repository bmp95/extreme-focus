package app.extremefocus.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultMonitoredAppsTest {

    @Test
    fun `only seeds apps that are installed on the device`() {
        val installed = setOf("com.instagram.android", "com.google.android.youtube")

        val seeded = DefaultMonitoredApps.seedFor(installed)

        assertEquals(installed, seeded.map { it.packageName }.toSet())
    }

    @Test
    fun `seeds nothing when none of the suggestions are installed`() {
        val seeded = DefaultMonitoredApps.seedFor(setOf("com.some.unrelated.app"))

        assertTrue(seeded.isEmpty())
    }

    @Test
    fun `never reports usage the user has not actually made`() {
        val everySuggestion = DefaultMonitoredApps.suggestions.map { it.packageName }.toSet()

        val seeded = DefaultMonitoredApps.seedFor(everySuggestion)

        assertTrue(seeded.all { it.currentUsageMinutes == 0 })
    }

    @Test
    fun `seeded apps start blocked with a positive daily limit`() {
        val everySuggestion = DefaultMonitoredApps.suggestions.map { it.packageName }.toSet()

        val seeded = DefaultMonitoredApps.seedFor(everySuggestion)

        assertTrue(seeded.all { it.isBlocked })
        assertTrue(seeded.all { it.dailyLimitMinutes > 0 })
    }
}
