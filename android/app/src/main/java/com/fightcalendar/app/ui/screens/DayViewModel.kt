package com.fightcalendar.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fightcalendar.app.domain.model.FreeTimeSlot
import com.fightcalendar.app.domain.usecase.BlockFreeTimeUseCase
import com.fightcalendar.app.domain.usecase.GetDayDataUseCase
import com.fightcalendar.app.ui.components.HourlyData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class DayScreenState(
    val winRate: Int = 0,
    val totalScreenTime: String = "0時間0分",
    val streakDays: Int = 0,
    val hourlyData: List<HourlyData> = emptyList(),
    val freeTimeSlots: List<FreeTimeSlot> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class DayViewModel @Inject constructor(
    private val getDayDataUseCase: GetDayDataUseCase,
    private val blockFreeTimeUseCase: BlockFreeTimeUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow(DayScreenState())
    val state: StateFlow<DayScreenState> = _state.asStateFlow()
    
    private val currentDate = LocalDate.now()
    
    init {
        loadDayData()
    }
    
    private fun loadDayData() {
        viewModelScope.launch {
            try {
                getDayDataUseCase.observe(currentDate)
                    .catch { throwable ->
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                error = throwable.message ?: "データの取得に失敗しました"
                            )
                        }
                    }
                    .collect { dayData ->
                        _state.update {
                            it.copy(
                                winRate = dayData.winRate,
                                totalScreenTime = dayData.getTotalScreenTimeFormatted(),
                                streakDays = dayData.streakDays,
                                hourlyData = dayData.hourlyData.map { hourlyData ->
                                    HourlyData(
                                        hour = hourlyData.hour,
                                        category = hourlyData.topCategory?.id,
                                        usageMinutes = (hourlyData.usageTimeMs / 60000).toInt(),
                                        hasEvents = hourlyData.hasEvents,
                                        hasFreeTime = hourlyData.hasFreeTime
                                    )
                                },
                                freeTimeSlots = dayData.freeTimeSlots,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "データの取得に失敗しました"
                    )
                }
            }
        }
    }
    
    fun blockFreeTimeSlot(slot: FreeTimeSlot) {
        viewModelScope.launch {
            try {
                val result = blockFreeTimeUseCase(currentDate, slot)
                result.onSuccess {
                    // トースト表示やウィジェット更新は後で実装
                    loadDayData() // データを再読み込み
                }.onFailure { throwable ->
                    _state.update { 
                        it.copy(error = throwable.message ?: "ブロックに失敗しました")
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(error = e.message ?: "ブロックに失敗しました")
                }
            }
        }
    }
    
    fun refresh() {
        _state.update { it.copy(isLoading = true, error = null) }
        loadDayData()
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}