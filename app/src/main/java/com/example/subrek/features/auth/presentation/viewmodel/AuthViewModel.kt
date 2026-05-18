package com.example.subrek.features.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subrek.core.utils.UiState
import com.example.subrek.features.auth.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val authState = _authState.asStateFlow()

    fun register(email: String, pass: String, confirmPass: String) {
        if (pass != confirmPass) {
            _authState.value = UiState.Error("Konfirmasi password tidak cocok.")
            return
        }
        viewModelScope.launch {
            _authState.value = UiState.Loading
            authRepository.signUp(email, pass)
                .onSuccess { _authState.value = UiState.Success(Unit) }
                .onFailure { _authState.value = UiState.Error(it.message ?: "Registrasi Gagal") }
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = UiState.Loading
            authRepository.signIn(email, pass)
                .onSuccess { _authState.value = UiState.Success(Unit) }
                .onFailure { _authState.value = UiState.Error(it.message ?: "Login Gagal") }
        }
    }
    
    fun resetState() {
        _authState.value = UiState.Idle
    }
}
