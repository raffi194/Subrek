package com.example.subrek.core.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.example.subrek.features.subscription.data.local.AppDatabase
import com.example.subrek.features.subscription.data.local.SubscriptionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
// --- PERBAIKAN IMPORT SUPABASE ---
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = "https://your-project-url.supabase.co",
            supabaseKey = "your-anon-key"
        ) {
            install(Postgrest)
            install(Auth)
        }
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences {
        return context.getSharedPreferences("subrek_prefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "subrek_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideSubscriptionDao(database: AppDatabase): SubscriptionDao {
        return database.subscriptionDao()
    }
}