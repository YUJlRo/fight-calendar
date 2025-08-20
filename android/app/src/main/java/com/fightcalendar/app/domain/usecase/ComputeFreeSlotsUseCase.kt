package com.fightcalendar.app.domain.usecase

import com.fightcalendar.app.data.calendar.BusyEvent
import com.fightcalendar.app.data.calendar.CalendarRepository
import com.fightcalendar.app.data.db.dao.FreeSlotDao
import com.fightcalendar.app.data.db.dao.SettingsDao
import com.fightcalendar.app.data.db.entities.FreeSlotEntity
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import javax.inject.Inject

/**
 * 空き時間スロット計算UseCase
 * 稼働時間とBusyイベントから空き時間を算出
 */
class ComputeFreeSlotsUseCase @Inject constructor(
    private val calendarRepository: CalendarRepository,
    private val freeSlotDao: FreeSlotDao,
    private val settingsDao: SettingsDao
) {

    /**
     * 指定日の空き時間スロットを計算・保存
     */
    suspend fun computeAndSaveFreeSlotsForDate(date: LocalDate): Result<List<FreeSlotEntity>> {
        return try {
            val settings = settingsDao.getSettingsOnce()
                ?: return Result.failure(IllegalStateException("設定が見つかりません"))

            if (!settings.hasCalendarSelected()) {
                return Result.failure(IllegalStateException("カレンダーが選択されていません"))
            }

            // Busyイベントを取得
            val busyEvents = calendarRepository.getBusyEvents(settings.selectedCalendarId, date)

            // 空き時間スロットを計算
            val freeSlots = calculateFreeSlots(date, settings, busyEvents)

            // 既存のスロットを削除して新しいスロットを保存
            freeSlotDao.deleteSlotsByDate(date.toString())
            freeSlotDao.insertSlots(freeSlots)

            Result.success(freeSlots)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 空き時間スロットを計算
     */
    private fun calculateFreeSlots(
        date: LocalDate,
        settings: com.fightcalendar.app.data.db.entities.SettingsEntity,
        busyEvents: List<BusyEvent>
    ): List<FreeSlotEntity> {
        val freeSlots = mutableListOf<FreeSlotEntity>()
        val timezone = TimeZone.currentSystemDefault()
        
        // 稼働時間の開始・終了（ミリ秒）
        val dayStart = date.atStartOfDayIn(timezone).toEpochMilliseconds()
        val workingStart = dayStart + (settings.workingStartHour * 60 + settings.workingStartMinute) * 60 * 1000L
        val workingEnd = dayStart + (settings.workingEndHour * 60 + settings.workingEndMinute) * 60 * 1000L

        // Busyイベントを時間順にソートし、稼働時間内のもののみフィルタ
        val filteredBusyEvents = busyEvents
            .filter { event ->
                // 稼働時間と重複するイベントのみ
                event.endTimeMs > workingStart && event.startTimeMs < workingEnd
            }
            .map { event ->
                // 稼働時間内にクリップ
                TimeSlot(
                    start = maxOf(event.startTimeMs, workingStart),
                    end = minOf(event.endTimeMs, workingEnd)
                )
            }
            .filter { it.start < it.end } // 有効な時間幅のみ
            .sortedBy { it.start }

        // Busyイベントをマージして重複を解消
        val mergedBusySlots = mergeBusySlots(filteredBusyEvents)

        // 空き時間スロットを生成
        var currentTime = workingStart
        
        for (busySlot in mergedBusySlots) {
            // Busyスロットの前の空き時間
            if (currentTime < busySlot.start) {
                val freeSlot = createFreeSlotIfValid(date, currentTime, busySlot.start)
                if (freeSlot != null) {
                    freeSlots.add(freeSlot)
                }
            }
            currentTime = maxOf(currentTime, busySlot.end)
        }

        // 最後のBusyスロット後の空き時間
        if (currentTime < workingEnd) {
            val freeSlot = createFreeSlotIfValid(date, currentTime, workingEnd)
            if (freeSlot != null) {
                freeSlots.add(freeSlot)
            }
        }

        return freeSlots
    }

    /**
     * Busyスロットをマージして重複を解消
     */
    private fun mergeBusySlots(busySlots: List<TimeSlot>): List<TimeSlot> {
        if (busySlots.isEmpty()) return emptyList()

        val merged = mutableListOf<TimeSlot>()
        var current = busySlots.first()

        for (i in 1 until busySlots.size) {
            val next = busySlots[i]
            
            if (current.end >= next.start) {
                // 重複または隣接している場合はマージ
                current = TimeSlot(current.start, maxOf(current.end, next.end))
            } else {
                // 重複していない場合は現在のスロットを追加
                merged.add(current)
                current = next
            }
        }
        
        merged.add(current)
        return merged
    }

    /**
     * 有効な空き時間スロットを作成（60分以上の場合のみ）
     */
    private fun createFreeSlotIfValid(
        date: LocalDate,
        startTimeMs: Long,
        endTimeMs: Long
    ): FreeSlotEntity? {
        val durationMs = endTimeMs - startTimeMs
        val durationMinutes = (durationMs / (60 * 1000)).toInt()
        
        // 60分未満の空き時間は除外
        if (durationMinutes < 60) {
            return null
        }

        val timezone = TimeZone.currentSystemDefault()
        val startDateTime = kotlinx.datetime.Instant.fromEpochMilliseconds(startTimeMs)
            .toLocalDateTime(timezone)
        val endDateTime = kotlinx.datetime.Instant.fromEpochMilliseconds(endTimeMs)
            .toLocalDateTime(timezone)

        return FreeSlotEntity(
            localDate = date.toString(),
            startTimeMs = startTimeMs,
            endTimeMs = endTimeMs,
            durationMinutes = durationMinutes,
            startHour = startDateTime.hour,
            endHour = endDateTime.hour
        )
    }

    /**
     * 今日の次の利用可能な空きスロットを取得
     */
    suspend fun getNextAvailableFreeSlot(): FreeSlotEntity? {
        val currentMs = System.currentTimeMillis()
        return freeSlotDao.getNextAvailableSlot(currentMs)
    }

    /**
     * 指定日の空き時間合計を取得
     */
    suspend fun getTotalFreeMinutes(date: LocalDate): Int {
        return freeSlotDao.getTotalFreeMinutes(date.toString())
    }

    /**
     * 指定日の利用可能な空きスロット数を取得
     */
    suspend fun getAvailableFreeSlotCount(date: LocalDate): Int {
        return freeSlotDao.getAvailableSlotsByDate(date.toString()).size
    }

    /**
     * 空きスロットをブロック（フォーカス予定作成時）
     */
    suspend fun blockFreeSlot(slotId: Long): Result<Unit> {
        return try {
            freeSlotDao.blockSlot(slotId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 進行中の空きスロットを取得
     */
    suspend fun getCurrentFreeSlot(): FreeSlotEntity? {
        val currentMs = System.currentTimeMillis()
        return freeSlotDao.getCurrentFreeSlot(currentMs)
    }

    /**
     * 古い空きスロットデータを削除（7日より古いもの）
     */
    suspend fun cleanupOldFreeSlots() {
        val sevenDaysAgo = LocalDate.fromEpochDays(
            kotlinx.datetime.Clock.System.todayIn(TimeZone.currentSystemDefault()).toEpochDays() - 7
        )
        freeSlotDao.deleteOldSlots(sevenDaysAgo.toString())
    }
}

/**
 * 時間スロット（内部使用）
 */
private data class TimeSlot(
    val start: Long,
    val end: Long
)