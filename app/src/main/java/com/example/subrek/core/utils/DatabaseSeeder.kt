package com.example.subrek.core.utils

import android.content.SharedPreferences
import com.example.subrek.features.subscription.data.local.SubscriptionDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeeder @Inject constructor(
    private val subscriptionDao: SubscriptionDao,
    private val sharedPreferences: SharedPreferences
) {
    companion object {
        private const val KEY_SEED_DONE = "key_db_seed_done_v1"
    }

    fun seedIfNeeded() {
        val alreadySeeded = sharedPreferences.getBoolean(KEY_SEED_DONE, false)
        if (alreadySeeded) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Seed kategori default
                SeedData.defaultCategories.forEach { category ->
                    subscriptionDao.insertCategory(category)
                }

                // Seed katalog app default
                SeedData.defaultApps.forEach { app ->
                    subscriptionDao.insertCustomApp(app)
                }

                // Seed demo subscriptions HANYA jika tabel kosong
                val existing = subscriptionDao.getAllSubscriptions().first()
                if (existing.isEmpty()) {
                    subscriptionDao.insertSubscriptions(SeedData.demoSubscriptions)
                }

                sharedPreferences.edit()
                    .putBoolean(KEY_SEED_DONE, true)
                    .apply()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
