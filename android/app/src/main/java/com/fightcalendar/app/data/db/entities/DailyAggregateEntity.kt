package com.fightcalendar.app.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

/**
 * 日次集計データエンティティ
 */
@Entity(tableName = "daily_aggregates")
data class DailyAggregateEntity(
    @PrimaryKey
    val localDate: String, // YYYY-MM-DD 形式
    val workMinutes: Int = 0,          // 仕事カテゴリの使用時間（分）
    val studyMinutes: Int = 0,         // 学習カテゴリの使用時間（分）
    val entertainmentMinutes: Int = 0, // 娯楽・SNSカテゴリの使用時間（分）
    val toolsMinutes: Int = 0,         // ツールカテゴリの使用時間（分）
    val totalMinutes: Int = 0,         // 総使用時間（分）
    val productiveMinutes: Int = 0,    // 生産的カテゴリ合計（分）
    val winRate: Float = 0f,           // 勝率（%）
    val freeMinutes: Int = 0,          // 空き時間（分）
    val streak: Int = 0,               // 連続達成日数
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * ローカル日付をLocalDateとして取得
     */
    fun getLocalDate(): LocalDate = LocalDate.parse(localDate)

    /**
     * 勝率を計算（生産時間 / 総時間 * 100）
     */
    fun calculateWinRate(workingMinutes: Int): Float {
        return if (workingMinutes > 0) {
            (productiveMinutes.toFloat() / workingMinutes.toFloat()) * 100f
        } else {
            0f
        }
    }

    /**
     * 総使用時間を時:分形式で取得
     */
    fun getTotalTimeFormatted(): String {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return if (hours > 0) {
            "${hours}時間${minutes}分"
        } else {
            "${minutes}分"
        }
    }

    /**
     * 生産時間を時:分形式で取得
     */
    fun getProductiveTimeFormatted(): String {
        val hours = productiveMinutes / 60
        val minutes = productiveMinutes % 60
        return if (hours > 0) {
            "${hours}時間${minutes}分"
        } else {
            "${minutes}分"
        }
    }

    /**
     * 空き時間を時:分形式で取得
     */
    fun getFreeTimeFormatted(): String {
        val hours = freeMinutes / 60
        val minutes = freeMinutes % 60
        return if (hours > 0) {
            "${hours}時間${minutes}分"
        } else {
            "${minutes}分"
        }
    }
}