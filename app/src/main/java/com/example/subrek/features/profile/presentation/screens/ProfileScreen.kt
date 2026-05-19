package com.example.subrek.features.profile.presentation.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.subrek.core.theme.Slate950
import com.example.subrek.features.profile.presentation.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var fullNameInput by remember(uiState.fullName) { mutableStateOf(uiState.fullName ?: "") }
    var localSelectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher untuk mendeteksi user memilih file gambar dari galeri HP
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) localSelectedImageUri = uri
    }

    LaunchedEffect(uiState.isUpdateSuccess) {
        if (uiState.isUpdateSuccess) {
            viewModel.resetUpdateStatus()
            // Reset local selection after success
            localSelectedImageUri = null
        }
    }
    
    if (uiState.isLoggedOut) {
        LaunchedEffect(Unit) {
            onNavigateToLogin()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Profil Pengguna", fontWeight = FontWeight.Bold) })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Slate950)
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ---- SEKTOR FOTO PROFIL (AVATAR) ----
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier.size(120.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { imagePickerLauncher.launch("image/*") }, // Membuka file picker gambar
                    contentAlignment = Alignment.Center
                ) {
                    if (localSelectedImageUri != null) {
                        // Tampilkan pratinjau lokal file gambar yang baru saja dipilih
                        AsyncImage(
                            model = localSelectedImageUri,
                            contentDescription = "Preview Foto Profil",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else if (!uiState.avatarUrl.isNullOrEmpty()) {
                        // Tampilkan hasil string URL gambar dari database
                        AsyncImage(
                            model = uiState.avatarUrl,
                            contentDescription = "Foto Profil User",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Placeholder jika nama/foto profil user masih kosong
                        Text(
                            text = fullNameInput.take(1).uppercase().ifBlank { "U" },
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Icon Edit overlay
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Text(
                text = "Klik foto untuk mengganti file gambar",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ---- KOLOM EMAIL (READ-ONLY / TIDAK BISA DIUBAH) ----
            OutlinedTextField(
                value = uiState.email,
                onValueChange = {},
                readOnly = true, // Dikunci total
                label = { Text("Alamat Email (Permanen)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Gray,
                    unfocusedBorderColor = Color.DarkGray,
                    disabledTextColor = Color.LightGray
                )
            )

            // ---- KOLOM NAMA LENGKAP (DAPAT DIUBAH DINAMIS) ----
            OutlinedTextField(
                value = fullNameInput,
                onValueChange = { fullNameInput = it },
                label = { Text("Nama Lengkap") },
                placeholder = { Text("Masukkan nama lengkap Anda") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            // ---- TOMBOL SIMPAN UTAMA ----
            Button(
                onClick = {
                    viewModel.updateProfileData(
                        newFullName = fullNameInput,
                        newImageUri = localSelectedImageUri
                    )
                },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Perbarui Profil", fontWeight = FontWeight.Bold)
                }
            }
            
            // ---- TOMBOL LOGOUT ----
            TextButton(
                onClick = { viewModel.logout() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Keluar Akun", fontWeight = FontWeight.Bold)
            }
        }
    }
}
