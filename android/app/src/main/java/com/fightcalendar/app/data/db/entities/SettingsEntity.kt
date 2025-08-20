package com.fightcalendar.app.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 設定エンティティ（シングルトン）
 */
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    val id: Int = 1, // シングルトン用固定ID
    
    // 稼働時間設定
    val workingStartHour: Int = 6,   // 稼働開始時間（0-23）
    val workingStartMinute: Int = 0, // 稼働開始分（0-59）
    val workingEndHour: Int = 24,    // 稼働終了時間（0-24）
    val workingEndMinute: Int = 0,   // 稼働終了分（0-59）
    
    // 睡眠時間設定
    val sleepStartHour: Int = 0,     // 睡眠開始時間（0-23）
    val sleepStartMinute: Int = 0,   // 睡眠開始分（0-59）
    val sleepEndHour: Int = 6,       // 睡眠終了時間（0-24）
    val sleepEndMinute: Int = 0,     // 睡眠終了分（0-59）
    
    // カレンダー設定
    val selectedCalendarId: Long = -1L,    // 選択されたカレンダーID
    val selectedCalendarName: String = "", // 選択されたカレンダー名
    val timelineEnabled: Boolean = false,  // タイムライン同期有効
    val timelineThresholdMinutes: Int = 10, // タイムライン作成閾値（分）
    
    // ゲーミフィケーション設定
    val winRateThreshold: Float = 50f, // 勝率達成閾値（%）
    
    // オンボーディング状態
    val isOnboardingCompleted: Boolean = false,
    val hasUsagePermission: Boolean = false,
    val hasCalendarPermission: Boolean = false,
    val hasNotificationPermission: Boolean = false,
    
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * 稼働時間（分）を取得
     */
    fun getWorkingMinutes(): Int {
        val startMinutes = workingStartHour * 60 + workingStartMinute
        val endMinutes = workingEndHour * 60 + workingEndMinute
        return if (endMinutes > startMinutes) {
            endMinutes - startMinutes
        } else {
            // 翌日にまたがる場合
            (24 * 60) - startMinutes + endMinutes
        }
    }

    /**
     * 睡眠時間（分）を取得
     */
    fun getSleepMinutes(): Int {
        val startMinutes = sleepStartHour * 60 + sleepStartMinute
        val endMinutes = sleepEndHour * 60 + sleepEndMinute
        return if (endMinutes > startMinutes) {
            endMinutes - startMinutes
        } else {
            // 翌日にまたがる場合
            (24 * 60) - startMinutes + endMinutes
        }
    }

    /**
     * 稼働時間範囲を「HH:MM - HH:MM」形式で取得
     */
    fun getWorkingTimeRange(): String {
        val start = "${workingStartHour.toString().padStart(2, '0')}:${workingStartMinute.toString().padStart(2, '0')}"
        val end = "${workingEndHour.toString().padStart(2, '0')}:${workingEndMinute.toString().padStart(2, '0')}"
        return "$start - $end"
    }

    /**
     * 睡眠時間範囲を「HH:MM - HH:MM」形式で取得
     */
    fun getSleepTimeRange(): String {
        val start = "${sleepStartHour.toString().padStart(2, '0')}:${sleepStartMinute.toString().padStart(2, '0')}"
        val end = "${sleepEndHour.toString().padStart(2, '0')}:${sleepEndMinute.toString().padStart(2, '0')}"
        return "$start - $end"
    }

    /**
     * 全ての必要な権限が付与されているか確認
     */
    fun hasAllPermissions(): Boolean {
        return hasUsagePermission && hasCalendarPermission && hasNotificationPermission
    }

    /**
     * カレンダーが設定されているか確認
     */
    fun hasCalendarSelected(): Boolean {
        return selectedCalendarId != -1L && selectedCalendarName.isNotEmpty()
    }
}