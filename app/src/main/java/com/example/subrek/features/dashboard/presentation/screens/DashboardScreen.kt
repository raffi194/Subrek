package com.example.subrek.features.dashboard.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.subrek.core.utils.UiState
import com.example.subrek.features.dashboard.presentation.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val state by viewModel.dashboardState.collectAsState()

    Scaffold(
        topBar = {
            LargeTopAppBar(title = { Text("Dashboard") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (state) {
                is UiState.Loading -> CircularProgressIndicator()
                is UiState.Success -> {
                    val stats = (state as UiState.Success).data
                    Text(
                        text = "Total Pengeluaran: ${stats.currency} ${stats.totalMonthlySpend}",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                is UiState.Error -> Text("Error: ${(state as UiState.Error).message}")
                else -> {}
            }
        }
    }
}
