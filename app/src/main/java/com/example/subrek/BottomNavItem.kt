package com.example.subrek

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : BottomNavItem("homepage", Icons.Default.Home, "Home")
    object Add : BottomNavItem("tambah_langganan", Icons.Default.AddCircle, "Tambah")
    object Profile : BottomNavItem("profile", Icons.Default.Person, "Profile")
}
