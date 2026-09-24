package app.extremefocus.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [MonitoredAppEntity::class, BlockEventLog::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun monitoredAppDao(): MonitoredAppDao
    abstract fun blockEventDao(): BlockEventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // No destructive fallback: a schema change must ship a written migration
                // rather than silently wiping the user's limits and history.
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "extreme_focus.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
