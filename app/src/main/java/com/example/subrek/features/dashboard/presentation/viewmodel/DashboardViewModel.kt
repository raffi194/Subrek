package com.example.subrek.features.dashboard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subrek.core.utils.UiState
import com.example.subrek.features.dashboard.domain.model.DashboardStats
import com.example.subrek.features.dashboard.domain.usecase.GetDashboardStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardStatsUseCase: GetDashboardStatsUseCase
) : ViewModel() {

    private val _dashboardState = MutableStateFlow<UiState<DashboardStats>>(UiState.Idle)
    val dashboardState: StateFlow<UiState<DashboardStats>> = _dashboardState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _dashboardState.value = UiState.Loading
            getDashboardStatsUseCase()
                .catch { e ->
                    _dashboardState.value = UiState.Error(e.message ?: "Unknown Error")
                }
                .collect { stats ->
                    _dashboardState.value = UiState.Success(stats)
                }
        }
    }
}
