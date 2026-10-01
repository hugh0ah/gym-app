package com.fittracker.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Today
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Hoy", Icons.Default.Today)
    object Routine : Screen("routine", "Entreno", Icons.Default.FitnessCenter)
    object Symmetry : Screen("symmetry", "Simetría", Icons.Default.AccessibilityNew)
    object Food : Screen("food", "Nutrición", Icons.Default.Restaurant)
    object Assistant : Screen("assistant", "Coach IA", Icons.Default.AutoAwesome)
}
