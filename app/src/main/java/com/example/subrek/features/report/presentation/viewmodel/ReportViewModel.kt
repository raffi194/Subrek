package com.example.subrek.features.report.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subrek.core.utils.UiState
import com.example.subrek.features.report.domain.model.SubscriptionReport
import com.example.subrek.features.report.domain.usecase.GetMonthlyReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val getMonthlyReportUseCase: GetMonthlyReportUseCase
) : ViewModel() {

    private val _reportState = MutableStateFlow<UiState<SubscriptionReport>>(UiState.Idle)
    val reportState: StateFlow<UiState<SubscriptionReport>> = _reportState.asStateFlow()

    fun loadMonthlyReport(month: String) {
        viewModelScope.launch {
            _reportState.value = UiState.Loading
            getMonthlyReportUseCase(month)
                .catch { e ->
                    _reportState.value = UiState.Error(e.message ?: "Unknown Error")
                }
                .collect { report ->
                    _reportState.value = UiState.Success(report)
                }
        }
    }
}
