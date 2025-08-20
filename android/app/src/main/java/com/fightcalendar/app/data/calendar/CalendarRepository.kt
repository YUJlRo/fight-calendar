package com.fightcalendar.app.data.calendar

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.fightcalendar.app.data.db.entities.DailyAggregateEntity
import com.fightcalendar.app.domain.model.Category
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Googleカレンダー連携Repository
 * Calendar Providerを使用して透明イベントを作成・管理
 */
@Singleton
class CalendarRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val FIGHT_CALENDAR_TAG = "#fightcalendar"
        private const val FC_ID_PREFIX = "fcId="
    }

    /**
     * 利用可能なカレンダー一覧を取得
     */
    suspend fun getAvailableCalendars(): List<CalendarInfo> = withContext(Dispatchers.IO) {
        if (!hasCalendarPermission()) {
            return@withContext emptyList()
        }

        val calendars = mutableListOf<CalendarInfo>()
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
            CalendarContract.Calendars.OWNER_ACCOUNT
        )

        val cursor = context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            "${CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL} >= ?",
            arrayOf(CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR.toString()),
            null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getLong(0)
                val displayName = it.getString(1) ?: ""
                val accountName = it.getString(2) ?: ""
                val accessLevel = it.getInt(3)
                val ownerAccount = it.getString(4) ?: ""

                calendars.add(
                    CalendarInfo(
                        id = id,
                        displayName = displayName,
                        accountName = accountName,
                        accessLevel = accessLevel,
                        ownerAccount = ownerAccount
                    )
                )
            }
        }

        calendars
    }

    /**
     * 日次サマリイベントを作成・更新
     */
    suspend fun upsertDailySummary(
        calendarId: Long,
        date: LocalDate,
        aggregate: DailyAggregateEntity,
        topApps: List<TopAppInfo>
    ): Result<Long> = withContext(Dispatchers.IO) {
        try {
            if (!hasCalendarPermission()) {
                return@withContext Result.failure(SecurityException("カレンダー権限がありません"))
            }

            val fcId = "summary-${date}"
            val existingEventId = findExistingEvent(calendarId, fcId)

            val title = "App使用サマリ ${date} | 勝率 ${aggregate.winRate.toInt()}%"
            val description = buildDailySummaryDescription(aggregate, topApps, fcId)

            // All-day イベントの開始・終了時間
            val startMillis = date.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            val endMillis = startMillis + 24 * 60 * 60 * 1000L

            val values = ContentValues().apply {
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.DESCRIPTION, description)
                put(CalendarContract.Events.DTSTART, startMillis)
                put(CalendarContract.Events.DTEND, endMillis)
                put(CalendarContract.Events.ALL_DAY, 1)
                put(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_FREE)
                put(CalendarContract.Events.ACCESS_LEVEL, CalendarContract.Events.ACCESS_PRIVATE)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.currentSystemDefault().id)
            }

            val eventId = if (existingEventId != null) {
                // 更新
                context.contentResolver.update(
                    ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, existingEventId),
                    values,
                    null,
                    null
                )
                existingEventId
            } else {
                // 新規作成
                val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
                ContentUris.parseId(uri!!)
            }

            Result.success(eventId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 時間帯イベントを作成・更新
     */
    suspend fun upsertHourlyEvent(
        calendarId: Long,
        date: LocalDate,
        hour: Int,
        topCategory: Category,
        usedMinutes: Int,
        apps: List<String>
    ): Result<Long> = withContext(Dispatchers.IO) {
        try {
            if (!hasCalendarPermission()) {
                return@withContext Result.failure(SecurityException("カレンダー権限がありません"))
            }

            val fcId = "hourly-${date}-${hour.toString().padStart(2, '0')}"
            val existingEventId = findExistingEvent(calendarId, fcId)

            val title = "【${topCategory.displayName}】${hour.toString().padStart(2, '0')}:00台 ${usedMinutes}分"
            val description = buildHourlyDescription(apps, fcId)

            // 1時間の開始・終了時間
            val baseTime = date.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            val startMillis = baseTime + hour * 60 * 60 * 1000L
            val endMillis = startMillis + 60 * 60 * 1000L

            val values = ContentValues().apply {
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.DESCRIPTION, description)
                put(CalendarContract.Events.DTSTART, startMillis)
                put(CalendarContract.Events.DTEND, endMillis)
                put(CalendarContract.Events.ALL_DAY, 0)
                put(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_FREE)
                put(CalendarContract.Events.ACCESS_LEVEL, CalendarContract.Events.ACCESS_PRIVATE)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.currentSystemDefault().id)
            }

            val eventId = if (existingEventId != null) {
                // 更新
                context.contentResolver.update(
                    ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, existingEventId),
                    values,
                    null,
                    null
                )
                existingEventId
            } else {
                // 新規作成
                val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
                ContentUris.parseId(uri!!)
            }

            Result.success(eventId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 指定したカレンダーのBusyイベントを取得
     */
    suspend fun getBusyEvents(calendarId: Long, date: LocalDate): List<BusyEvent> = withContext(Dispatchers.IO) {
        if (!hasCalendarPermission()) {
            return@withContext emptyList()
        }

        val events = mutableListOf<BusyEvent>()
        val startOfDay = date.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val endOfDay = startOfDay + 24 * 60 * 60 * 1000L

        val projection = arrayOf(
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.ALL_DAY,
            CalendarContract.Instances.AVAILABILITY
        )

        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, startOfDay)
        ContentUris.appendId(builder, endOfDay)

        val cursor = context.contentResolver.query(
            builder.build(),
            projection,
            "${CalendarContract.Instances.CALENDAR_ID} = ? AND ${CalendarContract.Instances.AVAILABILITY} = ?",
            arrayOf(calendarId.toString(), CalendarContract.Events.AVAILABILITY_BUSY.toString()),
            "${CalendarContract.Instances.BEGIN} ASC"
        )

        cursor?.use {
            while (it.moveToNext()) {
                val begin = it.getLong(0)
                val end = it.getLong(1)
                val title = it.getString(2) ?: ""
                val allDay = it.getInt(3) == 1
                val availability = it.getInt(4)

                // ファイトカレンダーのイベントは除外
                if (!title.contains(FIGHT_CALENDAR_TAG)) {
                    events.add(
                        BusyEvent(
                            startTimeMs = begin,
                            endTimeMs = end,
                            title = title,
                            isAllDay = allDay,
                            availability = availability
                        )
                    )
                }
            }
        }

        events
    }

    /**
     * ファイトカレンダーのイベントを一括削除
     */
    suspend fun deleteAllFightCalendarEvents(calendarId: Long): Result<Int> = withContext(Dispatchers.IO) {
        try {
            if (!hasCalendarPermission()) {
                return@withContext Result.failure(SecurityException("カレンダー権限がありません"))
            }

            val deletedCount = context.contentResolver.delete(
                CalendarContract.Events.CONTENT_URI,
                "${CalendarContract.Events.CALENDAR_ID} = ? AND ${CalendarContract.Events.DESCRIPTION} LIKE ?",
                arrayOf(calendarId.toString(), "%$FIGHT_CALENDAR_TAG%")
            )

            Result.success(deletedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * フォーカス予定を作成
     */
    suspend fun createFocusEvent(
        calendarId: Long,
        title: String,
        startTimeMs: Long,
        endTimeMs: Long,
        isFree: Boolean = true
    ): Result<Long> = withContext(Dispatchers.IO) {
        try {
            if (!hasCalendarPermission()) {
                return@withContext Result.failure(SecurityException("カレンダー権限がありません"))
            }

            val values = ContentValues().apply {
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.DESCRIPTION, "ファイトカレンダーで作成されたフォーカス予定")
                put(CalendarContract.Events.DTSTART, startTimeMs)
                put(CalendarContract.Events.DTEND, endTimeMs)
                put(CalendarContract.Events.ALL_DAY, 0)
                put(CalendarContract.Events.AVAILABILITY, 
                    if (isFree) CalendarContract.Events.AVAILABILITY_FREE 
                    else CalendarContract.Events.AVAILABILITY_BUSY)
                put(CalendarContract.Events.ACCESS_LEVEL, CalendarContract.Events.ACCESS_PRIVATE)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.currentSystemDefault().id)
            }

            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            val eventId = ContentUris.parseId(uri!!)

            Result.success(eventId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun hasCalendarPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED
    }

    private fun findExistingEvent(calendarId: Long, fcId: String): Long? {
        val cursor = context.contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            arrayOf(CalendarContract.Events._ID),
            "${CalendarContract.Events.CALENDAR_ID} = ? AND ${CalendarContract.Events.DESCRIPTION} LIKE ?",
            arrayOf(calendarId.toString(), "%$FC_ID_PREFIX$fcId%"),
            null
        )

        return cursor?.use {
            if (it.moveToFirst()) it.getLong(0) else null
        }
    }

    private fun buildDailySummaryDescription(
        aggregate: DailyAggregateEntity,
        topApps: List<TopAppInfo>,
        fcId: String
    ): String {
        return buildString {
            appendLine("総使用時間: ${aggregate.getTotalTimeFormatted()}")
            appendLine("生産時間: ${aggregate.getProductiveTimeFormatted()}")
            appendLine("空き時間: ${aggregate.getFreeTimeFormatted()}")
            appendLine()
            
            appendLine("カテゴリ別使用時間:")
            appendLine("• 仕事: ${formatMinutes(aggregate.workMinutes)}")
            appendLine("• 学習: ${formatMinutes(aggregate.studyMinutes)}")
            appendLine("• 娯楽・SNS: ${formatMinutes(aggregate.entertainmentMinutes)}")
            appendLine("• ツール: ${formatMinutes(aggregate.toolsMinutes)}")
            appendLine()
            
            if (topApps.isNotEmpty()) {
                appendLine("主要アプリ:")
                topApps.take(5).forEach { app ->
                    appendLine("• ${app.name}: ${formatMinutes(app.minutes)}")
                }
                appendLine()
            }
            
            append("$FIGHT_CALENDAR_TAG $FC_ID_PREFIX$fcId")
        }
    }

    private fun buildHourlyDescription(apps: List<String>, fcId: String): String {
        return buildString {
            if (apps.isNotEmpty()) {
                appendLine("使用アプリ:")
                apps.forEach { app ->
                    appendLine("• $app")
                }
                appendLine()
            }
            append("$FIGHT_CALENDAR_TAG $FC_ID_PREFIX$fcId")
        }
    }

    private fun formatMinutes(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return if (hours > 0) "${hours}時間${mins}分" else "${mins}分"
    }
}

/**
 * カレンダー情報
 */
data class CalendarInfo(
    val id: Long,
    val displayName: String,
    val accountName: String,
    val accessLevel: Int,
    val ownerAccount: String
)

/**
 * Busyイベント情報
 */
data class BusyEvent(
    val startTimeMs: Long,
    val endTimeMs: Long,
    val title: String,
    val isAllDay: Boolean,
    val availability: Int
)

/**
 * トップアプリ情報
 */
data class TopAppInfo(
    val name: String,
    val minutes: Int
)