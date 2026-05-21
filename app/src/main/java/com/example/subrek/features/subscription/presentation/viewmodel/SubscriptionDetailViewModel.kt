package com.example.subrek.features.subscription.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subrek.features.subscription.domain.model.Subscription
import com.example.subrek.features.subscription.domain.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val subscription: Subscription? = null,
    val isLoading: Boolean = false,
    val isUpdateSuccess: Boolean = false,
    val isTerminationSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class SubscriptionDetailViewModel @Inject constructor(
    private val repository: SubscriptionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Mengambil ID parameter dari argumen navigasi Compose
    private val subscriptionId: String = checkNotNull(savedStateHandle["subscriptionId"])

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadSubscriptionDetail()
    }

    private fun loadSubscriptionDetail() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            repository.getSubscriptionByIdFlow(subscriptionId)
                .catch { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.localizedMessage) }
                }
                .collect { subscription ->
                    _uiState.update { it.copy(subscription = subscription, isLoading = false) }
                }
        }
    }

    fun updateBillingDetails(
        price: Double,
        billingCycle: String,
        startDate: String,
        paymentMethod: String,
        isTrial: Boolean
    ) {
        viewModelScope.launch {
            try {
                val status = if (isTrial) "TRIAL" else "ACTIVE"
                repository.updateSubscriptionBilling(subscriptionId, price, billingCycle, startDate, paymentMethod, isTrial, status)
                _uiState.update { it.copy(isUpdateSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.localizedMessage) }
            }
        }
    }

    fun terminateSubscriptionService() {
        viewModelScope.launch {
            try {
                repository.terminateSubscription(subscriptionId)
                _uiState.update { it.copy(isTerminationSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.localizedMessage) }
            }
        }
    }
}
