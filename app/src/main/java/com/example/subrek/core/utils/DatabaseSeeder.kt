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
        private const val KEY_SEED_DONE = "key_db_seed_done_v5"
    }

    fun seedIfNeeded() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val existingApps = subscriptionDao.getCustomApps().first()
                val alreadySeeded = sharedPreferences.getBoolean(KEY_SEED_DONE, false)
                
                if (existingApps.isEmpty() || !alreadySeeded) {
                    subscriptionDao.deleteAllCategories()
                    subscriptionDao.deleteAllApps()
                    subscriptionDao.deleteAllSubscriptions()

                    SeedData.defaultApps.forEach { app ->
                        subscriptionDao.insertCustomApp(app)
                    }

                    sharedPreferences.edit()
                        .putBoolean(KEY_SEED_DONE, true)
                        .apply()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
