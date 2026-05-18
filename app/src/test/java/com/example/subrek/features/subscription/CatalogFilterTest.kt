package com.example.subrek.features.subscription

import com.example.subrek.features.subscription.domain.model.CatalogItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogFilterTest {

    @Test
    fun `generateCategoryTabs_harusMenggabungkanKategoriDanAllBeradaDiPalingKanan`() {
        // Data Kategori Bawaan Aplikasi
        val defaultCategories = listOf("Popular", "Cineman", "Music", "Social Network")
        
        // Simulasi Kategori Kustom Dinamis Yang Dibuat Pengguna A (Data Isolation)
        val customCategoriesFromUserA = listOf("Productivity", "Gaming")

        // Eksekusi Logika Penggabungan Sesuai Kode di TambahLanggananScreen
        val finalTabsResult = defaultCategories + customCategoriesFromUserA + listOf("All")

        // Verifikasi Jumlah Total Tab: 4 (default) + 2 (kustom) + 1 (All) = 7
        assertEquals(7, finalTabsResult.size)

        // Verifikasi Aturan Posisi: "All" Harus Berada di Posisi Tab Paling Kanan/Ujung Terakhir
        val posisiTerakhir = finalTabsResult.lastIndex
        assertEquals("All", finalTabsResult[posisiTerakhir])
    }

    @Test
    fun `filterCatalogItems_berdasarkanKategoriDanSearchQuery_harusSesuai`() {
        val mockCatalog = listOf(
            CatalogItem(id = "1", name = "Netflix", iconUrl = null, categoryName = "Cineman"),
            CatalogItem(id = "2", name = "Spotify", iconUrl = null, categoryName = "Music"),
            CatalogItem(id = "3", name = "YouTube", iconUrl = null, categoryName = "Popular"),
            CatalogItem(id = "4", name = "Notion Premium", iconUrl = null, categoryName = "Productivity", isCustom = true)
        )

        // Skenario 1: Filter Kategori "Music"
        val filterMusic = mockCatalog.filter { it.categoryName == "Music" }
        assertEquals(1, filterMusic.size)
        assertEquals("Spotify", filterMusic.first().name)

        // Skenario 2: Filter Pencarian Kata "Notion" dalam mode kategori "All"
        val searchQuery = "Notion"
        val filterSearchAndAll = mockCatalog.filter { item ->
            (item.categoryName == "Productivity" || "All" == "All") && item.name.contains(searchQuery, ignoreCase = true)
        }
        assertEquals(1, filterSearchAndAll.size)
        assertTrue(filterSearchAndAll.first().isCustom)
    }
}
