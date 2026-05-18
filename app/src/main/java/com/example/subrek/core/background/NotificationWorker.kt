package com.example.subrek.core.background

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker
import com.example.subrek.MainActivity
import com.example.subrek.features.subscription.domain.repository.SubscriptionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: SubscriptionRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val CHANNEL_ID = "subrek_billing_alerts"
        private const val CHANNEL_NAME = "Pengingat Tagihan Subrek"
    }

    override suspend fun doWork(): ListenableWorker.Result {
        return try {
            // 1. DETEKSI JATUH TEMPO: Ambil layanan yang kritis (Hari-H, 3 Hari, dan 7 Hari lagi)
            val expiringToday = repository.getSubscriptionsExpiringInDays(0)
            val expiringIn3Days = repository.getSubscriptionsExpiringInDays(3)
            val expiringIn7Days = repository.getSubscriptionsExpiringInDays(7)

            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            createNotificationChannel(notificationManager)

            var notificationId = 100

            // 2. TRIGGER PUSH NOTIFICATION DENGAN PESAN RELEVAN
            expiringToday.forEach { sub ->
                showNotification(
                    notificationManager,
                    notificationId++,
                    "Tagihan Hari Ini! 🚨",
                    "Langganan ${sub.name} jatuh tempo hari ini. Siapkan saldo sebesar ${sub.currency} ${sub.price}."
                )
            }

            expiringIn3Days.forEach { sub ->
                showNotification(
                    notificationManager,
                    notificationId++,
                    "3 Hari Lagi Jatuh Tempo ⏳",
                    "Jangan lupa, 3 hari lagi tagihan ${sub.name} akan diperpanjang secara otomatis."
                )
            }

            expiringIn7Days.forEach { sub ->
                showNotification(
                    notificationManager,
                    notificationId++,
                    "Pengingat Tagihan (7 Hari) 🔔",
                    "Layanan ${sub.name} memasuki rentang seminggu sebelum jatuh tempo siklus berikutnya."
                )
            }

            ListenableWorker.Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            ListenableWorker.Result.retry() // Coba kembali nanti jika terjadi interupsi internal
        }
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Saluran utama notifikasi pengingat pembayaran berkala layanan digital"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(
        notificationManager: NotificationManager,
        id: Int,
        title: String,
        message: String
    ) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 
            id, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Menggunakan sistem default icon alarm sementara
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(id, notification)
    }
}
