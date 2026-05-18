package com.example.subrek

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.example.subrek.core.background.NotificationWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class SubrekApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        setupDailyNotificationWorker()
    }

    private fun setupDailyNotificationWorker() {
        // KONFIGURASI BATASAN: Berjalan hanya saat perangkat charging / idle (Efisien Baterai)
        val constraints = Constraints.Builder()
            .setRequiresCharging(true)
            .setRequiresDeviceIdle(true)
            .build()

        // PENJADWALAN PERIODIK: Berulang setiap 24 Jam sekali secara teratur
        val dailyWorkRequest = PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        // DEDUPLIKASI UNIK: Menggunakan kebijakan KEEP agar tidak menumpuk task ganda dalam sehari
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "SubrekDailyBillingJob",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyWorkRequest
        )
    }
}
