package com.fightcalendar.app.data.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.fightcalendar.app.data.db.dao.*
import com.fightcalendar.app.data.db.entities.*

/**
 * ファイトカレンダーメインデータベース
 */
@Database(
    entities = [
        AppEntity::class,
        UsageSessionEntity::class,
        DailyAggregateEntity::class,
        FreeSlotEntity::class,
        SettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appDao(): AppDao
    abstract fun usageSessionDao(): UsageSessionDao
    abstract fun dailyAggregateDao(): DailyAggregateDao
    abstract fun freeSlotDao(): FreeSlotDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        const val DATABASE_NAME = "fight_calendar_db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

/**
 * Room用の型変換器
 */
class Converters {
    // 将来的に必要になった場合の型変換をここに追加
}