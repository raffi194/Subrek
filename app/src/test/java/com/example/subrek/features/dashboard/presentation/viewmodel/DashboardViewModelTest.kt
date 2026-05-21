package com.example.subrek.features.dashboard.presentation.viewmodel

import com.example.subrek.features.dashboard.domain.usecase.GetDashboardStatsUseCase
import com.example.subrek.features.subscription.domain.model.BillingCycle
import com.example.subrek.features.subscription.domain.model.Subscription
import com.example.subrek.features.subscription.domain.model.SubscriptionStatus
import com.example.subrek.features.subscription.domain.repository.SubscriptionRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private lateinit var viewModel: DashboardViewModel
    private val repository = mockk<SubscriptionRepository>(relaxed = true)
    private val getDashboardStatsUseCase = GetDashboardStatsUseCase()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadDashboardData - check dual-mode spending calculation`() = runTest {
        val today = LocalDate.now()
        val startOfThisMonth = today.withDayOfMonth(1)

        val activeSub = createSubscription(
            id = "1",
            name = "Netflix",
            price = 50000.0,
            billingCycle = BillingCycle.MONTHLY,
            startDate = startOfThisMonth,
            status = SubscriptionStatus.ACTIVE,
            confirmedPaymentDates = ""
        )

        val endedSub = createSubscription(
            id = "2",
            name = "Spotify Old",
            price = 30000.0,
            billingCycle = BillingCycle.MONTHLY,
            startDate = startOfThisMonth.minusMonths(1),
            status = SubscriptionStatus.ENDED,
            confirmedPaymentDates = startOfThisMonth.toString()
        )

        every { repository.getAllSubscriptions() } returns flowOf(listOf(activeSub))
        every { repository.getSubscriptionHistory() } returns flowOf(listOf(endedSub))
        every { repository.getActiveSubscriptionsCount() } returns flowOf(1)

        viewModel = DashboardViewModel(repository, getDashboardStatsUseCase)
        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertEquals(80000.0, state.totalConsumptionThisMonth, 0.1)

        assertEquals(30000.0, state.lifetimeSpending, 0.1)
    }

    @Test
    fun `markAsPaid - should update confirmed dates and increase lifetime spending`() = runTest {
        val today = LocalDate.now()
        val startOfThisMonth = today.withDayOfMonth(1)
        
        val activeSub = createSubscription(
            id = "1",
            name = "Netflix",
            price = 50000.0,
            billingCycle = BillingCycle.MONTHLY,
            startDate = startOfThisMonth,
            status = SubscriptionStatus.ACTIVE,
            confirmedPaymentDates = ""
        )

        every { repository.getAllSubscriptions() } returns flowOf(listOf(activeSub))
        every { repository.getSubscriptionHistory() } returns flowOf(emptyList())
        every { repository.getActiveSubscriptionsCount() } returns flowOf(1)

        viewModel = DashboardViewModel(repository, getDashboardStatsUseCase)
        advanceUntilIdle()

        assertEquals(50000.0, viewModel.uiState.value.totalConsumptionThisMonth, 0.1)
        assertEquals(0.0, viewModel.uiState.value.lifetimeSpending, 0.1)

        viewModel.markAsPaid(activeSub)
        advanceUntilIdle()

        val confirmedSub = activeSub.confirmPaymentDate(startOfThisMonth)
        every { repository.getAllSubscriptions() } returns flowOf(listOf(confirmedSub))
        
        viewModel.loadDashboardData()
        advanceUntilIdle()

        val updatedState = viewModel.uiState.value
        assertEquals(50000.0, updatedState.totalConsumptionThisMonth, 0.1)
        assertEquals(50000.0, updatedState.lifetimeSpending, 0.1)
    }

    @Test
    fun `calculateTotalSpendForMonth - check weekly subscription calculation`() = runTest {
        val today = LocalDate.of(2026, 5, 21) // Arbitrary date
        val startOfThisMonth = today.withDayOfMonth(1) // 2026-05-01

        val weeklySub = createSubscription(
            id = "3",
            name = "Weekly App",
            price = 10000.0,
            billingCycle = BillingCycle.WEEKLY,
            startDate = LocalDate.of(2026, 5, 1),
            status = SubscriptionStatus.ACTIVE
        )

        every { repository.getAllSubscriptions() } returns flowOf(listOf(weeklySub))
        every { repository.getSubscriptionHistory() } returns flowOf(emptyList())
        every { repository.getActiveSubscriptionsCount() } returns flowOf(1)

        viewModel = DashboardViewModel(repository, getDashboardStatsUseCase)
        
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertTrue("Weekly consumption should be calculated", state.totalConsumptionThisMonth > 0)
        assertEquals(0.0, state.totalConsumptionThisMonth % 10000.0, 0.001) // Should be multiple of price
    }

    private fun createSubscription(
        id: String,
        name: String,
        price: Double,
        billingCycle: BillingCycle,
        startDate: LocalDate,
        status: SubscriptionStatus,
        confirmedPaymentDates: String = ""
    ): Subscription {
        return Subscription(
            id = id,
            name = name,
            price = price,
            currency = "IDR",
            billingCycle = billingCycle,
            startDate = startDate,
            nextPaymentDate = startDate.plusMonths(1),
            paymentMethod = "Gopay",
            isTrial = false,
            status = status,
            createdAt = startDate,
            updatedAt = LocalDate.now(),
            confirmedPaymentDates = confirmedPaymentDates
        )
    }
}
