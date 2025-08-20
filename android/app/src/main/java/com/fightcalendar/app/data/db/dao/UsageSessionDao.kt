package com.fightcalendar.app.data.db.dao

import androidx.room.*
import com.fightcalendar.app.data.db.entities.UsageSessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * 使用セッションDAO
 */
@Dao
interface UsageSessionDao {

    /**
     * 指定日の使用セッションを取得
     */
    @Query("SELECT * FROM usage_sessions WHERE localDate = :localDate ORDER BY startTimeMs")
    suspend fun getSessionsByDate(localDate: String): List<UsageSessionEntity>

    /**
     * 指定期間の使用セッションを取得
     */
    @Query("""
        SELECT * FROM usage_sessions 
        WHERE startTimeMs >= :startMs AND endTimeMs <= :endMs 
        ORDER BY startTimeMs
    """)
    suspend fun getSessionsByTimeRange(startMs: Long, endMs: Long): List<UsageSessionEntity>

    /**
     * 指定日・パッケージの使用セッションを取得
     */
    @Query("""
        SELECT * FROM usage_sessions 
        WHERE localDate = :localDate AND packageName = :packageName 
        ORDER BY startTimeMs
    """)
    suspend fun getSessionsByDateAndPackage(localDate: String, packageName: String): List<UsageSessionEntity>

    /**
     * 最新の使用セッションを取得（Flow）
     */
    @Query("SELECT * FROM usage_sessions ORDER BY createdAt DESC LIMIT 50")
    fun getRecentSessions(): Flow<List<UsageSessionEntity>>

    /**
     * セッションを挿入
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: UsageSessionEntity)

    /**
     * 複数のセッションを挿入
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<UsageSessionEntity>)

    /**
     * セッションを更新
     */
    @Update
    suspend fun updateSession(session: UsageSessionEntity)

    /**
     * セッションを削除
     */
    @Delete
    suspend fun deleteSession(session: UsageSessionEntity)

    /**
     * 指定日のセッションをすべて削除
     */
    @Query("DELETE FROM usage_sessions WHERE localDate = :localDate")
    suspend fun deleteSessionsByDate(localDate: String)

    /**
     * 古いセッションを削除（指定日数より前）
     */
    @Query("DELETE FROM usage_sessions WHERE startTimeMs < :beforeMs")
    suspend fun deleteOldSessions(beforeMs: Long)

    /**
     * 指定日のパッケージ別使用時間を集計
     */
    @Query("""
        SELECT packageName, SUM(durationMs) as totalMs
        FROM usage_sessions 
        WHERE localDate = :localDate 
        GROUP BY packageName
        ORDER BY totalMs DESC
    """)
    suspend fun getPackageUsageSummary(localDate: String): List<PackageUsageSummary>

    /**
     * 指定日の時間別使用状況を取得
     */
    @Query("""
        SELECT 
            CAST(strftime('%H', datetime(startTimeMs/1000, 'unixepoch', 'localtime')) AS INTEGER) as hour,
            packageName,
            SUM(durationMs) as totalMs
        FROM usage_sessions 
        WHERE localDate = :localDate 
        GROUP BY hour, packageName
        ORDER BY hour, totalMs DESC
    """)
    suspend fun getHourlyUsage(localDate: String): List<HourlyUsage>

    /**
     * 総セッション数を取得
     */
    @Query("SELECT COUNT(*) FROM usage_sessions")
    suspend fun getSessionCount(): Int

    /**
     * 指定日の総使用時間を取得（分）
     */
    @Query("SELECT COALESCE(SUM(durationMs), 0) / 60000 FROM usage_sessions WHERE localDate = :localDate")
    suspend fun getTotalUsageMinutes(localDate: String): Int
}

/**
 * パッケージ使用サマリ
 */
data class PackageUsageSummary(
    val packageName: String,
    val totalMs: Long
)

/**
 * 時間別使用状況
 */
data class HourlyUsage(
    val hour: Int,
    val packageName: String,
    val totalMs: Long
)