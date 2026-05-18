package com.example.subrek.features.subscription.presentation.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.subrek.core.theme.*
import com.example.subrek.features.subscription.domain.model.BillingCycle
import com.example.subrek.features.subscription.presentation.viewmodel.AddSubscriptionUiEvent
import com.example.subrek.features.subscription.presentation.viewmodel.AddSubscriptionViewModel
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubscriptionScreen(
    viewModel: AddSubscriptionViewModel,
    onNavigateBack: () -> Unit
) {
    val formState by viewModel.formState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val categories = listOf("Hiburan", "Productivity", "Utilitas", "Kesehatan", "Finansial")
    val currencies = listOf("IDR", "USD", "EUR", "SGD")
    val paymentMethods = listOf("Kartu Kredit", "E-Wallet", "Transfer Bank", "PayPal")

    var categoryExpanded by remember { mutableStateOf(false) }
    var currencyExpanded by remember { mutableStateOf(false) }
    var paymentExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is AddSubscriptionUiEvent.Success -> {
                    snackbarHostState.showSnackbar("Langganan berhasil disimpan!")
                    onNavigateBack()
                }
                is AddSubscriptionUiEvent.Error -> {
                    snackbarHostState.showSnackbar("Error: ${event.message}")
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Tambah Langganan", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Slate950)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Slate950) // Tailwind Slate950
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Input Nama Layanan
            OutlinedTextField(
                value = formState.name,
                onValueChange = { viewModel.onNameChange(it) },
                label = { Text("Nama Layanan (e.g. Netflix)") },
                isError = formState.nameError != null,
                supportingText = { formState.nameError?.let { Text(it, color = Rose500) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Blue500, errorBorderColor = Rose500)
            )

            // Input Mata Uang & Harga (Row)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(0.3f)) {
                    OutlinedTextField(
                        value = formState.currency,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Valuta") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, "Dropdown") },
                        modifier = Modifier.clickable { currencyExpanded = true }
                    )
                    DropdownMenu(expanded = currencyExpanded, onDismissRequest = { currencyExpanded = false }) {
                        currencies.forEach { curr ->
                            DropdownMenuItem(text = { Text(curr) }, onClick = {
                                viewModel.onCurrencyChange(curr)
                                currencyExpanded = false
                            })
                        }
                    }
                }

                OutlinedTextField(
                    value = formState.price,
                    onValueChange = { viewModel.onPriceChange(it) },
                    label = { Text("Biaya Berkala") },
                    isError = formState.priceError != null,
                    supportingText = { formState.priceError?.let { Text(it, color = Rose500) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(0.7f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Blue500, errorBorderColor = Rose500)
                )
            }

            // Dropdown Kategori
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = formState.category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kategori") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, "Dropdown") },
                    modifier = Modifier.fillMaxWidth().clickable { categoryExpanded = true }
                )
                DropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }, modifier = Modifier.fillMaxWidth(0.85f)) {
                    categories.forEach { cat ->
                        DropdownMenuItem(text = { Text(cat) }, onClick = {
                            viewModel.onCategoryChange(cat)
                            categoryExpanded = false
                        })
                    }
                }
            }

            // Dropdown Metode Pembayaran
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = formState.paymentMethod,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Metode Pembayaran") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, "Dropdown") },
                    modifier = Modifier.fillMaxWidth().clickable { paymentExpanded = true }
                )
                DropdownMenu(expanded = paymentExpanded, onDismissRequest = { paymentExpanded = false }, modifier = Modifier.fillMaxWidth(0.85f)) {
                    paymentMethods.forEach { method ->
                        DropdownMenuItem(text = { Text(method) }, onClick = {
                            viewModel.onPaymentMethodChange(method)
                            paymentExpanded = false
                        })
                    }
                }
            }

            // Segmented/Row Penentu Siklus Penagihan
            Column {
                Text("Siklus Penagihan", fontSize = 14.sp, color = Slate400, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BillingCycle.values().forEach { cycle ->
                        val isSelected = formState.billingCycle == cycle
                        Button(
                            onClick = { viewModel.onBillingCycleChange(cycle) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) Blue600 else Slate900,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else Slate400
                            )
                        ) {
                            Text(cycle.name, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Date Picker untuk Tanggal Pembayaran Pertama / Berikutnya
            val datePickerDialog = DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    viewModel.onFirstPaymentDateChange(LocalDate.of(year, month + 1, dayOfMonth))
                },
                formState.firstPaymentDate.year,
                formState.firstPaymentDate.monthValue - 1,
                formState.firstPaymentDate.dayOfMonth
            )

            OutlinedTextField(
                value = formState.firstPaymentDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                onValueChange = {},
                readOnly = true,
                label = { Text("Tanggal Mulai Penagihan") },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Pilih Tanggal",
                        modifier = Modifier.clickable { datePickerDialog.show() }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Toggle Free Trial
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900, shape = RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Layanan Free Trial?", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text("Tandai jika saat ini masih masa percobaan gratis", fontSize = 12.sp, color = Slate400)
                }
                Switch(
                    checked = formState.isTrial,
                    onCheckedChange = { viewModel.onTrialToggle(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = Blue500)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Button Simpan Utama
            Button(
                onClick = { viewModel.saveSubscription() },
                enabled = !formState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue600)
            ) {
                if (formState.isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Simpan Langganan", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
