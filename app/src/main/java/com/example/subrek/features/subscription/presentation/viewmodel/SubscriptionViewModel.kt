package com.example.subrek.features.subscription.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subrek.core.utils.UiState
import com.example.subrek.features.subscription.domain.model.Subscription
import com.example.subrek.features.subscription.domain.usecase.GetSubscriptionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val getSubscriptionsUseCase: GetSubscriptionsUseCase
) : ViewModel() {

    private val _subscriptionsState = MutableStateFlow<UiState<List<Subscription>>>(UiState.Idle)
    val subscriptionsState: StateFlow<UiState<List<Subscription>>> = _subscriptionsState.asStateFlow()

    init {
        loadSubscriptions()
    }

    fun loadSubscriptions() {
        viewModelScope.launch {
            _subscriptionsState.value = UiState.Loading
            getSubscriptionsUseCase()
                .catch { e ->
                    _subscriptionsState.value = UiState.Error(e.message ?: "Unknown Error")
                }
                .collect { list ->
                    _subscriptionsState.value = UiState.Success(list)
                }
        }
    }
}
