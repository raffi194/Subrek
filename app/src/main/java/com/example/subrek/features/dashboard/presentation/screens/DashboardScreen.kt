package com.example.subrek.features.dashboard.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.subrek.core.theme.*
import com.example.subrek.core.utils.UiState
import com.example.subrek.features.dashboard.presentation.viewmodel.DashboardViewModel
import com.example.subrek.features.dashboard.presentation.viewmodel.SortOption
import com.example.subrek.features.subscription.domain.model.Subscription
import com.example.subrek.features.subscription.domain.model.SubscriptionStatus
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToAddSubscription: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val categories = listOf("Semua", "Hiburan", "Productivity", "Utilitas", "Kesehatan", "Finansial")

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Subrek", fontWeight = FontWeight.Black, letterSpacing = (-1).sp) },
                actions = {
                    IconButton(onClick = { viewModel.triggerSync() }) {
                        Icon(Icons.Default.Refresh, "Sync Cloud")
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = Slate950)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddSubscription, containerColor = Blue600, contentColor = MaterialTheme.colorScheme.onPrimary) {
                Icon(Icons.Default.Add, "Tambah Layanan")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Slate950)
                .padding(paddingValues)
        ) {
            // 1. RINGKASAN PENGELUARAN BULANAN (IDR CARD) DI BAGIAN ATAS
            when (val stats = state.statsState) {
                is UiState.Success -> {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate900)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text("Estimasi Pengeluaran Bulanan", color = Slate400, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            val formattedPrice = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).format(stats.data.totalMonthlySpend)
                            Text(formattedPrice, color = MaterialTheme.colorScheme.onBackground, fontSize = 28.sp, fontWeight = FontWeight.Black)
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Segera Tagihan", color = Slate400, fontSize = 11.sp)
                                    Text("${stats.data.upcomingBillsCount} Layanan", color = Amber500, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Termahal", color = Slate400, fontSize = 11.sp)
                                    Text(stats.data.mostExpensiveSubscription ?: "-", color = Rose500, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
                is UiState.Loading -> Box(modifier = Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Blue500) }
                else -> Unit
            }

            // 2. FILTER & SORT BAR (LOGIKA PENYARINGAN CHIPS)
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = state.selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.changeCategoryFilter(cat) },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Blue600,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = Slate900,
                            labelColor = Slate400
                        )
                    )
                }
            }

            // 3. LAZYCOLUMN MENAMPILKAN DAFTAR SUBSCRIPTION AKTIF LENGKAP BADGE DINAMIS
            if (state.subscriptionsList.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Tidak ada langganan aktif ditemukan", color = Slate400, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.subscriptionsList, key = { it.id }) { item ->
                        SubscriptionItemRow(item)
                    }
                }
            }
        }
    }
}

@Composable
fun SubscriptionItemRow(sub: Subscription) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Slate900, shape = RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(0.6f)) {
            Text(sub.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
            Text("${sub.category} • ${sub.paymentMethod}", fontSize = 12.sp, color = Slate400)
            Spacer(modifier = Modifier.height(8.dp))
            
            // LOGIKA DETEKSI BADGE STATUS JATUH TEMPO DINAMIS
            val today = LocalDate.now()
            val daysDiff = ChronoUnit.DAYS.between(today, sub.nextPaymentDate)
            
            when {
                sub.status == SubscriptionStatus.TRIAL -> {
                    BadgeCard(text = "Trial Berakhir", containerColor = Amber500.copy(alpha = 0.15f), textColor = Amber500)
                }
                daysDiff == 0L -> {
                    BadgeCard(text = "Hari Ini", containerColor = Rose500.copy(alpha = 0.15f), textColor = Rose500)
                }
                daysDiff in 1..3 -> {
                    BadgeCard(text = "$daysDiff Hari Lagi", containerColor = Rose500.copy(alpha = 0.15f), textColor = Rose500)
                }
                daysDiff in 4..7 -> {
                    BadgeCard(text = "Segera Jatuh Tempo", containerColor = Amber500.copy(alpha = 0.15f), textColor = Amber500)
                }
                else -> {
                    val formattedDate = sub.nextPaymentDate.format(DateTimeFormatter.ofPattern("dd MMM"))
                    BadgeCard(text = "Tempo $formattedDate", containerColor = Emerald500.copy(alpha = 0.15f), textColor = Emerald500)
                }
            }
        }

        Column(modifier = Modifier.weight(0.4f), horizontalAlignment = Alignment.End) {
            val formattedPrice = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).format(sub.price)
            Text(formattedPrice, fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
            Text("/${sub.billingCycle.name.lowercase()}", fontSize = 11.sp, color = Slate400)
        }
    }
}

@Composable
fun BadgeCard(text: String, containerColor: androidx.compose.ui.graphics.Color, textColor: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .background(containerColor, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textColor)
    }
}
