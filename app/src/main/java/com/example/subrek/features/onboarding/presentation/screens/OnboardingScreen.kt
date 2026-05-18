package com.example.subrek.features.onboarding.presentation.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.subrek.core.theme.Blue500
import com.example.subrek.core.theme.Blue600
import com.example.subrek.core.theme.Slate400
import com.example.subrek.core.theme.Slate900
import com.example.subrek.core.theme.Slate950
import com.example.subrek.features.onboarding.presentation.viewmodel.OnboardingViewModel
import kotlinx.coroutines.launch

data class OnboardingPageData(
    val title: String,
    val description: String,
    val illustrationText: String
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onNavigateToDashboard: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    
    val pages = listOf(
        OnboardingPageData(
            title = "Pantau Semua Langganan",
            description = "Kumpulkan semua tagihan layanan digital Anda dalam satu dashboard terpusat tanpa ribet.",
            illustrationText = "📱"
        ),
        OnboardingPageData(
            title = "Deteksi Ghost Subscription",
            description = "Temukan pengeluaran tersembunyi dari layanan lama yang lupa Anda batalkan dengan cerdas.",
            illustrationText = "👻"
        ),
        OnboardingPageData(
            title = "Pengingat Tepat Waktu",
            description = "Dapatkan push notification lokal beberapa hari sebelum saldo Anda otomatis terpotong.",
            illustrationText = "🔔"
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })

    // Launcher untuk Request Izin Notifikasi (Android 13+)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Jalankan navigasi utama ke dashboard terlepas dari diizinkan atau tidak
        viewModel.completeOnboarding()
    }

    // Mengamati jika onboarding telah selesai, langsung arahkan ke Dashboard
    LaunchedEffect(uiState.isOnboardingCompleted) {
        if (uiState.isOnboardingCompleted) {
            onNavigateToDashboard()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950) // Menggunakan basis token Tailwind Slate950
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Tombol Lewati (Skip) di bagian atas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            if (pagerState.currentPage < pages.size - 1) {
                TextButton(onClick = { viewModel.completeOnboarding() }) {
                    Text(text = "Lewati", color = Slate400, fontSize = 14.sp)
                }
            }
        }

        // Konten Slider Onboarding
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Ilustrasi Karakter/Icon besar (Skala kelipatan 4dp)
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Slate900),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = pages[page].illustrationText, fontSize = 64.sp)
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Judul Text
                Text(
                    text = pages[page].title,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 32.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Deskripsi Text
                Text(
                    text = pages[page].description,
                    color = Slate400,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
            }
        }

        // Bagian Bawah: Indikator Pager & Tombol Aksi
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Pager Indicators Dot (Tailwind spacing)
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(pages.size) { it ->
                    val isSelected = pagerState.currentPage == it
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Blue500 else Slate900)
                            .size(if (isSelected) 24.dp else 8.dp, 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Tombol Lanjut / Mulai Sekarang
            Button(
                onClick = {
                    if (pagerState.currentPage < pages.size - 1) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        // Di langkah terakhir, jalankan alur runtime request izin notifikasi untuk Android 13+
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.completeOnboarding()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue600)
            ) {
                Text(
                    text = if (pagerState.currentPage == pages.size - 1) "Mulai Sekarang" else "Lanjutkan",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
