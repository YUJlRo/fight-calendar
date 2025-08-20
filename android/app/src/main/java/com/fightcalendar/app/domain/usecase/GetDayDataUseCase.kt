package com.fightcalendar.app.domain.usecase

import com.fightcalendar.app.domain.model.DayData
import com.fightcalendar.app.domain.repository.DayDataRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

/**
 * 日次データ取得ユースケース
 */
class GetDayDataUseCase @Inject constructor(
    private val repository: DayDataRepository
) {
    suspend operator fun invoke(date: LocalDate): DayData {
        return repository.getDayData(date)
    }
    
    fun observe(date: LocalDate): Flow<DayData> {
        return repository.observeDayData(date)
    }
}