package com.example.subrek.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan_tennert.supabase.SupabaseClient
import io.github.jan_tennert.supabase.auth.auth
import io.github.jan_tennert.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

sealed interface AuthState {
    object Idle : AuthState
    object Loading : AuthState
    object Authenticated : AuthState
    data class Error(val message: String) : AuthState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isUserLoggedIn = MutableStateFlow<Boolean?>(null)
    val isUserLoggedIn: StateFlow<Boolean?> = _isUserLoggedIn.asStateFlow()

    init {
        checkSession()
    }

    // Memeriksa apakah ada sesi aktif yang tersimpan (Bypass untuk User Lama)
    private fun checkSession() {
        viewModelScope.launch {
            try {
                val currentSession = supabaseClient.auth.currentSessionOrNull()
                _isUserLoggedIn.value = currentSession != null
            } catch (e: Exception) {
                _isUserLoggedIn.value = false
            }
        }
    }

    // Fungsi Register menggunakan Email & Password
    fun register(emailInput: String, passwordInput: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                supabaseClient.auth.signUpWith(Email) {
                    email = emailInput
                    password = passwordInput
                }
                _authState.value = AuthState.Authenticated
                _isUserLoggedIn.value = true
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Registrasi Gagal")
            }
        }
    }

    // Fungsi Login menggunakan Email & Password
    fun login(emailInput: String, passwordInput: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                supabaseClient.auth.signInWith(Email) {
                    email = emailInput
                    password = passwordInput
                }
                _authState.value = AuthState.Authenticated
                _isUserLoggedIn.value = true
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Login Gagal")
            }
        }
    }
    
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
