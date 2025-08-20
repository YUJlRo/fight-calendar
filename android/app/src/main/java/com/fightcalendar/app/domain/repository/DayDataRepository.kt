package com.fightcalendar.app.domain.repository

import com.fightcalendar.app.domain.model.DayData
import com.fightcalendar.app.domain.model.FreeTimeSlot
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * 日次データのリポジトリ
 */
interface DayDataRepository {
    
    /**
     * 指定日の日次データを取得
     */
    suspend fun getDayData(date: LocalDate): DayData
    
    /**
     * 指定日の日次データを監視
     */
    fun observeDayData(date: LocalDate): Flow<DayData>
    
    /**
     * 空き時間スロットをブロック（カレンダーにイベント作成）
     */
    suspend fun blockFreeTimeSlot(
        date: LocalDate,
        slot: FreeTimeSlot,
        title: String = "集中時間"
    ): Boolean
    
    /**
     * データを更新（UsageStatsから）
     */
    suspend fun refreshData(date: LocalDate)
    
    /**
     * 勝率を計算
     */
    suspend fun calculateWinRate(date: LocalDate): Int
}