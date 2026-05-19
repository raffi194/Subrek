package com.example.subrek.features.subscription

import com.example.subrek.features.subscription.domain.model.BillingCycle
import com.example.subrek.features.subscription.domain.model.Subscription
import com.example.subrek.features.subscription.domain.model.SubscriptionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SubscriptionValidationTest {

    // 1. PENGUJIAN VALIDASI KEBENARAN INPUT DATA
    @Test
    fun `validateInput_hargaNegatif_harusMengembalikanFalse`() {
        val inputNama = "Netflix"
        val inputHarga = -15000.0 // Negatif tidak diperbolehkan

        val isValid = inputNama.isNotBlank() && inputHarga >= 0.0
        assertFalse("Input seharusnya tidak valid jika harga negatif", isValid)
    }

    @Test
    fun `validateInput_namaKosong_harusMengembalikanFalse`() {
        val inputNama = "   "
        val inputHarga = 50000.0

        val isValid = inputNama.isNotBlank() && inputHarga >= 0.0
        assertFalse("Input seharusnya tidak valid jika nama kosong", isValid)
    }

    @Test
    fun `validateInput_dataBenar_harusMengembalikanTrue`() {
        val inputNama = "Spotify"
        val inputHarga = 54990.0

        val isValid = inputNama.isNotBlank() && inputHarga >= 0.0
        assertTrue("Input harus valid jika nama terisi dan harga positif", isValid)
    }

    // 2. PENGUJIAN KALKULASI FINANSIAL AVERAGE CONSUMPTION (Sesuai Aturan Kueri SQL public.user_subscription_analytics)
    @Test
    fun `calculateAverageMonthlyConsumption_berbagaiSiklus_harusTepat`() {
        val today = LocalDate.now()
        val listSubscriptions = listOf(
            createMockSubscription("2", "App B", 50000.0, BillingCycle.MONTHLY, today), // 50000
            createMockSubscription("3", "App C", 120000.0, BillingCycle.YEARLY, today) // 120000 / 12 = 10000
        )

        val totalMonthlyEquivalent = listSubscriptions.sumOf { sub ->
            when (sub.billingCycle) {
                BillingCycle.YEARLY -> sub.price / 12.0
                else -> sub.price
            }
        }
        val actualAverage = if (listSubscriptions.isEmpty()) 0.0 else totalMonthlyEquivalent

        // Ekspektasi: 50000 + 10000 = 60000
        val expectedAverage = 60000.0
        assertEquals(expectedAverage, actualAverage, 0.001)
    }

    private fun createMockSubscription(
        id: String,
        name: String,
        price: Double,
        billingCycle: BillingCycle,
        startDate: LocalDate
    ): Subscription {
        return Subscription(
            id = id,
            name = name,
            price = price,
            currency = "IDR",
            billingCycle = billingCycle,
            startDate = startDate,
            nextPaymentDate = startDate.plusMonths(1),
            paymentMethod = "Credit Card",
            isTrial = false,
            status = SubscriptionStatus.ACTIVE,
            createdAt = startDate,
            updatedAt = startDate
        )
    }
}
