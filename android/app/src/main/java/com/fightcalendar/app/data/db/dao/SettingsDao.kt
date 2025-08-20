package com.fightcalendar.app.data.db.dao

import androidx.room.*
import com.fightcalendar.app.data.db.entities.SettingsEntity
import kotlinx.coroutines.flow.Flow

/**
 * 設定DAO
 */
@Dao
interface SettingsDao {

    /**
     * 設定を取得（Flow）
     */
    @Query("SELECT * FROM settings WHERE id = 1")
    fun getSettings(): Flow<SettingsEntity?>

    /**
     * 設定を取得（一回限り）
     */
    @Query("SELECT * FROM settings WHERE id = 1")
    suspend fun getSettingsOnce(): SettingsEntity?

    /**
     * 設定を挿入または更新
     */
    @Upsert
    suspend fun upsertSettings(settings: SettingsEntity)

    /**
     * 稼働時間を更新
     */
    @Query("""
        UPDATE settings 
        SET workingStartHour = :startHour, workingStartMinute = :startMinute,
            workingEndHour = :endHour, workingEndMinute = :endMinute,
            updatedAt = :updatedAt
        WHERE id = 1
    """)
    suspend fun updateWorkingHours(
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        updatedAt: Long = System.currentTimeMillis()
    )

    /**
     * 睡眠時間を更新
     */
    @Query("""
        UPDATE settings 
        SET sleepStartHour = :startHour, sleepStartMinute = :startMinute,
            sleepEndHour = :endHour, sleepEndMinute = :endMinute,
            updatedAt = :updatedAt
        WHERE id = 1
    """)
    suspend fun updateSleepHours(
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        updatedAt: Long = System.currentTimeMillis()
    )

    /**
     * カレンダー設定を更新
     */
    @Query("""
        UPDATE settings 
        SET selectedCalendarId = :calendarId, selectedCalendarName = :calendarName,
            updatedAt = :updatedAt
        WHERE id = 1
    """)
    suspend fun updateCalendarSettings(
        calendarId: Long,
        calendarName: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    /**
     * タイムライン設定を更新
     */
    @Query("""
        UPDATE settings 
        SET timelineEnabled = :enabled, timelineThresholdMinutes = :thresholdMinutes,
            updatedAt = :updatedAt
        WHERE id = 1
    """)
    suspend fun updateTimelineSettings(
        enabled: Boolean,
        thresholdMinutes: Int,
        updatedAt: Long = System.currentTimeMillis()
    )

    /**
     * 勝率閾値を更新
     */
    @Query("""
        UPDATE settings 
        SET winRateThreshold = :threshold, updatedAt = :updatedAt
        WHERE id = 1
    """)
    suspend fun updateWinRateThreshold(
        threshold: Float,
        updatedAt: Long = System.currentTimeMillis()
    )

    /**
     * オンボーディング完了を更新
     */
    @Query("""
        UPDATE settings 
        SET isOnboardingCompleted = :completed, updatedAt = :updatedAt
        WHERE id = 1
    """)
    suspend fun updateOnboardingCompleted(
        completed: Boolean,
        updatedAt: Long = System.currentTimeMillis()
    )

    /**
     * 権限状態を更新
     */
    @Query("""
        UPDATE settings 
        SET hasUsagePermission = :usagePermission,
            hasCalendarPermission = :calendarPermission,
            hasNotificationPermission = :notificationPermission,
            updatedAt = :updatedAt
        WHERE id = 1
    """)
    suspend fun updatePermissions(
        usagePermission: Boolean,
        calendarPermission: Boolean,
        notificationPermission: Boolean,
        updatedAt: Long = System.currentTimeMillis()
    )

    /**
     * 使用統計権限を更新
     */
    @Query("""
        UPDATE settings 
        SET hasUsagePermission = :hasPermission, updatedAt = :updatedAt
        WHERE id = 1
    """)
    suspend fun updateUsagePermission(
        hasPermission: Boolean,
        updatedAt: Long = System.currentTimeMillis()
    )

    /**
     * カレンダー権限を更新
     */
    @Query("""
        UPDATE settings 
        SET hasCalendarPermission = :hasPermission, updatedAt = :updatedAt
        WHERE id = 1
    """)
    suspend fun updateCalendarPermission(
        hasPermission: Boolean,
        updatedAt: Long = System.currentTimeMillis()
    )

    /**
     * 通知権限を更新
     */
    @Query("""
        UPDATE settings 
        SET hasNotificationPermission = :hasPermission, updatedAt = :updatedAt
        WHERE id = 1
    """)
    suspend fun updateNotificationPermission(
        hasPermission: Boolean,
        updatedAt: Long = System.currentTimeMillis()
    )

    /**
     * 設定をリセット（初期値に戻す）
     */
    @Query("DELETE FROM settings WHERE id = 1")
    suspend fun resetSettings()
}