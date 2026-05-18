package com.example.subrek.features.auth.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.subrek.features.auth.presentation.AuthState
import com.example.subrek.features.auth.presentation.AuthViewModel

private enum class AuthMode { LOGIN, REGISTER, CHANGE_PASSWORD }

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: () -> Unit
) {
    var authMode by remember { mutableStateOf(AuthMode.LOGIN) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmNewPasswordVisible by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onAuthSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = when (authMode) {
                AuthMode.LOGIN -> "Selamat Datang Kembali"
                AuthMode.REGISTER -> "Buat Akun Baru"
                AuthMode.CHANGE_PASSWORD -> "Ubah Password"
            },
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        when (authMode) {
            AuthMode.LOGIN -> {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, "Toggle")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            AuthMode.REGISTER -> {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, "Toggle")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Konfirmasi Password") },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, "Toggle")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            AuthMode.CHANGE_PASSWORD -> {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Akun") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Password Baru") },
                    visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                            Icon(if (newPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, "Toggle")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = confirmNewPassword,
                    onValueChange = { confirmNewPassword = it },
                    label = { Text("Konfirmasi Password Baru") },
                    visualTransformation = if (confirmNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmNewPasswordVisible = !confirmNewPasswordVisible }) {
                            Icon(if (confirmNewPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, "Toggle")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (authState is AuthState.Error) {
            Text(
                text = (authState as AuthState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        if (authState is AuthState.ChangePasswordSuccess) {
            Text(
                text = "Password berhasil diubah! Silakan login kembali.",
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Button(
            onClick = {
                when (authMode) {
                    AuthMode.LOGIN -> viewModel.login(email, password)
                    AuthMode.REGISTER -> {
                        if (password == confirmPassword) viewModel.register(email, password)
                    }
                    AuthMode.CHANGE_PASSWORD -> {
                        if (newPassword == confirmNewPassword) viewModel.changePassword(newPassword)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = authState !is AuthState.Loading
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            } else {
                Text(when (authMode) {
                    AuthMode.LOGIN -> "Login"
                    AuthMode.REGISTER -> "Register"
                    AuthMode.CHANGE_PASSWORD -> "Ubah Password"
                })
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (authMode) {
            AuthMode.LOGIN -> {
                TextButton(onClick = { authMode = AuthMode.REGISTER; viewModel.resetState() }) {
                    Text("Belum punya akun? Register disini")
                }
                TextButton(onClick = { authMode = AuthMode.CHANGE_PASSWORD; viewModel.resetState() }) {
                    Text("Lupa / Ubah Password")
                }
            }
            AuthMode.REGISTER -> {
                TextButton(onClick = { authMode = AuthMode.LOGIN; viewModel.resetState() }) {
                    Text("Sudah punya akun? Login disini")
                }
            }
            AuthMode.CHANGE_PASSWORD -> {
                TextButton(onClick = { authMode = AuthMode.LOGIN; viewModel.resetState() }) {
                    Text("Kembali ke Login")
                }
            }
        }
    }
}
