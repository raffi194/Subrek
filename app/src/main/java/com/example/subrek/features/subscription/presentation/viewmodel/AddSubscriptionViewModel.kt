package com.example.subrek.features.subscription.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subrek.features.subscription.domain.model.BillingCycle
import com.example.subrek.features.subscription.domain.model.Subscription
import com.example.subrek.features.subscription.domain.model.SubscriptionStatus
import com.example.subrek.features.subscription.domain.usecase.AddSubscriptionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

data class AddSubscriptionFormState(
    val name: String = "",
    val nameError: String? = null,
    val price: String = "",
    val priceError: String? = null,
    val currency: String = "IDR",
    val category: String = "Hiburan",
    val paymentMethod: String = "Kartu Kredit",
    val billingCycle: BillingCycle = BillingCycle.MONTHLY,
    val firstPaymentDate: LocalDate = LocalDate.now(),
    val isTrial: Boolean = false,
    val isLoading: Boolean = false
)

sealed class AddSubscriptionUiEvent {
    object Success : AddSubscriptionUiEvent()
    data class Error(val message: String) : AddSubscriptionUiEvent()
}

@HiltViewModel
class AddSubscriptionViewModel @Inject constructor(
    private val addSubscriptionUseCase: AddSubscriptionUseCase
) : ViewModel() {

    private val _formState = MutableStateFlow(AddSubscriptionFormState())
    val formState: StateFlow<AddSubscriptionFormState> = _formState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<AddSubscriptionUiEvent>()
    val eventFlow: SharedFlow<AddSubscriptionUiEvent> = _eventFlow.asSharedFlow()

    fun onNameChange(newName: String) {
        val error = when {
            newName.isBlank() -> "Nama layanan tidak boleh kosong"
            newName.length > 100 -> "Nama layanan maksimal 100 karakter"
            else -> null
        }
        _formState.value = _formState.value.copy(name = newName, nameError = error)
    }

    fun onPriceChange(newPrice: String) {
        val cleanedPrice = newPrice.replace(",", "").replace(".", "")
        val priceValue = cleanedPrice.toDoubleOrNull()
        val error = when {
            newPrice.isBlank() -> "Harga tidak boleh kosong"
            priceValue == null || priceValue <= 0 -> "Harga harus lebih besar dari 0"
            else -> null
        }
        _formState.value = _formState.value.copy(price = cleanedPrice, priceError = error)
    }

    fun onCurrencyChange(newCurrency: String) {
        _formState.value = _formState.value.copy(currency = newCurrency)
    }

    fun onCategoryChange(newCategory: String) {
        _formState.value = _formState.value.copy(category = newCategory)
    }

    fun onPaymentMethodChange(newMethod: String) {
        _formState.value = _formState.value.copy(paymentMethod = newMethod)
    }

    fun onBillingCycleChange(newCycle: BillingCycle) {
        _formState.value = _formState.value.copy(billingCycle = newCycle)
    }

    fun onFirstPaymentDateChange(newDate: LocalDate) {
        _formState.value = _formState.value.copy(firstPaymentDate = newDate)
    }

    fun onTrialToggle(isTrial: Boolean) {
        _formState.value = _formState.value.copy(isTrial = isTrial)
    }

    fun saveSubscription() {
        // Trigger validasi akhir sebelum menyimpan
        onNameChange(_formState.value.name)
        onPriceChange(_formState.value.price)

        val currentState = _formState.value
        if (currentState.nameError != null || currentState.priceError != null || currentState.name.isBlank() || currentState.price.isBlank()) {
            return
        }

        viewModelScope.launch {
            _formState.value = currentState.copy(isLoading = true)
            try {
                val newSub = Subscription(
                    id = UUID.randomUUID().toString(),
                    name = currentState.name,
                    price = currentState.price.toDouble(),
                    currency = currentState.currency,
                    billingCycle = currentState.billingCycle,
                    startDate = currentState.firstPaymentDate,
                    nextPaymentDate = currentState.firstPaymentDate, // Inisialisasi awal pembayaran
                    category = currentState.category,
                    paymentMethod = currentState.paymentMethod,
                    isTrial = currentState.isTrial,
                    status = if (currentState.isTrial) SubscriptionStatus.TRIAL else SubscriptionStatus.ACTIVE,
                    createdAt = LocalDate.now(),
                    updatedAt = LocalDate.now()
                )
                
                addSubscriptionUseCase(newSub)
                _eventFlow.emit(AddSubscriptionUiEvent.Success)
            } catch (e: Exception) {
                _eventFlow.emit(AddSubscriptionUiEvent.Error(e.localizedMessage ?: "Gagal menyimpan data"))
            } finally {
                _formState.value = _formState.value.copy(isLoading = false)
            }
        }
    }
}
