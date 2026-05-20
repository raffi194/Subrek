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
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

data class MonthlySpend(val monthName: String, val amount: Double)

data class DashboardUiState(
    val rawSubscriptions: List<Subscription> = emptyList(),
    val subscriptionsList: List<Subscription> = emptyList(),
    val subscriptionHistory: List<Subscription> = emptyList(),
    val statsState: UiState<DashboardStats> = UiState.Loading,
    val userName: String = "User",
    val userAvatarUrl: String? = null,
    val totalConsumptionThisMonth: Double = 0.0,
    val lifetimeSpending: Double = 0.0,
    val monthlyHistorySpending: List<MonthlySpend> = emptyList(),
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
        repository.getActiveSubscriptionsCount()
            .onEach { count -> _uiState.update { it.copy(activeAppsCount = count) } }
            .launchIn(viewModelScope)
    }

    private fun getPaymentDatesInMonth(sub: Subscription, targetMonthDate: LocalDate): List<LocalDate> {
        val dates = mutableListOf<LocalDate>()
        val start = sub.startDate
        val targetMonth = targetMonthDate.month
        val targetYear = targetMonthDate.year

        if (targetMonthDate.withDayOfMonth(targetMonthDate.lengthOfMonth()).isBefore(start)) {
            return emptyList()
        }

        when (sub.billingCycle.name) {
            "WEEKLY" -> {
                var current = start
                while (current.year <= targetYear) {
                    if (current.year == targetYear && current.month == targetMonth) {
                        dates.add(current)
                    }
                    current = current.plusWeeks(1)
                }
            }
            "MONTHLY" -> {
                var current = start
                while (current.year <= targetYear) {
                    if (current.year == targetYear && current.month == targetMonth) {
                        dates.add(current)
                    }
                    current = current.plusMonths(1)
                }
            }
            "YEARLY" -> {
                var current = start
                while (current.year <= targetYear) {
                    if (current.year == targetYear && current.month == targetMonth) {
                        dates.add(current)
                    }
                    current = current.plusYears(1)
                }
            }
        }
        return dates
    }

    private fun calculateTotalSpendForMonth(
        activeSubs: List<Subscription>,
        endedSubs: List<Subscription>,
        targetMonthDate: LocalDate
    ): Double {
        val today = LocalDate.now()
        var total = 0.0

        fun getSpendingForSub(sub: Subscription): Double {
            val paymentDates = getPaymentDatesInMonth(sub, targetMonthDate)
            var subTotal = 0.0
            for (date in paymentDates) {
                // Jika tanggal pembayaran di masa lalu, harus sudah dikonfirmasi (nextPaymentDate > date)
                // Jika tanggal pembayaran di hari ini atau masa depan, langsung dihitung (belum terlewat/overdue)
                val isConfirmedOrNotOverdue = (sub.nextPaymentDate > date) || (date >= today)

                // Jika statusnya ENDED, pastikan tanggal pembayaran tidak melebihi tanggal dinonaktifkan
                val isWithinActivePeriod = if (sub.status.name == "ENDED") {
                    !date.isAfter(sub.nextPaymentDate)
                } else {
                    true
                }

                if (isConfirmedOrNotOverdue && isWithinActivePeriod) {
                    subTotal += sub.price
                }
            }
            return subTotal
        }

        for (sub in activeSubs) {
            total += getSpendingForSub(sub)
        }
        for (sub in endedSubs) {
            total += getSpendingForSub(sub)
        }

        return total
    }

    private fun calculatePastMonthsSpending(
        activeSubs: List<Subscription>,
        endedSubs: List<Subscription>
    ): List<MonthlySpend> {
        val allSubs = activeSubs + endedSubs
        if (allSubs.isEmpty()) return emptyList()

        val earliestDate = allSubs.minOf { it.startDate }
        val today = LocalDate.now()
        val monthNameFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("id-ID"))

        val result = mutableListOf<MonthlySpend>()
        var current = earliestDate.withDayOfMonth(1)
        val endLimit = today.withDayOfMonth(1)

        while (!current.isAfter(endLimit)) {
            val totalForMonth = calculateTotalSpendForMonth(activeSubs, endedSubs, current)
            if (totalForMonth > 0.0) {
                result.add(MonthlySpend(current.format(monthNameFormatter), totalForMonth))
            }
            current = current.plusMonths(1)
        }

        return result
    }

    private fun calculateLifetimeSpending(
        activeSubs: List<Subscription>,
        endedSubs: List<Subscription>
    ): Double {
        val today = LocalDate.now()
        var total = 0.0

        fun getLifetimeForSub(sub: Subscription): Double {
            val start = sub.startDate
            val end = if (sub.status.name == "ENDED") sub.nextPaymentDate else today

            if (end.isBefore(start)) return 0.0

            return when (sub.billingCycle.name) {
                "MONTHLY" -> {
                    val months = ChronoUnit.MONTHS.between(start.withDayOfMonth(1), end.withDayOfMonth(1)) + 1
                    sub.price * months.coerceAtLeast(1)
                }
                "WEEKLY" -> {
                    val weeks = ChronoUnit.WEEKS.between(start, end) + 1
                    sub.price * weeks.coerceAtLeast(1)
                }
                "YEARLY" -> {
                    val years = ChronoUnit.YEARS.between(start, end) + 1
                    sub.price * years.coerceAtLeast(1)
                }
                else -> 0.0
            }
        }

        for (sub in activeSubs) {
            total += getLifetimeForSub(sub)
        }
        for (sub in endedSubs) {
            total += getLifetimeForSub(sub)
        }

        return total
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

                    // Mengambil data riwayat yang dinonaktifkan (ENDED) secara langsung
                    val endedSubs = try {
                        repository.getSubscriptionHistory().first()
                    } catch (e: Exception) {
                        emptyList()
                    }

                    // Menghitung pengeluaran khusus bulan ini
                    val activeSubs = subs.filter { it.status.name == "ACTIVE" || it.status.name == "TRIAL" }
                    val totalThisMonth = calculateTotalSpendForMonth(activeSubs, endedSubs, LocalDate.now())

                    // Hitung data riwayat dan lifetime spending
                    val monthlyHistory = calculatePastMonthsSpending(activeSubs, endedSubs)
                    val lifetimeSpend = calculateLifetimeSpending(activeSubs, endedSubs)

                    _uiState.update { it.copy(
                        rawSubscriptions = subs,
                        statsState = UiState.Success(stats),
                        totalConsumptionThisMonth = totalThisMonth,
                        lifetimeSpending = lifetimeSpend,
                        monthlyHistorySpending = monthlyHistory,
                        isLoading = false
                    ) }
                    applyFilter()
                }
        }
        viewModelScope.launch {
            repository.getSubscriptionHistory()
                .catch { /* silent fail */ }
                .collect { history ->
                    _uiState.update { it.copy(subscriptionHistory = history) }
                    
                    // Juga update history spending saat history berubah
                    val currentSubs = _uiState.value.rawSubscriptions
                    val activeSubs = currentSubs.filter { it.status.name == "ACTIVE" || it.status.name == "TRIAL" }
                    val monthlyHistory = calculatePastMonthsSpending(activeSubs, history)
                    val lifetimeSpend = calculateLifetimeSpending(activeSubs, history)
                    
                    _uiState.update { it.copy(
                        lifetimeSpending = lifetimeSpend,
                        monthlyHistorySpending = monthlyHistory
                    ) }
                }
        }
    }

    private fun applyFilter() {
        val current = _uiState.value
        _uiState.update { it.copy(subscriptionsList = current.rawSubscriptions) }
    }

    fun deleteSubscription(subscriptionId: String) {
        viewModelScope.launch {
            repository.deleteSubscription(subscriptionId)
        }
    }

    fun markAsPaid(subscription: Subscription) {
        viewModelScope.launch {
            val updatedSub = subscription.copy(
                nextPaymentDate = when (subscription.billingCycle.name) {
                    "WEEKLY" -> subscription.nextPaymentDate.plusWeeks(1)
                    "MONTHLY" -> subscription.nextPaymentDate.plusMonths(1)
                    "YEARLY" -> subscription.nextPaymentDate.plusYears(1)
                    else -> subscription.nextPaymentDate.plusMonths(1)
                },
                updatedAt = LocalDate.now()
            )
            repository.insertSubscription(updatedSub)
        }
    }
}