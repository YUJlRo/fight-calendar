package com.fightcalendar.app.domain.model

import java.time.LocalDate
import java.time.LocalTime

/**
 * 日次データ
 */
data class DayData(
    val date: LocalDate,
    val winRate: Int,
    val totalScreenTimeMs: Long,
    val streakDays: Int,
    val hourlyData: List<HourlyData>,
    val freeTimeSlots: List<FreeTimeSlot>,
    val categoryUsage: Map<Category, Long>
) {
    fun getTotalScreenTimeFormatted(): String {
        val hours = totalScreenTimeMs / 3600000
        val minutes = (totalScreenTimeMs % 3600000) / 60000
        return "${hours}時間${minutes}分"
    }
}

/**
 * 時間別データ
 */
data class HourlyData(
    val hour: Int, // 0-23
    val topCategory: Category?,
    val usageTimeMs: Long,
    val hasEvents: Boolean,
    val hasFreeTime: Boolean,
    val apps: List<AppUsage> = emptyList()
)

/**
 * アプリ使用データ
 */
data class AppUsage(
    val packageName: String,
    val label: String,
    val category: Category,
    val usageTimeMs: Long
)

/**
 * 空き時間スロット
 */
data class FreeTimeSlot(
    val startTime: LocalTime,
    val endTime: LocalTime,
    val durationMinutes: Int
) {
    fun getFormattedTimeRange(): String {
        return "${startTime.hour.toString().padStart(2, '0')}:${startTime.minute.toString().padStart(2, '0')}–${endTime.hour.toString().padStart(2, '0')}:${endTime.minute.toString().padStart(2, '0')}"
    }
    
    fun getFormattedDuration(): String {
        val hours = durationMinutes / 60
        val minutes = durationMinutes % 60
        return if (hours > 0) {
            "${hours}時間${minutes}分"
        } else {
            "${minutes}分"
        }
    }
}