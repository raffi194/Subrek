package com.example.subrek.features.dashboard.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.subrek.core.theme.*
import com.example.subrek.core.utils.UiState
import com.example.subrek.features.dashboard.presentation.components.DonutChartCategories
import com.example.subrek.features.dashboard.presentation.components.HorizontalBarChartMethods
import com.example.subrek.features.dashboard.presentation.viewmodel.DashboardViewModel
import com.example.subrek.features.profile.presentation.viewmodel.ProfileViewModel
import com.example.subrek.features.subscription.domain.model.Subscription
import com.example.subrek.features.subscription.domain.model.SubscriptionStatus
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    profileViewModel: ProfileViewModel,
    onNavigateToAddSubscription: () -> Unit,
    onNavigateToDetail: (String) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val profileState by profileViewModel.uiState.collectAsState()
    val categories = listOf("Semua", "Hiburan", "Productivity", "Utilitas", "Kesehatan", "Finansial")

    // Pemicu reaktif untuk memastikan data profil ter-refresh langsung dari database setiap screen aktif
    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Avatar profil user
                        if (!profileState.avatarUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = profileState.avatarUrl,
                                contentDescription = "Foto Profil",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Slate800)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Blue600, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (profileState.fullName?.takeIf { it.isNotEmpty() } ?: "U").take(1).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "Selamat datang,",
                                fontSize = 11.sp,
                                color = Slate400,
                                fontWeight = FontWeight.Normal
                            )
                            // Mengambil data full_name secara dinamis & real-time dari profileState database profiles
                            Text(
                                text = when {
                                    profileState.isLoading -> "Memuat..."
                                    !profileState.fullName.isNullOrBlank() -> profileState.fullName!!
                                    else -> "Pengguna"
                                },
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                actions = {
                    // Tombol lonceng notifikasi
                    IconButton(onClick = { /* TODO: navigate ke log notifikasi */ }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Log Notifikasi",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate950)
            )
        },
        contentWindowInsets = WindowInsets.statusBars
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Slate950)
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {

            // 1. KARTU ESTIMASI RINGKASAN BULANAN
            item {
                when (val stats = state.statsState) {
                    is UiState.Success -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Slate900)
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Text(
                                    "Estimasi Pengeluaran Bulanan",
                                    color = Slate400,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                val formattedPrice = NumberFormat
                                    .getCurrencyInstance(Locale.forLanguageTag("id-ID"))
                                    .format(stats.data.totalMonthlySpend)
                                Text(
                                    formattedPrice,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Segera Tagihan", color = Slate400, fontSize = 11.sp)
                                        Text(
                                            "${stats.data.upcomingBillsCount} Layanan",
                                            color = Amber500,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Termahal", color = Slate400, fontSize = 11.sp)
                                        Text(
                                            stats.data.mostExpensiveSubscription ?: "-",
                                            color = Rose500,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                    is UiState.Loading -> Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Blue500)
                    }
                    else -> Unit
                }
            }

            // 2. CHART VISUALISASI (hanya jika data >= 2)
            item {
                AnimatedVisibility(
                    visible = state.rawSubscriptions.size >= 2,
                    enter = fadeIn() + expandVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        DonutChartCategories(subscriptions = state.rawSubscriptions)
                        HorizontalBarChartMethods(subscriptions = state.rawSubscriptions)
                    }
                }
            }

            // 3. JUDUL DAFTAR AKTIF
            item {
                Text(
                    text = "Active Subscriptions",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                )
            }

            // 4. FILTER CHIPS
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
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
            }

            // 5. LIST AKTIF DENGAN SWIPE-TO-DELETE
            if (state.subscriptionsList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Tidak ada langganan aktif ditemukan",
                            color = Slate400,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(state.subscriptionsList, key = { it.id }) { item ->
                    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                        // ---------------------------------------------------------
                        // PERBAIKAN GAP 2: SwipeToDelete pada setiap item aktif
                        // ---------------------------------------------------------
                        SwipeToDeleteSubscriptionItem(
                            subscription = item,
                            onDelete = { viewModel.deleteSubscription(item.id) },
                            onClick = { onNavigateToDetail(item.id) }
                        )
                    }
                }
            }

            // -----------------------------------------------------------------
            // PERBAIKAN GAP 3: Card Riwayat Subscriptions di bagian bawah
            // -----------------------------------------------------------------
            if (state.subscriptionHistory.isNotEmpty()) {
                item {
                    Text(
                        text = "Riwayat Subscriptions",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(
                            start = 24.dp,
                            end = 24.dp,
                            top = 8.dp,
                            bottom = 4.dp
                        )
                    )
                }
                items(state.subscriptionHistory, key = { "history_${it.id}" }) { item ->
                    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                        SubscriptionHistoryItem(subscription = item)
                    }
                }
            }
        }
    }
}

// =============================================================================
// PERBAIKAN GAP 2: Composable SwipeToDelete
// Menggunakan draggable manual agar tidak perlu library tambahan.
// Threshold swipe kiri 120dp → tombol delete muncul, swipe habis → konfirmasi hapus.
// =============================================================================
@Composable
fun SwipeToDeleteSubscriptionItem(
    subscription: Subscription,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    val revealThreshold = -120.dp.value
    val dismissThreshold = -300.dp.value
    var showConfirmDialog by remember { mutableStateOf(false) }

    val animatedOffset by animateFloatAsState(
        targetValue = offsetX,
        label = "swipe_offset"
    )

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                showConfirmDialog = false
                offsetX = 0f
            },
            title = { Text("Hapus Langganan?") },
            text = {
                Text("Langganan \"${subscription.name}\" akan dihapus secara permanen dari lokal dan cloud.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rose500)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    offsetX = 0f
                }) {
                    Text("Batal")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
    ) {
        // Layer background merah dengan ikon trash (terlihat saat digeser kiri)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Rose500, shape = RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Hapus",
                tint = Color.White,
                modifier = Modifier.padding(end = 24.dp)
            )
        }

        // Layer konten item di atas background
        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        // Hanya izinkan geser ke kiri (nilai negatif), maksimum -320dp
                        val newOffset = (offsetX + delta).coerceIn(-320.dp.value, 0f)
                        offsetX = newOffset
                    },
                    onDragStopped = {
                        when {
                            // Swipe sangat jauh → langsung trigger konfirmasi hapus
                            offsetX < dismissThreshold -> {
                                showConfirmDialog = true
                            }
                            // Swipe setengah → pertahankan posisi reveal tombol delete
                            offsetX < revealThreshold -> {
                                offsetX = revealThreshold
                            }
                            // Swipe sedikit → snap kembali ke posisi awal
                            else -> {
                                offsetX = 0f
                            }
                        }
                    }
                )
        ) {
            SubscriptionItemRow(
                sub = subscription,
                onClick = onClick
            )
        }
    }
}

// =============================================================================
// SubscriptionItemRow — ditambahkan parameter onClick untuk navigasi ke detail
// =============================================================================
@Composable
fun SubscriptionItemRow(
    sub: Subscription,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Slate900, shape = RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(0.6f)) {
            Text(
                sub.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "${sub.category} • ${sub.paymentMethod}",
                fontSize = 12.sp,
                color = Slate400
            )
            Spacer(modifier = Modifier.height(8.dp))

            val today = LocalDate.now()
            val daysDiff = ChronoUnit.DAYS.between(today, sub.nextPaymentDate)

            when {
                sub.status == SubscriptionStatus.TRIAL ->
                    BadgeCard("Trial Berakhir", Amber500.copy(alpha = 0.15f), Amber500)
                daysDiff == 0L ->
                    BadgeCard("Hari Ini", Rose500.copy(alpha = 0.15f), Rose500)
                daysDiff in 1..3 ->
                    BadgeCard("$daysDiff Hari Lagi", Rose500.copy(alpha = 0.15f), Rose500)
                daysDiff in 4..7 ->
                    BadgeCard("Segera Jatuh Tempo", Amber500.copy(alpha = 0.15f), Amber500)
                else -> {
                    val formattedDate = sub.nextPaymentDate
                        .format(DateTimeFormatter.ofPattern("dd MMM"))
                    BadgeCard("Tempo $formattedDate", Emerald500.copy(alpha = 0.15f), Emerald500)
                }
            }
        }

        Column(modifier = Modifier.weight(0.4f), horizontalAlignment = Alignment.End) {
            val formattedPrice = NumberFormat
                .getCurrencyInstance(Locale.forLanguageTag("id-ID"))
                .format(sub.price)
            Text(
                formattedPrice,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "/${sub.billingCycle.name.lowercase()}",
                fontSize = 11.sp,
                color = Slate400
            )
        }
    }
}

@Composable
fun BadgeCard(
    text: String,
    containerColor: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .background(containerColor, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textColor)
    }
}

// =============================================================================
// PERBAIKAN GAP 3: Composable item untuk Card Riwayat (status ENDED)
// Tampilan berbeda dari item aktif — grayscale, badge "Berakhir"
// =============================================================================
@Composable
fun SubscriptionHistoryItem(subscription: Subscription) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Slate900.copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(0.6f)) {
            Text(
                subscription.name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Slate400 // Muted — menandakan sudah tidak aktif
            )
            Text(
                subscription.category,
                fontSize = 12.sp,
                color = Slate400.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .background(Slate800, shape = RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    "Berakhir",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate400
                )
            }
        }
        Column(modifier = Modifier.weight(0.4f), horizontalAlignment = Alignment.End) {
            val formattedPrice = NumberFormat
                .getCurrencyInstance(Locale.forLanguageTag("id-ID"))
                .format(subscription.price)
            Text(
                formattedPrice,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Slate400
            )
            Text(
                "/${subscription.billingCycle.name.lowercase()}",
                fontSize = 11.sp,
                color = Slate400.copy(alpha = 0.5f)
            )
        }
    }
}
