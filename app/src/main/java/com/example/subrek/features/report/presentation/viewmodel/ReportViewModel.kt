package com.example.subrek.features.report.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subrek.core.utils.UiState
import com.example.subrek.features.report.domain.model.SubscriptionReport
import com.example.subrek.features.report.domain.usecase.GetMonthlyReportUseCase
import com.example.subrek.features.subscription.domain.repository.SubscriptionRepository
import com.example.subrek.features.subscription.domain.usecase.ScanGhostSubscriptionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportUiState(
    val reportState: UiState<SubscriptionReport> = UiState.Loading,
    val isYearlyTrend: Boolean = false,
    val detectedGhostCount: Int = 0
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
    private val getMonthlyReportUseCase: GetMonthlyReportUseCase,
    private val scanGhostSubscriptionsUseCase: ScanGhostSubscriptionsUseCase
) : ViewModel() {

    private val _isYearlyTrend = MutableStateFlow(false)
    private val _ghostCount = MutableStateFlow(0)

    val uiState: StateFlow<ReportUiState> = combine(
        subscriptionRepository.getAllSubscriptions(),
        _isYearlyTrend,
        _ghostCount
    ) { subs, isYearly, ghostCount ->

        val report = getMonthlyReportUseCase(subs, isYearly)
        
        ReportUiState(
            reportState = UiState.Success(report),
            isYearlyTrend = isYearly,
            detectedGhostCount = ghostCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReportUiState()
    )

    init {
        viewModelScope.launch {
            subscriptionRepository.getAllSubscriptions().firstOrNull()?.let { currentSubs ->
                val detected = scanGhostSubscriptionsUseCase(currentSubs)
                _ghostCount.value = detected
            }
        }
    }

    fun toggleTrendType(isYearly: Boolean) {
        _isYearlyTrend.value = isYearly
    }
}
