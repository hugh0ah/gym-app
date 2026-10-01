package com.fittracker.app.ui.navigation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.fittracker.app.ui.symmetry.SymmetryScreen
import com.fittracker.app.ui.symmetry.SymmetryViewModel
import com.fittracker.app.ui.theme.*
import com.fittracker.app.ui.scale.SmartScaleViewModel
import com.fittracker.app.ui.wearable.WearableViewModel
import com.fittracker.app.ui.weight.WeightViewModel
import com.fittracker.app.ui.workout.LiveWorkoutSheet
import com.fittracker.app.ui.workout.LiveWorkoutStickyBar
import com.fittracker.app.ui.workout.LiveWorkoutViewModel
import com.fittracker.app.ui.workout.WorkoutFinishedDialog

@Composable
fun AppNavigation(
    dashboardViewModel: DashboardViewModel,
    routineViewModel: RoutineViewModel,
    liveWorkoutViewModel: LiveWorkoutViewModel,
    symmetryViewModel: SymmetryViewModel,
    foodViewModel: FoodViewModel,
    assistantViewModel: AssistantViewModel,
    wearableViewModel: WearableViewModel,
    weightViewModel: WeightViewModel,
    smartScaleViewModel: SmartScaleViewModel
) {
    val navController = rememberNavController()
    val liveState by liveWorkoutViewModel.uiState.collectAsState()

    val items = listOf(
        Screen.Dashboard,
        Screen.Routine,
        Screen.Symmetry,
        Screen.Food,
        Screen.Assistant
    )

    Scaffold(
        containerColor = DarkBg,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                // Barra pegajosa de sesión en vivo activa (flotante sobre la barra de navegación)
                LiveWorkoutStickyBar(
                    session = liveState.activeSession,
                    onClick = { liveWorkoutViewModel.expandSheet() }
                )

                NavigationBar(
                    containerColor = DarkSurface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.border(
                        width = 1.dp,
                        color = DarkCardBorder,
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
                                    fontSize = 10.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            selected = selected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = NeonMint,
                                indicatorColor = NeonMint,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
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
                    },
                    onNavigateToSymmetry = {
                        navController.navigate(Screen.Symmetry.route)
                    }
                )
            }

            composable(Screen.Routine.route) {
                RoutineScreen(
                    viewModel = routineViewModel,
                    liveWorkoutViewModel = liveWorkoutViewModel
                )
            }

            composable(Screen.Symmetry.route) {
                SymmetryScreen(
                    viewModel = symmetryViewModel
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

        // Hoja completa de Registro de Entrenamiento en Vivo
        if (liveState.isSheetExpanded) {
            LiveWorkoutSheet(
                viewModel = liveWorkoutViewModel,
                onDismiss = { liveWorkoutViewModel.collapseSheet() }
            )
        }

        // Diálogo de felicitaciones tras terminar sesión
        if (liveState.isWorkoutFinishedDialogVisible && liveState.finishedWorkoutSummary != null) {
            WorkoutFinishedDialog(
                summary = liveState.finishedWorkoutSummary!!,
                onDismiss = { liveWorkoutViewModel.dismissFinishedDialog() }
            )
        }
    }
}
