package com.fittracker.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Today
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Hoy", Icons.Default.Today)
    object Routine : Screen("routine", "Rutina", Icons.Default.FitnessCenter)
    object Food : Screen("food", "Comidas", Icons.Default.Restaurant)
    object Assistant : Screen("assistant", "Asistente IA", Icons.Default.AutoAwesome)
}
