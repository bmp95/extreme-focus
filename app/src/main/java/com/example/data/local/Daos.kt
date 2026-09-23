package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MonitoredAppDao {
    @Query("SELECT * FROM monitored_apps ORDER BY currentUsageMinutes DESC")
    fun getAllMonitoredAppsFlow(): Flow<List<MonitoredAppEntity>>

    @Query("SELECT * FROM monitored_apps")
    suspend fun getAllMonitoredApps(): List<MonitoredAppEntity>

    @Query("SELECT * FROM monitored_apps WHERE packageName = :packageName LIMIT 1")
    suspend fun getAppByPackage(packageName: String): MonitoredAppEntity?

    @Query("SELECT * FROM monitored_apps WHERE packageName = :packageName LIMIT 1")
    fun getAppByPackageFlow(packageName: String): Flow<MonitoredAppEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateApp(app: MonitoredAppEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(apps: List<MonitoredAppEntity>)

    @Update
    suspend fun updateApp(app: MonitoredAppEntity)

    @Query("UPDATE monitored_apps SET currentUsageMinutes = :usage WHERE packageName = :packageName")
    suspend fun updateUsage(packageName: String, usage: Int)

    @Query("UPDATE monitored_apps SET isTemporaryUnlocked = :unlocked, temporaryUnlockExpiresAt = :expiresAt WHERE packageName = :packageName")
    suspend fun setTemporaryUnlock(packageName: String, unlocked: Boolean, expiresAt: Long)

    @Query("UPDATE monitored_apps SET timesBlockedToday = timesBlockedToday + 1 WHERE packageName = :packageName")
    suspend fun incrementBlockedCount(packageName: String)

    @Query("UPDATE monitored_apps SET blockNotifications = :blockNotif WHERE packageName = :packageName")
    suspend fun updateNotificationBlock(packageName: String, blockNotif: Boolean)

    @Query("DELETE FROM monitored_apps WHERE packageName = :packageName")
    suspend fun deleteApp(packageName: String)

    @Query("SELECT COUNT(*) FROM monitored_apps")
    suspend fun getCount(): Int
}

@Dao
interface BlockEventDao {
    @Query("SELECT * FROM block_events ORDER BY timestamp DESC LIMIT 50")
    fun getRecentBlockEventsFlow(): Flow<List<BlockEventLog>>

    @Insert
    suspend fun logEvent(event: BlockEventLog)

    @Query("DELETE FROM block_events")
    suspend fun clearLogs()
}
