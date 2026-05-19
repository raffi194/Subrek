package com.example.subrek.features.subscription

import com.example.subrek.features.subscription.domain.model.CatalogItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogFilterTest {

    @Test
    fun `filterCatalogItems_berdasarkanSearchQuery_harusSesuai`() {
        val mockCatalog = listOf(
            CatalogItem(id = "1", name = "Netflix", iconUrl = null),
            CatalogItem(id = "2", name = "Spotify", iconUrl = null),
            CatalogItem(id = "3", name = "YouTube", iconUrl = null),
            CatalogItem(id = "4", name = "Notion Premium", iconUrl = null, isCustom = true)
        )

        // Skenario: Filter Pencarian Kata "Notion"
        val searchQuery = "Notion"
        val filterSearch = mockCatalog.filter { item ->
            item.name.contains(searchQuery, ignoreCase = true)
        }
        assertEquals(1, filterSearch.size)
        assertTrue(filterSearch.first().isCustom)
        assertEquals("Notion Premium", filterSearch.first().name)
    }
}
