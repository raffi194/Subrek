package com.example.subrek

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SubrekApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Tempat inisialisasi library global nantinya jika diperlukan
    }
}