package com.example.subrek.features.dashboard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subrek.core.utils.UiState
import com.example.subrek.features.dashboard.domain.model.DashboardStats
import com.example.subrek.features.dashboard.domain.usecase.GetDashboardStatsUseCase
import com.example.subrek.features.subscription.domain.model.Subscription
import com.example.subrek.features.subscription.domain.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val rawSubscriptions: List<Subscription> = emptyList(),
    val subscriptionsList: List<Subscription> = emptyList(),
    val subscriptionHistory: List<Subscription> = emptyList(),
    val statsState: UiState<DashboardStats> = UiState.Loading,
    val userName: String = "User",
    val userAvatarUrl: String? = null,
    val averageConsumption: Double = 0.0,
    val activeAppsCount: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: SubscriptionRepository,
    private val getDashboardStatsUseCase: GetDashboardStatsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
        observeStats()
    }

    private fun observeStats() {
        repository.getAverageConsumption()
            .onEach { value -> _uiState.update { it.copy(averageConsumption = value) } }
            .launchIn(viewModelScope)

        repository.getActiveSubscriptionsCount()
            .onEach { count -> _uiState.update { it.copy(activeAppsCount = count) } }
            .launchIn(viewModelScope)
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            repository.getAllSubscriptions()
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.localizedMessage) }
                }
                .collect { subs ->
                    val stats = getDashboardStatsUseCase(subs)
                    _uiState.update { it.copy(
                        rawSubscriptions = subs,
                        statsState = UiState.Success(stats),
                        isLoading = false
                    ) }
                    applyFilter()

                    // Melakukan force sync data lokal ke remote (termasuk verifikasi session data user profiles)
                    repository.syncWithRemote()
                }
        }
        // Load riwayat langganan yang sudah berakhir (status = ENDED)
        viewModelScope.launch {
            repository.getSubscriptionHistory()
                .catch { /* silent fail, history bukan data kritis */ }
                .collect { history ->
                    _uiState.update { it.copy(subscriptionHistory = history) }
                }
        }
    }

    // 🛠️ DIUBAH: Disederhanakan karena penyaringan kategori tidak diperlukan lagi
    private fun applyFilter() {
        val current = _uiState.value
        _uiState.update { it.copy(subscriptionsList = current.rawSubscriptions) }
    }

    fun deleteSubscription(subscriptionId: String) {
        viewModelScope.launch {
            repository.deleteSubscriptionFromLocalAndRemote(subscriptionId)
        }
    }

    fun triggerSync() {
        viewModelScope.launch {
            repository.syncWithRemote()
        }
    }
}
