package com.fightcalendar.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingState(
    val currentStep: Int = 0,
    val isCompleting: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class OnboardingViewModel @Inject constructor() : ViewModel() {
    
    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()
    
    fun nextStep() {
        _state.value = _state.value.copy(
            currentStep = _state.value.currentStep + 1
        )
    }
    
    fun previousStep() {
        if (_state.value.currentStep > 0) {
            _state.value = _state.value.copy(
                currentStep = _state.value.currentStep - 1
            )
        }
    }
    
    fun completeOnboarding() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isCompleting = true)
            
            try {
                // TODO: Save onboarding completion flag to preferences
                // TODO: Initialize default settings
                
                _state.value = _state.value.copy(isCompleting = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isCompleting = false,
                    error = e.message ?: "設定の保存に失敗しました"
                )
            }
        }
    }
    
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}