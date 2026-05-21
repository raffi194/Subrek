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

    /**
     * Mencari tanggal-tanggal pembayaran yang jatuh tempo di bulan tertentu
     */
    private fun getPaymentDatesInMonth(sub: Subscription, targetMonthDate: LocalDate): List<LocalDate> {
        val dates = mutableListOf<LocalDate>()
        val start = sub.startDate
        val targetMonth = targetMonthDate.month
        val targetYear = targetMonthDate.year

        // Jika bulan target sebelum langganan dimulai, abaikan
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

    /**
     * Menghitung total biaya untuk bulan tertentu
     * @param onlyConfirmed Jika true, hanya menghitung yang sudah ditandai bayar. 
     *                      Jika false, menghitung seluruh tagihan yang seharusnya dibayar (estimasi).
     */
    private fun calculateTotalSpendForMonth(
        activeSubs: List<Subscription>,
        endedSubs: List<Subscription>,
        targetMonthDate: LocalDate,
        onlyConfirmed: Boolean = true
    ): Double {
        var total = 0.0
        val targetMonthStart = targetMonthDate.withDayOfMonth(1)

        fun getSpendingForSub(sub: Subscription): Double {
            val createdMonthStart = sub.startDate.withDayOfMonth(1)
            if (targetMonthStart.isBefore(createdMonthStart)) return 0.0
            
            val paymentDates = getPaymentDatesInMonth(sub, targetMonthDate)
            var subTotal = 0.0
            for (date in paymentDates) {
                // Untuk langganan yang sudah berhenti (ENDED), hanya hitung sampai tanggal berhentinya
                val isWithinActivePeriod = if (sub.status.name == "ENDED") {
                    !date.isAfter(sub.nextPaymentDate)
                } else {
                    true
                }

                if (isWithinActivePeriod) {
                    val isConfirmed = sub.confirmedPaymentDates.split(",").contains(date.toString())
                    if (!onlyConfirmed || isConfirmed) {
                        subTotal += sub.price
                    }
                }
            }
            return subTotal
        }

        for (sub in activeSubs) total += getSpendingForSub(sub)
        for (sub in endedSubs) total += getSpendingForSub(sub)

        return total
    }

    private fun calculatePastMonthsSpending(
        activeSubs: List<Subscription>,
        endedSubs: List<Subscription>
    ): List<MonthlySpend> {
        val allSubs = activeSubs + endedSubs
        if (allSubs.isEmpty()) return emptyList()

        // Ambil semua bulan unik dari daftar tanggal pembayaran yang sudah dikonfirmasi
        val confirmedMonths = allSubs.flatMap { sub ->
            sub.confirmedPaymentDates.split(",")
                .filter { it.isNotBlank() }
                .mapNotNull { 
                    try { LocalDate.parse(it).withDayOfMonth(1) } catch (e: Exception) { null }
                }
        }.toSet().sorted()

        val monthNameFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("id-ID"))
        val result = mutableListOf<MonthlySpend>()

        for (monthDate in confirmedMonths) {
            val totalForMonth = calculateTotalSpendForMonth(activeSubs, endedSubs, monthDate, onlyConfirmed = true)
            if (totalForMonth > 0.0) {
                result.add(MonthlySpend(monthDate.format(monthNameFormatter), totalForMonth))
            }
        }

        return result
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            repository.getAllSubscriptions()
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.localizedMessage) }
                }
                .collect { subs ->
                    val stats = getDashboardStatsUseCase(subs)

                    // Debug Log sesuai permintaan
                    subs.forEach { sub ->
                        android.util.Log.d("DASH_SUBS", "name=${sub.name} price=${sub.price} cycle=${sub.billingCycle} nextPayment=${sub.nextPaymentDate}")
                    }

                    val endedSubs = try {
                        repository.getSubscriptionHistory().first()
                    } catch (e: Exception) {
                        emptyList()
                    }

                    // 1. Perhitungan Total Konsumsi Bulan Ini (Estimasi dari subscriptions aktif)
                    val activeSubs = subs.filter { it.status.name == "ACTIVE" || it.status.name == "TRIAL" }
                    val totalThisMonth = calculateTotalSpendForMonth(activeSubs, endedSubs, LocalDate.now(), onlyConfirmed = false)

                    // 2. Perhitungan Riwayat (Hanya yang sudah terkonfirmasi bayar)
                    val monthlyHistory = calculatePastMonthsSpending(activeSubs, endedSubs)
                    val lifetimeSpend = monthlyHistory.sumOf { it.amount }

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
                    
                    val currentSubs = _uiState.value.rawSubscriptions
                    val activeSubs = currentSubs.filter { it.status.name == "ACTIVE" || it.status.name == "TRIAL" }
                    val monthlyHistory = calculatePastMonthsSpending(activeSubs, history)
                    val lifetimeSpend = monthlyHistory.sumOf { it.amount }
                    
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

    /**
     * Menandai tagihan tertua yang belum dibayar sebagai lunas.
     */
    fun markAsPaid(subscription: Subscription) {
        viewModelScope.launch {
            val unconfirmed = subscription.getUnconfirmedPaymentDates()
            val dateToConfirm = unconfirmed.firstOrNull() ?: subscription.nextPaymentDate
            val updatedSub = subscription.confirmPaymentDate(dateToConfirm)
            repository.insertSubscription(updatedSub)
        }
    }

    /**
     * Melompati seluruh tagihan tertunda dan menandai bulan ini sebagai lunas.
     */
    fun skipAndConfirmCurrent(subscription: Subscription) {
        viewModelScope.launch {
            val updatedSub = subscription.skipOverdueAndConfirmCurrent()
            repository.insertSubscription(updatedSub)
        }
    }
}
