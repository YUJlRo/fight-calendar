package com.fightcalendar.app.data.repository

import com.fightcalendar.app.domain.model.*
import com.fightcalendar.app.domain.repository.DayDataRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 日次データリポジトリ実装（モックデータ使用）
 */
@Singleton
class DayDataRepositoryImpl @Inject constructor() : DayDataRepository {
    
    override suspend fun getDayData(date: LocalDate): DayData {
        delay(500) // データ取得のシミュレーション
        
        // モックデータを生成
        val hourlyData = generateMockHourlyData()
        val freeTimeSlots = generateMockFreeTimeSlots()
        val categoryUsage = generateMockCategoryUsage()
        
        val totalScreenTimeMs = categoryUsage.values.sum()
        val productiveTimeMs = categoryUsage.filterKeys { it.isProductive }.values.sum()
        val winRate = if (totalScreenTimeMs > 0) {
            ((productiveTimeMs.toDouble() / totalScreenTimeMs) * 100).toInt()
        } else {
            0
        }
        
        return DayData(
            date = date,
            winRate = winRate,
            totalScreenTimeMs = totalScreenTimeMs,
            streakDays = 3, // モック値
            hourlyData = hourlyData,
            freeTimeSlots = freeTimeSlots,
            categoryUsage = categoryUsage
        )
    }
    
    override fun observeDayData(date: LocalDate): Flow<DayData> = flow {
        while (true) {
            emit(getDayData(date))
            delay(300000) // 5分間隔で更新
        }
    }
    
    override suspend fun blockFreeTimeSlot(
        date: LocalDate,
        slot: FreeTimeSlot,
        title: String
    ): Boolean {
        delay(1000) // カレンダー操作のシミュレーション
        // TODO: 実際のCalendar Provider実装
        return true
    }
    
    override suspend fun refreshData(date: LocalDate) {
        delay(2000) // データ更新のシミュレーション
        // TODO: UsageStatsManagerからデータ取得
    }
    
    override suspend fun calculateWinRate(date: LocalDate): Int {
        val dayData = getDayData(date)
        return dayData.winRate
    }
    
    private fun generateMockHourlyData(): List<HourlyData> {
        return (0..23).map { hour ->
            val category = when (hour) {
                in 6..8 -> Category.WORK
                in 9..11 -> Category.STUDY
                in 13..17 -> Category.WORK
                in 19..21 -> Category.ENTERTAINMENT
                else -> null
            }
            
            HourlyData(
                hour = hour,
                topCategory = category,
                usageTimeMs = if (category != null) (30..60).random() * 60 * 1000L else 0L,
                hasEvents = hour in listOf(9, 14, 16),
                hasFreeTime = hour in listOf(10, 15, 18),
                apps = if (category != null) generateMockAppUsage(category) else emptyList()
            )
        }
    }
    
    private fun generateMockAppUsage(category: Category): List<AppUsage> {
        val apps = when (category) {
            Category.WORK -> listOf("com.slack", "com.zoom", "com.microsoft.office")
            Category.STUDY -> listOf("com.duolingo", "com.coursera", "com.kindle")
            Category.ENTERTAINMENT -> listOf("com.instagram", "com.tiktok", "com.youtube")
            Category.TOOLS -> listOf("com.calculator", "com.calendar", "com.camera")
        }
        
        return apps.take(2).mapIndexed { index, packageName ->
            AppUsage(
                packageName = packageName,
                label = packageName.split(".").last().capitalize(),
                category = category,
                usageTimeMs = ((30 - index * 10)..(45 - index * 10)).random() * 60 * 1000L
            )
        }
    }
    
    private fun generateMockFreeTimeSlots(): List<FreeTimeSlot> {
        return listOf(
            FreeTimeSlot(
                startTime = LocalTime.of(10, 0),
                endTime = LocalTime.of(11, 30),
                durationMinutes = 90
            ),
            FreeTimeSlot(
                startTime = LocalTime.of(15, 30),
                endTime = LocalTime.of(17, 0),
                durationMinutes = 90
            ),
            FreeTimeSlot(
                startTime = LocalTime.of(18, 0),
                endTime = LocalTime.of(19, 0),
                durationMinutes = 60
            )
        )
    }
    
    private fun generateMockCategoryUsage(): Map<Category, Long> {
        return mapOf(
            Category.WORK to 4 * 60 * 60 * 1000L, // 4時間
            Category.STUDY to 2 * 60 * 60 * 1000L, // 2時間
            Category.ENTERTAINMENT to 1 * 60 * 60 * 1000L, // 1時間
            Category.TOOLS to 30 * 60 * 1000L // 30分
        )
    }
}