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
    val defaultCategories: List<String> = listOf("Popular", "Cineman", "Music", "Social Network"),
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

    // Data Katalog Statis Bawaan Aplikasi
    private val defaultCatalog = listOf(
        CatalogItem("1", "Netflix", "https://placeholder.co/100", "Cineman"),
        CatalogItem("2", "Spotify", "https://placeholder.co/100", "Music"),
        CatalogItem("3", "YouTube Premium", "https://placeholder.co/100", "Popular"),
        CatalogItem("4", "Twitter Blue", "https://placeholder.co/100", "Social Network")
    )

    init {
        observeCatalogAndCategories()
    }

    private fun observeCatalogAndCategories() {
        viewModelScope.launch {
            combine(
                repository.getCustomCategories(),
                repository.getCustomApps()
            ) { localCats, localApps ->
                val customCats = localCats.map { it.name }
                val customItems = localApps.map { 
                    CatalogItem(it.id, it.name, it.iconUrl, it.categoryName, isCustom = true) 
                }
                Pair(customCats, customItems)
            }.collect { (cats, items) ->
                _uiState.update { it.copy(
                    customCategories = cats,
                    catalogItems = defaultCatalog + items
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
    
    fun resetSaveSuccess() { _uiState.update { it.copy(isSaveSuccess = false) } }
}
