package com.fightcalendar.app.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * 空き時間スロットエンティティ
 */
@Entity(tableName = "free_slots")
data class FreeSlotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val localDate: String,      // YYYY-MM-DD 形式
    val startTimeMs: Long,      // 開始時刻（エポック時間）
    val endTimeMs: Long,        // 終了時刻（エポック時間）
    val durationMinutes: Int,   // 時間（分）
    val startHour: Int,         // 開始時間（0-23）
    val endHour: Int,           // 終了時間（0-23）
    val isBlocked: Boolean = false, // フォーカス予定でブロック済みか
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * ローカル日付をLocalDateとして取得
     */
    fun getLocalDate(): LocalDate = LocalDate.parse(localDate)

    /**
     * 開始時刻を HH:MM 形式で取得
     */
    fun getStartTimeFormatted(): String {
        val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(startTimeMs)
        val localTime = instant.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).time
        return "${localTime.hour.toString().padStart(2, '0')}:${localTime.minute.toString().padStart(2, '0')}"
    }

    /**
     * 終了時刻を HH:MM 形式で取得
     */
    fun getEndTimeFormatted(): String {
        val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(endTimeMs)
        val localTime = instant.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).time
        return "${localTime.hour.toString().padStart(2, '0')}:${localTime.minute.toString().padStart(2, '0')}"
    }

    /**
     * 期間を「HH:MM - HH:MM」形式で取得
     */
    fun getTimeRangeFormatted(): String {
        return "${getStartTimeFormatted()} - ${getEndTimeFormatted()}"
    }

    /**
     * 時間を時:分形式で取得
     */
    fun getDurationFormatted(): String {
        val hours = durationMinutes / 60
        val minutes = durationMinutes % 60
        return if (hours > 0) {
            "${hours}時間${minutes}分"
        } else {
            "${minutes}分"
        }
    }

    /**
     * 現在時刻より後かどうか判定
     */
    fun isUpcoming(): Boolean {
        return startTimeMs > System.currentTimeMillis()
    }

    /**
     * 現在進行中かどうか判定
     */
    fun isOngoing(): Boolean {
        val now = System.currentTimeMillis()
        return startTimeMs <= now && now <= endTimeMs
    }
}