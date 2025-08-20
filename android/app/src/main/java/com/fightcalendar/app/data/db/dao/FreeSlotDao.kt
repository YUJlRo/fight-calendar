package com.fightcalendar.app.data.db.dao

import androidx.room.*
import com.fightcalendar.app.data.db.entities.FreeSlotEntity
import kotlinx.coroutines.flow.Flow

/**
 * 空き時間スロットDAO
 */
@Dao
interface FreeSlotDao {

    /**
     * 指定日の空きスロットを取得
     */
    @Query("SELECT * FROM free_slots WHERE localDate = :localDate ORDER BY startTimeMs")
    suspend fun getSlotsByDate(localDate: String): List<FreeSlotEntity>

    /**
     * 指定日の空きスロット（60分以上）を取得
     */
    @Query("""
        SELECT * FROM free_slots 
        WHERE localDate = :localDate AND durationMinutes >= 60
        ORDER BY startTimeMs
    """)
    suspend fun getAvailableSlotsByDate(localDate: String): List<FreeSlotEntity>

    /**
     * 今日の空きスロットを取得（Flow）
     */
    @Query("SELECT * FROM free_slots WHERE localDate = :today ORDER BY startTimeMs")
    fun getTodaySlots(today: String): Flow<List<FreeSlotEntity>>

    /**
     * 次の利用可能な空きスロットを取得（現在時刻以降、60分以上）
     */
    @Query("""
        SELECT * FROM free_slots 
        WHERE startTimeMs > :currentMs AND durationMinutes >= 60 AND isBlocked = 0
        ORDER BY startTimeMs 
        LIMIT 1
    """)
    suspend fun getNextAvailableSlot(currentMs: Long): FreeSlotEntity?

    /**
     * 指定日の空き時間合計を取得（分）
     */
    @Query("SELECT COALESCE(SUM(durationMinutes), 0) FROM free_slots WHERE localDate = :localDate")
    suspend fun getTotalFreeMinutes(localDate: String): Int

    /**
     * 指定日の利用可能な空き時間合計を取得（60分以上、未ブロック）
     */
    @Query("""
        SELECT COALESCE(SUM(durationMinutes), 0) FROM free_slots 
        WHERE localDate = :localDate AND durationMinutes >= 60 AND isBlocked = 0
    """)
    suspend fun getAvailableFreeMinutes(localDate: String): Int

    /**
     * 空きスロットを挿入
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlot(slot: FreeSlotEntity)

    /**
     * 複数の空きスロットを挿入
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlots(slots: List<FreeSlotEntity>)

    /**
     * 空きスロットを更新
     */
    @Update
    suspend fun updateSlot(slot: FreeSlotEntity)

    /**
     * 空きスロットを削除
     */
    @Delete
    suspend fun deleteSlot(slot: FreeSlotEntity)

    /**
     * 指定日の空きスロットをすべて削除
     */
    @Query("DELETE FROM free_slots WHERE localDate = :localDate")
    suspend fun deleteSlotsByDate(localDate: String)

    /**
     * 古い空きスロットを削除（指定日より前）
     */
    @Query("DELETE FROM free_slots WHERE localDate < :beforeDate")
    suspend fun deleteOldSlots(beforeDate: String)

    /**
     * 空きスロットをブロック状態に更新
     */
    @Query("UPDATE free_slots SET isBlocked = 1 WHERE id = :slotId")
    suspend fun blockSlot(slotId: Long)

    /**
     * 空きスロットのブロック解除
     */
    @Query("UPDATE free_slots SET isBlocked = 0 WHERE id = :slotId")
    suspend fun unblockSlot(slotId: Long)

    /**
     * 指定期間の空きスロット統計を取得
     */
    @Query("""
        SELECT 
            COUNT(*) as slotCount,
            SUM(durationMinutes) as totalMinutes,
            AVG(durationMinutes) as avgMinutes,
            MAX(durationMinutes) as maxMinutes
        FROM free_slots 
        WHERE localDate >= :startDate AND localDate <= :endDate
    """)
    suspend fun getSlotStats(startDate: String, endDate: String): SlotStats?

    /**
     * 進行中の空きスロットを取得
     */
    @Query("""
        SELECT * FROM free_slots 
        WHERE startTimeMs <= :currentMs AND endTimeMs >= :currentMs AND isBlocked = 0
        LIMIT 1
    """)
    suspend fun getCurrentFreeSlot(currentMs: Long): FreeSlotEntity?

    /**
     * 時間帯別の空きスロット分布を取得
     */
    @Query("""
        SELECT 
            startHour,
            COUNT(*) as slotCount,
            AVG(durationMinutes) as avgDuration
        FROM free_slots 
        WHERE localDate = :localDate
        GROUP BY startHour
        ORDER BY startHour
    """)
    suspend fun getHourlyFreeSlotDistribution(localDate: String): List<HourlySlotDistribution>
}

/**
 * 空きスロット統計
 */
data class SlotStats(
    val slotCount: Int,
    val totalMinutes: Int,
    val avgMinutes: Float,
    val maxMinutes: Int
)

/**
 * 時間別空きスロット分布
 */
data class HourlySlotDistribution(
    val startHour: Int,
    val slotCount: Int,
    val avgDuration: Float
)