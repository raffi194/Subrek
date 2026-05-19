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
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.subrek.core.theme.Rose500
import com.example.subrek.core.theme.Slate400
import com.example.subrek.core.theme.Slate800
import com.example.subrek.core.theme.Slate900
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
    var showEditSheet by remember { mutableStateOf(false) }

    // Navigasi ke login setelah logout
    if (uiState.isLoggedOut) {
        LaunchedEffect(Unit) { onNavigateToLogin() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Slate950,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Slate950)
                .padding(paddingValues)
        ) {

            // ---- BAGIAN 1: HEADER PROFIL (Foto, Nama, Email) ----
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900)
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Foto Profil
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(Slate800),
                    contentAlignment = Alignment.Center
                ) {
                    if (!uiState.avatarUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = uiState.avatarUrl,
                            contentDescription = "Foto Profil",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = (uiState.fullName?.takeIf { it.isNotEmpty() } ?: uiState.email)
                                .take(1).uppercase().ifBlank { "U" },
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // Nama Lengkap
                Text(
                    text = uiState.fullName?.takeIf { it.isNotEmpty() } ?: "Pengguna",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Email — ukuran lebih kecil, warna muted
                Text(
                    text = uiState.email,
                    fontSize = 13.sp,
                    color = Slate400
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ---- BAGIAN 2: ITEM LIST MENU ----
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                // Item: Edit Profile
                ProfileMenuItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    label = "Edit Profil",
                    sublabel = "Perbarui nama dan foto profil",
                    onClick = { showEditSheet = true }
                )

                // Item: Logout
                ProfileMenuItem(
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = Rose500
                        )
                    },
                    label = "Keluar Akun",
                    sublabel = "Logout dari sesi aktif",
                    labelColor = Rose500,
                    onClick = { viewModel.logout() }
                )
            }
        }

        // ---- BAGIAN 3: BOTTOM SHEET EDIT PROFIL ----
        if (showEditSheet) {
            EditProfileBottomSheet(
                currentName = uiState.fullName ?: "",
                currentAvatarUrl = uiState.avatarUrl,
                isLoading = uiState.isLoading,
                onDismiss = { showEditSheet = false },
                onConfirm = { newName, newUri ->
                    viewModel.updateProfileData(newFullName = newName, newImageUri = newUri)
                }
            )
        }

        // Tutup sheet & tampilkan snackbar setelah update sukses
        LaunchedEffect(uiState.isUpdateSuccess) {
            if (uiState.isUpdateSuccess) {
                showEditSheet = false
                viewModel.resetUpdateStatus()
            }
        }
    }
}

// =============================================================================
// Komponen: Item baris menu profil
// =============================================================================
@Composable
private fun ProfileMenuItem(
    icon: @Composable () -> Unit,
    label: String,
    sublabel: String,
    labelColor: Color = MaterialTheme.colorScheme.onBackground,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Slate900)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = labelColor)
            Text(sublabel, fontSize = 12.sp, color = Slate400)
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Slate400,
            modifier = Modifier.size(20.dp)
        )
    }
}

// =============================================================================
// Komponen: Bottom Sheet Edit Profil
// =============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileBottomSheet(
    currentName: String,
    currentAvatarUrl: String?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, Uri?) -> Unit
) {
    var nameInput by remember { mutableStateOf(currentName) }
    var localImageUri by remember { mutableStateOf<Uri?>(null) }
    val hasChanges = nameInput != currentName || localImageUri != null

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) localImageUri = uri
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Edit Profil", fontWeight = FontWeight.Bold, fontSize = 18.sp)

            // Avatar preview + tombol ganti foto
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier.size(96.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Slate800)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        localImageUri != null -> AsyncImage(
                            model = localImageUri,
                            contentDescription = "Preview",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        !currentAvatarUrl.isNullOrEmpty() -> AsyncImage(
                            model = currentAvatarUrl,
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        else -> Text(
                            text = nameInput.take(1).uppercase().ifBlank { "U" },
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Text(
                text = "Ketuk foto untuk mengganti",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )

            // Field Nama Lengkap
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Nama Lengkap") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Tombol Konfirmasi / Kembali
            Button(
                onClick = {
                    if (hasChanges) {
                        onConfirm(nameInput, localImageUri)
                    } else {
                        onDismiss()
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = if (hasChanges) "Konfirmasi Perubahan" else "Kembali",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
