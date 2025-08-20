package com.fightcalendar.app.data.usage

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import com.fightcalendar.app.data.db.dao.AppDao
import com.fightcalendar.app.data.db.dao.UsageSessionDao
import com.fightcalendar.app.data.db.entities.AppEntity
import com.fightcalendar.app.data.db.entities.UsageSessionEntity
import com.fightcalendar.app.domain.model.Category
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 使用状況データの取得とセッション復元を担当するRepository
 */
@Singleton
class UsageRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val usageSessionDao: UsageSessionDao,
    private val appDao: AppDao,
    private val categorizer: AppCategorizer
) {
    private val usageStatsManager: UsageStatsManager by lazy {
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    }

    private val packageManager: PackageManager by lazy {
        context.packageManager
    }

    /**
     * 指定日の使用セッションを取得
     */
    suspend fun getSessionsByDate(localDate: String): List<UsageSessionEntity> {
        return usageSessionDao.getSessionsByDate(localDate)
    }

    /**
     * 最新の使用セッションを監視
     */
    fun getRecentSessions(): Flow<List<UsageSessionEntity>> {
        return usageSessionDao.getRecentSessions()
    }

    /**
     * 指定期間の使用状況を収集してセッションとして保存
     */
    suspend fun collectUsageData(startTimeMs: Long, endTimeMs: Long): Result<Int> = withContext(Dispatchers.IO) {
        try {
            // UsageEventsを取得
            val usageEvents = usageStatsManager.queryEvents(startTimeMs, endTimeMs)
            
            // セッションを復元
            val sessions = restoreSessionsFromEvents(usageEvents)
            
            // 5分未満のセッションを除外
            val filteredSessions = sessions.filter { it.durationMs >= 5 * 60 * 1000 }
            
            // アプリ情報を更新
            updateAppEntities(filteredSessions)
            
            // セッションをデータベースに保存
            usageSessionDao.insertSessions(filteredSessions)
            
            Result.success(filteredSessions.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * UsageEventsからセッションを復元
     */
    private suspend fun restoreSessionsFromEvents(usageEvents: UsageEvents): List<UsageSessionEntity> {
        val sessions = mutableListOf<UsageSessionEntity>()
        val activeApps = mutableMapOf<String, Long>() // packageName -> startTime

        while (usageEvents.hasNextEvent()) {
            val event = UsageEvents.Event()
            usageEvents.getNextEvent(event)

            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED,
                UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                    // アプリが前面に来た
                    activeApps[event.packageName] = event.timeStamp
                }
                
                UsageEvents.Event.ACTIVITY_PAUSED,
                UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                    // アプリが背景に移った
                    val startTime = activeApps.remove(event.packageName)
                    if (startTime != null && event.timeStamp > startTime) {
                        val duration = event.timeStamp - startTime
                        
                        sessions.add(
                            UsageSessionEntity(
                                packageName = event.packageName,
                                startTimeMs = startTime,
                                endTimeMs = event.timeStamp,
                                durationMs = duration,
                                localDate = UsageSessionEntity.formatLocalDate(startTime)
                            )
                        )
                    }
                }
            }
        }

        return sessions.sortedBy { it.startTimeMs }
    }

    /**
     * セッションからアプリエンティティを更新
     */
    private suspend fun updateAppEntities(sessions: List<UsageSessionEntity>) {
        val appUpdates = sessions.groupBy { it.packageName }
        
        for ((packageName, packageSessions) in appUpdates) {
            val totalUsageMs = packageSessions.sumOf { it.durationMs }
            val lastUsed = packageSessions.maxOf { it.endTimeMs }
            
            // 既存のアプリ情報を取得または新規作成
            val existingApp = appDao.getAppByPackageName(packageName)
            
            if (existingApp != null) {
                // 既存アプリの統計を更新
                appDao.updateAppUsageStats(packageName, lastUsed, totalUsageMs)
            } else {
                // 新規アプリを登録
                val appLabel = getAppLabel(packageName)
                val category = categorizer.assignCategory(packageName, appLabel)
                
                val newApp = AppEntity(
                    appId = generateAppId(packageName),
                    packageName = packageName,
                    label = appLabel,
                    category = category.id,
                    lastUsed = lastUsed,
                    totalUsageMs = totalUsageMs
                )
                
                appDao.upsertApp(newApp)
            }
        }
    }

    /**
     * パッケージ名からアプリ名を取得
     */
    private fun getAppLabel(packageName: String): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }

    /**
     * パッケージ名からアプリIDを生成
     */
    private fun generateAppId(packageName: String): String {
        return "app_${packageName.replace(".", "_")}"
    }

    /**
     * 指定日の総使用時間を取得（分）
     */
    suspend fun getTotalUsageMinutes(localDate: String): Int {
        return usageSessionDao.getTotalUsageMinutes(localDate)
    }

    /**
     * 指定日のパッケージ別使用時間を取得
     */
    suspend fun getPackageUsageSummary(localDate: String) = 
        usageSessionDao.getPackageUsageSummary(localDate)

    /**
     * 指定日の時間別使用状況を取得
     */
    suspend fun getHourlyUsage(localDate: String) = 
        usageSessionDao.getHourlyUsage(localDate)

    /**
     * 古いセッションデータを削除（30日より古いもの）
     */
    suspend fun cleanupOldSessions() {
        val thirtyDaysAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)
        usageSessionDao.deleteOldSessions(thirtyDaysAgo)
    }

    /**
     * 使用統計のアクセス権限があるかチェック
     */
    fun hasUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
        val mode = appOps.checkOpNoThrow(
            android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == android.app.AppOpsManager.MODE_ALLOWED
    }

    /**
     * 今日の使用データを強制更新
     */
    suspend fun refreshTodayUsage(): Result<Int> {
        val now = System.currentTimeMillis()
        val todayStart = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
            .atStartOfDayIn(TimeZone.currentSystemDefault())
            .toEpochMilliseconds()
            
        return collectUsageData(todayStart, now)
    }
}