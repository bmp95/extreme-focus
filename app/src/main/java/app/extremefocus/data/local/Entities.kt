package app.extremefocus.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monitored_apps")
data class MonitoredAppEntity(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val dailyLimitMinutes: Int = 15,
    val isBlocked: Boolean = true,
    val currentUsageMinutes: Int = 0,
    val isTemporaryUnlocked: Boolean = false,
    val temporaryUnlockExpiresAt: Long = 0L,
    val timesBlockedToday: Int = 0,
    val iconCategory: String = "SOCIAL",
    val blockNotifications: Boolean = true, // True: notifications intercepted & wiped
    // What the user said they came to do, quoted back at them when the window runs out.
    val declaredIntent: String? = null
)

@Entity(tableName = "block_events")
data class BlockEventLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val packageName: String,
    val appName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val toughQuote: String,
    val challengeAttempted: String? = null,
    val challengeSucceeded: Boolean = false
)
