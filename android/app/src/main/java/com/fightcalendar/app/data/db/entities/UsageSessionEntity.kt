package com.fightcalendar.app.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toLocalDateTime

/**
 * アプリ使用セッションエンティティ
 */
@Entity(tableName = "usage_sessions")
data class UsageSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val startTimeMs: Long, // エポック時間（ミリ秒）
    val endTimeMs: Long,   // エポック時間（ミリ秒）
    val durationMs: Long,  // 使用時間（ミリ秒）
    val localDate: String, // YYYY-MM-DD 形式のローカル日付
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * 使用時間を分単位で取得
     */
    val durationMinutes: Int
        get() = (durationMs / 60_000).toInt()

    /**
     * 使用時間を時間単位で取得
     */
    val durationHours: Double
        get() = durationMs / 3_600_000.0

    /**
     * ローカル日付をLocalDateとして取得
     */
    fun getLocalDate(): LocalDate = LocalDate.parse(localDate)

    /**
     * 開始時刻の時間（0-23）を取得
     */
    fun getStartHour(): Int {
        return kotlinx.datetime.Instant.fromEpochMilliseconds(startTimeMs)
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .hour
    }

    companion object {
        /**
         * エポック時間からローカル日付文字列を生成
         */
        fun formatLocalDate(epochMs: Long): String {
            return kotlinx.datetime.Instant.fromEpochMilliseconds(epochMs)
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
                .toString()
        }
    }
}