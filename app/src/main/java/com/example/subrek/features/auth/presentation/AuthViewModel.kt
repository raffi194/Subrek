package com.example.subrek.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
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
    object ChangePasswordSuccess : AuthState
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

    private fun checkSession() {
        viewModelScope.launch {
            try {
                val currentSession = (supabaseClient.auth.sessionStatus.value as? SessionStatus.Authenticated)?.session
                _isUserLoggedIn.value = currentSession != null
            } catch (e: Exception) {
                _isUserLoggedIn.value = false
            }
        }
    }

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

    fun changePassword(newPassword: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                supabaseClient.auth.updateUser {
                    password = newPassword
                }
                _authState.value = AuthState.ChangePasswordSuccess
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Gagal mengubah password")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
