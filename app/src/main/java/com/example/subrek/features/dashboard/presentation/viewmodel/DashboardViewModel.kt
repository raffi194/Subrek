package com.example.subrek.features.dashboard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subrek.core.utils.UiState
import com.example.subrek.features.dashboard.domain.model.DashboardStats
import com.example.subrek.features.dashboard.domain.repository.DashboardRepository
import com.example.subrek.features.subscription.domain.repository.SubscriptionRepository
import com.example.subrek.features.subscription.domain.model.Subscription
import com.example.subrek.features.dashboard.domain.usecase.GetDashboardStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SortOption { NAME, PRICE_DESC, NEXT_PAYMENT }

data class DashboardUiState(
    val statsState: UiState<DashboardStats> = UiState.Loading,
    val subscriptionsList: List<Subscription> = emptyList(), // List untuk List View (Bisa Ter-filter)
    val rawSubscriptions: List<Subscription> = emptyList(),   // List Utuh Asli (Untuk Komponen Grafik Chart)
    val selectedCategory: String = "Semua",
    val currentSortOption: SortOption = SortOption.NEXT_PAYMENT
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
    private val dashboardRepository: DashboardRepository,
    private val getDashboardStatsUseCase: GetDashboardStatsUseCase
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("Semua")
    private val _currentSortOption = MutableStateFlow(SortOption.NEXT_PAYMENT)

    val uiState: StateFlow<DashboardUiState> = combine(
        subscriptionRepository.getAllSubscriptions(),
        _selectedCategory,
        _currentSortOption
    ) { subs, category, sortOption ->
        
        val stats = getDashboardStatsUseCase(subs)

        var filteredList = subs
        if (category != "Semua") {
            filteredList = filteredList.filter { it.category == category }
        }

        filteredList = when (sortOption) {
            SortOption.NAME -> filteredList.sortedBy { it.name.lowercase() }
            SortOption.PRICE_DESC -> filteredList.sortedByDescending { it.price }
            SortOption.NEXT_PAYMENT -> filteredList.sortedBy { it.nextPaymentDate }
        }

        DashboardUiState(
            statsState = UiState.Success(stats),
            subscriptionsList = filteredList,
            rawSubscriptions = subs, // Menyimpan list utuh untuk validasi chart >= 2 data
            selectedCategory = category,
            currentSortOption = sortOption
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun changeCategoryFilter(category: String) { _selectedCategory.value = category }
    fun changeSortOption(option: SortOption) { _currentSortOption.value = option }

    fun triggerSync() {
        viewModelScope.launch {
            subscriptionRepository.syncWithRemote()
        }
    }
}
