package com.example.subrek.core.di

import android.content.Context
import androidx.room.Room
import com.example.subrek.features.subscription.data.local.AppDatabase
import com.example.subrek.features.subscription.data.local.SubscriptionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan_tennert.supabase.SupabaseClient
import io.github.jan_tennert.supabase.createSupabaseClient
import io.github.jan_tennert.supabase.gotrue.Auth
import io.github.jan_tennert.supabase.postgrest.Postgrest
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
@Suppress("unused")
object AppModule {

    private const val SUPABASE_URL = "https://gjnbqivjikulpjoovcme.supabase.co"
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdqbmJxaXZqaWt1bHBqb292Y21lIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzkwMjY1NTksImV4cCI6MjA5NDYwMjU1OX0.J7s9iydrufRmxd6eKioEiNajBp5piaPPx4NE9yAT8lI"

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(SUPABASE_URL, SUPABASE_ANON_KEY) {
            install(Postgrest)
            install(Auth)
        }
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "subrek_database"
        ).fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    @Singleton
    fun provideSubscriptionDao(database: AppDatabase): SubscriptionDao {
        return database.subscriptionDao()
    }
}