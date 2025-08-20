package com.fightcalendar.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.fightcalendar.app.worker.DailySyncWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * ファイトカレンダーアプリケーションクラス
 * Hilt DI とWorkManagerの初期化を行う
 */
@HiltAndroidApp
class FightCalendarApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        
        // 日次同期ワーカーをスケジュール
        scheduleDailySync()
    }

    /**
     * 日次同期ワーカーを毎日0:30にスケジュール
     */
    private fun scheduleDailySync() {
        val dailySyncRequest = PeriodicWorkRequestBuilder<DailySyncWorker>(
            repeatInterval = 24,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            DailySyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            dailySyncRequest
        )
    }

    /**
     * 次の0:30までの遅延を計算
     */
    private fun calculateInitialDelay(): Long {
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = now
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 30)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            
            // 今日の0:30が過ぎていたら明日に設定
            if (timeInMillis <= now) {
                add(java.util.Calendar.DAY_OF_MONTH, 1)
            }
        }
        
        return calendar.timeInMillis - now
    }
}