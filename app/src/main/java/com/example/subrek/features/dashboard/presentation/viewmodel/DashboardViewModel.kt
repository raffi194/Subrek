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
    val subscriptionsList: List<Subscription> = emptyList(),
    val selectedCategory: String = "Semua",
    val selectedPaymentMethod: String = "Semua",
    val currentSortOption: SortOption = SortOption.NEXT_PAYMENT
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
    private val getDashboardStatsUseCase: GetDashboardStatsUseCase
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("Semua")
    private val _selectedPaymentMethod = MutableStateFlow("Semua")
    private val _currentSortOption = MutableStateFlow(SortOption.NEXT_PAYMENT)

    val uiState: StateFlow<DashboardUiState> = combine(
        subscriptionRepository.getAllSubscriptions(),
        _selectedCategory,
        _selectedPaymentMethod,
        _currentSortOption
    ) { subs, category, payment, sortOption ->
        
        // 1. Kalkulasi Statistik Atas secara dinamis dari data murni asli
        val stats = getDashboardStatsUseCase(subs)

        // 2. Filter data berdasarkan Kategori & Metode Pembayaran
        var filteredList = subs
        if (category != "Semua") {
            filteredList = filteredList.filter { it.category == category }
        }
        if (payment != "Semua") {
            filteredList = filteredList.filter { it.paymentMethod == payment }
        }

        // 3. Urutkan (*Sorting*) data berdasarkan opsi terpilih
        filteredList = when (sortOption) {
            SortOption.NAME -> filteredList.sortedBy { it.name.lowercase() }
            SortOption.PRICE_DESC -> filteredList.sortedByDescending { it.price }
            SortOption.NEXT_PAYMENT -> filteredList.sortedBy { it.nextPaymentDate }
        }

        DashboardUiState(
            statsState = UiState.Success(stats),
            subscriptionsList = filteredList,
            selectedCategory = category,
            selectedPaymentMethod = payment,
            currentSortOption = sortOption
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun changeCategoryFilter(category: String) { _selectedCategory.value = category }
    fun changePaymentFilter(method: String) { _selectedPaymentMethod.value = method }
    fun changeSortOption(option: SortOption) { _currentSortOption.value = option }

    fun triggerSync() {
        viewModelScope.launch {
            subscriptionRepository.syncWithRemote()
        }
    }
}
