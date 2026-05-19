package com.example.subrek.features.subscription.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.subrek.features.subscription.domain.model.CatalogItem
import com.example.subrek.features.subscription.presentation.viewmodel.TambahLanggananViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TambahLanggananScreen(
    viewModel: TambahLanggananViewModel,
    onSuccessSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedAppForForm by remember { mutableStateOf<CatalogItem?>(null) }
    
    var showCatDialog by remember { mutableStateOf(false) }
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
                    IconButton(onClick = { showCatDialog = true }) { Text("+Kategori", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }
                    IconButton(onClick = { showAppDialog = true }) { Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah App Baru") }
                }
            )
        },
        contentWindowInsets = WindowInsets.statusBars
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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

                // 2. FILTER BAR TABS KATEGORI (All paling kanan/ujung, disesuaikan urutannya)
                // Kategori default sudah ada di Room lewat seed, tidak perlu hardcode lagi
                val allCategories = uiState.customCategories + listOf("All")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    items(allCategories) { category ->
                        val isSelected = uiState.selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectCategory(category) },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                // 3. RENDERING LIST ITEM KATALOG
                val filteredItems = uiState.catalogItems.filter { item ->
                    (uiState.selectedCategory == "All" || item.categoryName == uiState.selectedCategory) &&
                    item.name.contains(uiState.searchQuery, ignoreCase = true)
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize().padding(top = 8.dp)
                ) {
                    items(filteredItems) { app ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { selectedAppForForm = app },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                                        Text(text = app.categoryName, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                }
                                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
                            }
                        }
                    }
                }
            } else {
                // 4. FORM DETAIL BERLANGGANAN (DIBUKA SAAT KATALOG DI-KLIK)
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

                    // Siklus Penagihan: 3 Opsi Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        ExposedDropdownMenuBox(expanded = isCycleDropdownExpanded, onExpandedChange = { isCycleDropdownExpanded = !isCycleDropdownExpanded }) {
                            OutlinedTextField(value = selectedCycle, onValueChange = {}, readOnly = true, label = { Text("Siklus Penagihan") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCycleDropdownExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true))
                            ExposedDropdownMenu(expanded = isCycleDropdownExpanded, onDismissRequest = { isCycleDropdownExpanded = false }) {
                                listOf("WEEKLY", "MONTHLY", "YEARLY").forEach { opt ->
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
                            viewModel.saveNewSubscription(
                                name = selectedAppForForm!!.name,
                                iconUrl = selectedAppForForm!!.iconUrl,
                                price = priceInput.toDoubleOrNull() ?: 0.0,
                                cycle = selectedCycle,
                                date = startDateInput,
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
        // DIALOG TAMBAHAN UNTUK ENTRI KUSTOM (DATA ISOLATION)
        // =========================================================================
        if (showCatDialog) {
            var catName by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showCatDialog = false },
                title = { Text("Tambah Kategori Baru") },
                text = { OutlinedTextField(value = catName, onValueChange = { catName = it }, label = { Text("Nama Kategori") }) },
                confirmButton = { Button(onClick = { if(catName.isNotBlank()){ viewModel.addCustomCategory(catName); showCatDialog = false } }) { Text("Tambah") } }
            )
        }

        if (showAppDialog) {
            var appName by remember { mutableStateOf("") }
            var appIcon by remember { mutableStateOf("") }
            var appCat by remember { mutableStateOf("Popular") }
            AlertDialog(
                onDismissRequest = { showAppDialog = false },
                title = { Text("Tambah Aplikasi Baru") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = appName, onValueChange = { appName = it }, label = { Text("Nama Aplikasi") })
                        OutlinedTextField(value = appIcon, onValueChange = { appIcon = it }, label = { Text("URL Link Icon Gambar") })
                        OutlinedTextField(value = appCat, onValueChange = { appCat = it }, label = { Text("Kategori Target") })
                    }
                },
                confirmButton = { Button(onClick = { if(appName.isNotBlank()){ viewModel.addCustomApp(appName, appIcon.ifBlank { null }, appCat); showAppDialog = false } }) { Text("Simpan") } }
            )
        }
    }
}
