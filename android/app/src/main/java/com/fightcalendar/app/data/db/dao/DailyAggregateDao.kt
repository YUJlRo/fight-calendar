package com.fightcalendar.app.data.db.dao

import androidx.room.*
import com.fightcalendar.app.data.db.entities.DailyAggregateEntity
import kotlinx.coroutines.flow.Flow

/**
 * 日次集計DAO
 */
@Dao
interface DailyAggregateDao {

    /**
     * 指定日の集計データを取得
     */
    @Query("SELECT * FROM daily_aggregates WHERE localDate = :localDate")
    suspend fun getAggregateByDate(localDate: String): DailyAggregateEntity?

    /**
     * 指定期間の集計データを取得
     */
    @Query("""
        SELECT * FROM daily_aggregates 
        WHERE localDate >= :startDate AND localDate <= :endDate 
        ORDER BY localDate DESC
    """)
    suspend fun getAggregatesByDateRange(startDate: String, endDate: String): List<DailyAggregateEntity>

    /**
     * 最新の集計データを取得（Flow）
     */
    @Query("SELECT * FROM daily_aggregates ORDER BY localDate DESC LIMIT 30")
    fun getRecentAggregates(): Flow<List<DailyAggregateEntity>>

    /**
     * 今日の集計データを取得（Flow）
     */
    @Query("SELECT * FROM daily_aggregates WHERE localDate = :today")
    fun getTodayAggregate(today: String): Flow<DailyAggregateEntity?>

    /**
     * 集計データを挿入または更新
     */
    @Upsert
    suspend fun upsertAggregate(aggregate: DailyAggregateEntity)

    /**
     * 複数の集計データを挿入または更新
     */
    @Upsert
    suspend fun upsertAggregates(aggregates: List<DailyAggregateEntity>)

    /**
     * 集計データを削除
     */
    @Delete
    suspend fun deleteAggregate(aggregate: DailyAggregateEntity)

    /**
     * 指定日の集計データを削除
     */
    @Query("DELETE FROM daily_aggregates WHERE localDate = :localDate")
    suspend fun deleteAggregateByDate(localDate: String)

    /**
     * 古い集計データを削除（指定日数より前）
     */
    @Query("DELETE FROM daily_aggregates WHERE localDate < :beforeDate")
    suspend fun deleteOldAggregates(beforeDate: String)

    /**
     * 週間統計を取得
     */
    @Query("""
        SELECT 
            AVG(winRate) as avgWinRate,
            SUM(totalMinutes) as totalMinutes,
            SUM(productiveMinutes) as productiveMinutes,
            AVG(freeMinutes) as avgFreeMinutes,
            COUNT(*) as dayCount
        FROM daily_aggregates 
        WHERE localDate >= :startDate AND localDate <= :endDate
    """)
    suspend fun getWeeklyStats(startDate: String, endDate: String): WeeklyStats?

    /**
     * 月間統計を取得
     */
    @Query("""
        SELECT 
            AVG(winRate) as avgWinRate,
            SUM(totalMinutes) as totalMinutes,
            SUM(productiveMinutes) as productiveMinutes,
            AVG(freeMinutes) as avgFreeMinutes,
            COUNT(*) as dayCount
        FROM daily_aggregates 
        WHERE substr(localDate, 1, 7) = :yearMonth
    """)
    suspend fun getMonthlyStats(yearMonth: String): WeeklyStats?

    /**
     * 勝率達成日数を取得
     */
    @Query("""
        SELECT COUNT(*) FROM daily_aggregates 
        WHERE winRate >= :threshold AND localDate >= :startDate AND localDate <= :endDate
    """)
    suspend fun getWinRateAchievementDays(threshold: Float, startDate: String, endDate: String): Int

    /**
     * 最高勝率を取得
     */
    @Query("SELECT MAX(winRate) FROM daily_aggregates WHERE localDate >= :startDate")
    suspend fun getMaxWinRate(startDate: String): Float?

    /**
     * 連続勝率達成日数を計算
     */
    @Query("""
        SELECT localDate, winRate FROM daily_aggregates 
        WHERE localDate <= :baseDate 
        ORDER BY localDate DESC 
        LIMIT 30
    """)
    suspend fun getRecentWinRates(baseDate: String): List<WinRateRecord>
}

/**
 * 週間・月間統計
 */
data class WeeklyStats(
    val avgWinRate: Float,
    val totalMinutes: Int,
    val productiveMinutes: Int,
    val avgFreeMinutes: Float,
    val dayCount: Int
)

/**
 * 勝率レコード
 */
data class WinRateRecord(
    val localDate: String,
    val winRate: Float
)