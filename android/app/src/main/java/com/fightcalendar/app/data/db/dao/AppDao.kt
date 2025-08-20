package com.fightcalendar.app.data.db.dao

import androidx.room.*
import com.fightcalendar.app.data.db.entities.AppEntity
import kotlinx.coroutines.flow.Flow

/**
 * アプリ情報DAO
 */
@Dao
interface AppDao {

    /**
     * 全アプリを取得（最終使用日時順）
     */
    @Query("SELECT * FROM apps ORDER BY lastUsed DESC")
    fun getAllApps(): Flow<List<AppEntity>>

    /**
     * パッケージ名でアプリを取得
     */
    @Query("SELECT * FROM apps WHERE packageName = :packageName")
    suspend fun getAppByPackageName(packageName: String): AppEntity?

    /**
     * カテゴリ別アプリ一覧を取得
     */
    @Query("SELECT * FROM apps WHERE category = :category ORDER BY totalUsageMs DESC")
    suspend fun getAppsByCategory(category: String): List<AppEntity>

    /**
     * アプリを挿入または更新
     */
    @Upsert
    suspend fun upsertApp(app: AppEntity)

    /**
     * 複数のアプリを挿入または更新
     */
    @Upsert
    suspend fun upsertApps(apps: List<AppEntity>)

    /**
     * アプリのカテゴリを更新
     */
    @Query("UPDATE apps SET category = :category WHERE packageName = :packageName")
    suspend fun updateAppCategory(packageName: String, category: String)

    /**
     * アプリの使用統計を更新
     */
    @Query("""
        UPDATE apps 
        SET lastUsed = :lastUsed, totalUsageMs = totalUsageMs + :additionalUsageMs 
        WHERE packageName = :packageName
    """)
    suspend fun updateAppUsageStats(
        packageName: String, 
        lastUsed: Long, 
        additionalUsageMs: Long
    )

    /**
     * アプリを削除
     */
    @Query("DELETE FROM apps WHERE packageName = :packageName")
    suspend fun deleteApp(packageName: String)

    /**
     * 使用頻度の高いアプリを取得（上位N件）
     */
    @Query("SELECT * FROM apps ORDER BY totalUsageMs DESC LIMIT :limit")
    suspend fun getTopUsedApps(limit: Int = 10): List<AppEntity>

    /**
     * 未カテゴライズのアプリを取得
     */
    @Query("SELECT * FROM apps WHERE category = '' OR category IS NULL ORDER BY lastUsed DESC")
    suspend fun getUncategorizedApps(): List<AppEntity>

    /**
     * アプリ数をカウント
     */
    @Query("SELECT COUNT(*) FROM apps")
    suspend fun getAppCount(): Int
}