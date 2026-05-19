package com.example.subrek.features.subscription.presentation.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.subrek.features.subscription.domain.model.CatalogItem
import com.example.subrek.features.subscription.presentation.viewmodel.TambahLanggananViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TambahLanggananScreen(
    viewModel: TambahLanggananViewModel,
    onSuccessSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedAppForForm by remember { mutableStateOf<CatalogItem?>(null) }
    
    var showAppDialog by remember { mutableStateOf(false) }

    // State Input Form Detail Berlangganan
    var priceInput by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("") }
    var selectedCycle by remember { mutableStateOf("MONTHLY") }
    var startDateInput by remember { mutableStateOf("") }
    var isFreeTrial by remember { mutableStateOf(false) }
    var isCycleDropdownExpanded by remember { mutableStateOf(false) }

    if (uiState.isSaveSuccess) {
        LaunchedEffect(Unit) {
            viewModel.resetSaveSuccess()
            selectedAppForForm = null
            onSuccessSave()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Langganan", fontWeight = FontWeight.Bold) },
                actions = {
                    // 🛠️ Button "+ Kategori" telah dihapus sepenuhnya di sini
                    IconButton(onClick = { showAppDialog = true }) { 
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah App Baru") 
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.statusBars
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .navigationBarsPadding()
            ) {
                if (selectedAppForForm == null) {
                    // 1. SEARCH FIELD
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = { Text("Cari spesifik app subscriptions...") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // 2. RENDERING LIST ITEM KATALOG
                    val filteredItems = uiState.catalogItems.filter { item ->
                        item.name.contains(uiState.searchQuery, ignoreCase = true)
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize().padding(top = 8.dp)
                    ) {
                        items(filteredItems, key = { it.id }) { app ->
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable { selectedAppForForm = app },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        AsyncImage(
                                            model = app.iconUrl ?: "https://placeholder.co/100",
                                            contentDescription = null,
                                            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp))
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(text = app.name, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
                                }
                            }
                        }
                    }
                } else {
                    // 3. FORM DETAIL BERLANGGANAN (DIBUKA SAAT KATALOG DI-KLIK)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize().padding(top = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                            AsyncImage(model = selectedAppForForm?.iconUrl ?: "https://placeholder.co/100", contentDescription = null, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = selectedAppForForm?.name ?: "", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }

                        OutlinedTextField(value = priceInput, onValueChange = { priceInput = it }, label = { Text("Biaya Berlangganan (Rp)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = paymentMethod, onValueChange = { paymentMethod = it }, label = { Text("Metode Pembayaran") }, modifier = Modifier.fillMaxWidth())

                        Box(modifier = Modifier.fillMaxWidth()) {
                            ExposedDropdownMenuBox(expanded = isCycleDropdownExpanded, onExpandedChange = { isCycleDropdownExpanded = !isCycleDropdownExpanded }) {
                                OutlinedTextField(
                                    value = selectedCycle, 
                                    onValueChange = {}, 
                                    readOnly = true, 
                                    label = { Text("Siklus Penagihan") }, 
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCycleDropdownExpanded) }, 
                                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                )
                                ExposedDropdownMenu(expanded = isCycleDropdownExpanded, onDismissRequest = { isCycleDropdownExpanded = false }) {
                                    listOf("MONTHLY", "YEARLY").forEach { opt ->
                                        DropdownMenuItem(text = { Text(opt) }, onClick = { selectedCycle = opt; isCycleDropdownExpanded = false })
                                    }
                                }
                            }
                        }

                        OutlinedTextField(value = startDateInput, onValueChange = { startDateInput = it }, label = { Text("Tanggal Mulai Penagihan (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())

                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Layanan Free Trial", fontWeight = FontWeight.Medium)
                            Switch(checked = isFreeTrial, onCheckedChange = { isFreeTrial = it })
                        }

                        Button(
                            onClick = {
                                val cleanPrice = priceInput.replace(",", ".").toDoubleOrNull() ?: 0.0
                                    viewModel.saveNewSubscription(
                                        name = selectedAppForForm!!.name,
                                        iconUrl = selectedAppForForm!!.iconUrl,
                                        price = cleanPrice,
                                        cycle = selectedCycle,
                                        date = startDateInput.ifBlank { "2026-01-01" },
                                        isTrial = isFreeTrial
                                    )
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp).padding(top = 8.dp)
                        ) {
                            Text("Simpan Langganan", fontWeight = FontWeight.Bold)
                        }
                        
                        TextButton(onClick = { selectedAppForForm = null }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Kembali ke Katalog") }
                    }
                }
            }

            // =========================================================================
            // DIALOG TAMBAHAN UNTUK ENTRI KUSTOM (DIALOG KATEGORI TELAH DIHAPUS)
            // =========================================================================
            if (showAppDialog) {
                var appName by remember { mutableStateOf("") }
                var appPrice by remember { mutableStateOf("") }
                var appCurrency by remember { mutableStateOf("IDR") }
                var appCycle by remember { mutableStateOf("MONTHLY") }
                var appPaymentMethod by remember { mutableStateOf("E-Wallet") }
                var appDate by remember { mutableStateOf("2026-05-19") }
                var isAppCycleExpanded by remember { mutableStateOf(false) }

                var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }

                val imagePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                    contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
                ) { uri: android.net.Uri? ->
                    selectedImageUri = uri
                }

                // 🛠️ PERBAIKAN: Integrasi Native DatePickerDialog untuk Form Kustom Dialog
                val calendar = Calendar.getInstance()
                val datePickerDialog = DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                        appDate = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )

                AlertDialog(
                    onDismissRequest = { showAppDialog = false },
                    title = { Text("Tambah Aplikasi Baru", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { imagePickerLauncher.launch("image/*") }
                                    .padding(vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (selectedImageUri != null) {
                                        AsyncImage(
                                            model = selectedImageUri,
                                            contentDescription = "Preview Icon",
                                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Pilih Gambar",
                                            tint = Color.Gray
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("Icon Aplikasi Profile", fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = if (selectedImageUri != null) "Gambar terpilih (Klik untuk ganti)" else "Klik untuk upload file gambar",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = appName,
                                onValueChange = { appName = it },
                                label = { Text("Nama Aplikasi *") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = appCurrency,
                                    onValueChange = { appCurrency = it },
                                    label = { Text("Valuta") },
                                    modifier = Modifier.weight(0.3f)
                                )
                                OutlinedTextField(
                                    value = appPrice,
                                    onValueChange = { appPrice = it },
                                    label = { Text("Biaya *") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(0.7f)
                                )
                            }

                            Box(modifier = Modifier.fillMaxWidth()) {
                                ExposedDropdownMenuBox(
                                    expanded = isAppCycleExpanded,
                                    onExpandedChange = { isAppCycleExpanded = !isAppCycleExpanded }
                                ) {
                                    OutlinedTextField(
                                        value = appCycle,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Siklus Penagihan *") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isAppCycleExpanded) },
                                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = isAppCycleExpanded,
                                        onDismissRequest = { isAppCycleExpanded = false }
                                    ) {
                                        listOf("MONTHLY", "YEARLY").forEach { opt ->
                                            DropdownMenuItem(
                                                text = { Text(opt) },
                                                onClick = { appCycle = opt; isAppCycleExpanded = false }
                                            )
                                        }
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = appPaymentMethod,
                                onValueChange = { appPaymentMethod = it },
                                label = { Text("Metode Pembayaran *") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            // 🛠️ PERBAIKAN: Mengubah input manual tanggal menjadi visual dialog picker
                            OutlinedTextField(
                                value = appDate,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Tanggal Mulai Penagihan *") },
                                trailingIcon = {
                                    IconButton(onClick = { datePickerDialog.show() }) {
                                        Icon(imageVector = Icons.Default.DateRange, contentDescription = "Pilih Tanggal")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() }
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (appName.isNotBlank() && appPrice.isNotBlank()) {
                                    val cleanPrice = appPrice.replace(",", ".").toDoubleOrNull() ?: 0.0

                                    viewModel.addCustomAppWithImage(
                                        name = appName,
                                        price = cleanPrice,
                                        currency = appCurrency.ifBlank { "IDR" },
                                        billingCycle = appCycle,
                                        paymentMethod = appPaymentMethod,
                                        nextPaymentDate = appDate.ifBlank { "2026-05-19" },
                                        imageUri = selectedImageUri
                                    )
                                    showAppDialog = false
                                }
                            }
                        ) {
                            Text("Simpan")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAppDialog = false }) { Text("Batal") }
                    }
                )
            }
        }
    }
}
