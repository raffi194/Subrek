package com.example.subrek.core.di

import android.content.Context
import androidx.room.Room
import com.example.subrek.features.dashboard.data.DashboardRepositoryImpl
import com.example.subrek.features.dashboard.domain.repository.DashboardRepository
import com.example.subrek.features.report.data.ReportRepositoryImpl
import com.example.subrek.features.report.domain.repository.ReportRepository
import com.example.subrek.features.subscription.data.local.AppDatabase
import com.example.subrek.features.subscription.data.local.SubscriptionDao
import com.example.subrek.features.subscription.data.repository.SubscriptionRepositoryImpl
import com.example.subrek.features.subscription.domain.repository.SubscriptionRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindSubscriptionRepository(
        subscriptionRepositoryImpl: SubscriptionRepositoryImpl
    ): SubscriptionRepository

    @Binds
    @Singleton
    abstract fun bindDashboardRepository(
        dashboardRepositoryImpl: DashboardRepositoryImpl
    ): DashboardRepository

    @Binds
    @Singleton
    abstract fun bindReportRepository(
        reportRepositoryImpl: ReportRepositoryImpl
    ): ReportRepository

    companion object {
        @Provides
        @Singleton
        fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "subrek_db"
            ).build()
        }

        @Provides
        @Singleton
        fun provideSubscriptionDao(database: AppDatabase): SubscriptionDao {
            return database.subscriptionDao()
        }
    }
}
