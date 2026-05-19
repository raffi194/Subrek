package com.example.subrek.features.subscription.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.subrek.features.subscription.data.local.LocalAppEntity
import com.example.subrek.features.subscription.data.local.LocalCategoryEntity
import com.example.subrek.features.subscription.domain.model.CatalogItem
import com.example.subrek.features.subscription.domain.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class CatalogUiState(
    val customCategories: List<String> = emptyList(),
    val catalogItems: List<CatalogItem> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val isSaveSuccess: Boolean = false
)

@HiltViewModel
class TambahLanggananViewModel @Inject constructor(
    private val repository: SubscriptionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    init {
        observeCatalogAndCategories()
    }

    private fun observeCatalogAndCategories() {
        viewModelScope.launch {
            combine(
                repository.getCustomCategories(),
                repository.getCustomApps()
            ) { localCats, localApps ->
                // Memasukkan "All" sebagai elemen pertama list kategori secara konstan dan stabil
                val allCategories = listOf("All") + localCats.map { it.name }.distinct().filter { it != "All" }
                val allItems = localApps.map { app ->
                    // App dengan id prefix "app_" adalah bawaan sistem, bukan custom user
                    val isUserCustom = !app.id.startsWith("app_")
                    CatalogItem(app.id, app.name, app.iconUrl, app.categoryName, isCustom = isUserCustom)
                }
                Pair(allCategories, allItems)
            }
            .catch { e -> e.printStackTrace() }
            .collect { (cats, items) ->
                _uiState.update { it.copy(
                    customCategories = cats,
                    catalogItems = items
                )}
            }
        }
    }

    fun updateSearchQuery(query: String) { _uiState.update { it.copy(searchQuery = query) } }
    fun selectCategory(category: String) { _uiState.update { it.copy(selectedCategory = category) } }

    fun addCustomCategory(name: String) {
        viewModelScope.launch {
            repository.insertCategory(LocalCategoryEntity(UUID.randomUUID().toString(), name))
        }
    }

    fun addCustomApp(name: String, iconUrl: String?, category: String) {
        viewModelScope.launch {
            repository.insertCustomApp(LocalAppEntity(UUID.randomUUID().toString(), name, iconUrl, category))
        }
    }

    fun saveNewSubscription(name: String, iconUrl: String?, price: Double, cycle: String, date: String, isTrial: Boolean) {
        viewModelScope.launch {
            repository.saveSubscription(name, iconUrl, price, cycle, date, isTrial)
            _uiState.update { it.copy(isSaveSuccess = true) }
        }
    }

    fun addCustomAppWithDetails(
        name: String,
        category: String,
        price: Double,
        currency: String,
        billingCycle: String,
        paymentMethod: String,
        nextPaymentDate: String
    ) {
        addCustomAppWithImage(name, category, price, currency, billingCycle, paymentMethod, nextPaymentDate, null)
    }

    fun addCustomAppWithImage(
        name: String,
        category: String,
        price: Double,
        currency: String,
        billingCycle: String,
        paymentMethod: String,
        nextPaymentDate: String,
        imageUri: android.net.Uri?
    ) {
        viewModelScope.launch {
            var remoteIconUrl: String? = null

            // 1. Eksekusi Upload File Gambar ke Supabase Storage jika file ada
            if (imageUri != null) {
                try {
                    // repository mengunggah file biner dan mereturn string public URL dari Supabase
                    remoteIconUrl = repository.uploadAppIconStorage(imageUri)
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Fallback jika upload bermasalah agar database tidak crash null constraint
                }
            }

            val generatedId = UUID.randomUUID().toString()

            // 2. Masukkan ke Katalog App Lokal
            repository.insertCustomApp(
                LocalAppEntity(
                    id = generatedId,
                    name = name,
                    iconUrl = remoteIconUrl, // Menyimpan String URL hemat storage
                    categoryName = category
                )
            )

            // 3. Simpan ke database terpusat (Supabase subscriptions table) dengan tambahan kolom iconUrl
            repository.saveSubscriptionExtended(
                id = generatedId,
                name = name,
                price = price,
                currency = currency,
                billingCycle = billingCycle,
                category = category,
                paymentMethod = paymentMethod,
                nextPaymentDate = nextPaymentDate,
                status = "ACTIVE",
                iconUrl = remoteIconUrl // Masuk sebagai string path/varchar di PostgreSQL
            )

            _uiState.update { it.copy(isSaveSuccess = true) }
        }
    }
    
    fun resetSaveSuccess() { _uiState.update { it.copy(isSaveSuccess = false) } }
}
