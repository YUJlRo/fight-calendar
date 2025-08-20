package com.fightcalendar.app.domain.usecase

import com.fightcalendar.app.domain.model.FreeTimeSlot
import com.fightcalendar.app.domain.repository.DayDataRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * 空き時間ブロックユースケース
 */
class BlockFreeTimeUseCase @Inject constructor(
    private val repository: DayDataRepository
) {
    suspend operator fun invoke(
        date: LocalDate,
        slot: FreeTimeSlot,
        title: String = "集中時間"
    ): Result<Unit> {
        return try {
            val success = repository.blockFreeTimeSlot(date, slot, title)
            if (success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("空き時間のブロックに失敗しました"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}