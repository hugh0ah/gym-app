package com.fittracker.app.ui.navigation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fittracker.app.ui.assistant.AssistantScreen
import com.fittracker.app.ui.assistant.AssistantViewModel
import com.fittracker.app.ui.dashboard.DashboardScreen
import com.fittracker.app.ui.dashboard.DashboardViewModel
import com.fittracker.app.ui.food.FoodScreen
import com.fittracker.app.ui.food.FoodViewModel
import com.fittracker.app.ui.routine.RoutineScreen
import com.fittracker.app.ui.routine.RoutineViewModel
import com.fittracker.app.ui.theme.AccentCoral
import com.fittracker.app.ui.theme.AccentCoralDark
import com.fittracker.app.ui.theme.CreamBg
import com.fittracker.app.ui.theme.CreamBorder
import com.fittracker.app.ui.theme.CreamSurface
import com.fittracker.app.ui.theme.TextDarkMuted
import com.fittracker.app.ui.scale.SmartScaleViewModel
import com.fittracker.app.ui.wearable.WearableViewModel
import com.fittracker.app.ui.weight.WeightViewModel

@Composable
fun AppNavigation(
    dashboardViewModel: DashboardViewModel,
    routineViewModel: RoutineViewModel,
    foodViewModel: FoodViewModel,
    assistantViewModel: AssistantViewModel,
    wearableViewModel: WearableViewModel,
    weightViewModel: WeightViewModel,
    smartScaleViewModel: SmartScaleViewModel
) {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Dashboard,
        Screen.Routine,
        Screen.Food,
        Screen.Assistant
    )

    Scaffold(
        containerColor = CreamBg,
        bottomBar = {
            NavigationBar(
                containerColor = CreamSurface,
                tonalElevation = 6.dp,
                modifier = Modifier.border(
                    width = 1.dp,
                    color = CreamBorder,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                )
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { screen ->
                    val selected = currentRoute == screen.route
                    NavigationBarItem(
                        icon = {
                            Icon(
                                screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                screen.title,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        selected = selected,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = AccentCoralDark,
                            indicatorColor = AccentCoral,
                            unselectedIconColor = TextDarkMuted,
                            unselectedTextColor = TextDarkMuted
                        ),
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    wearableViewModel = wearableViewModel,
                    weightViewModel = weightViewModel,
                    smartScaleViewModel = smartScaleViewModel,
                    onNavigateToFood = {
                        navController.navigate(Screen.Food.route)
                    },
                    onNavigateToRoutine = {
                        navController.navigate(Screen.Routine.route)
                    }
                )
            }

            composable(Screen.Routine.route) {
                RoutineScreen(
                    viewModel = routineViewModel
                )
            }

            composable(Screen.Food.route) {
                FoodScreen(
                    viewModel = foodViewModel
                )
            }

            composable(Screen.Assistant.route) {
                AssistantScreen(
                    viewModel = assistantViewModel
                )
            }
        }
    }
}
