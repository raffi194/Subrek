package com.example.subrek.core.di

import com.example.subrek.features.dashboard.data.DashboardRepositoryImpl
import com.example.subrek.features.dashboard.domain.repository.DashboardRepository
import com.example.subrek.features.report.data.ReportRepositoryImpl
import com.example.subrek.features.report.domain.repository.ReportRepository
import com.example.subrek.features.subscription.data.repository.SubscriptionRepositoryImpl
import com.example.subrek.features.subscription.domain.repository.SubscriptionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
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
}
