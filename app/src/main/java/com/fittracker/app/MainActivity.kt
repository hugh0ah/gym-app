package com.fittracker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fittracker.app.ui.assistant.AssistantViewModel
import com.fittracker.app.ui.dashboard.DashboardViewModel
import com.fittracker.app.ui.food.FoodViewModel
import com.fittracker.app.ui.navigation.AppNavigation
import com.fittracker.app.ui.routine.RoutineViewModel
import com.fittracker.app.ui.scale.SmartScaleViewModel
import com.fittracker.app.ui.symmetry.SymmetryViewModel
import com.fittracker.app.ui.theme.FitTrackerTheme
import com.fittracker.app.ui.wearable.WearableViewModel
import com.fittracker.app.ui.weight.WeightViewModel
import com.fittracker.app.ui.workout.LiveWorkoutViewModel

class MainActivity : ComponentActivity() {

    private val app by lazy { application as FitTrackerApp }

    private val dashboardViewModel: DashboardViewModel by viewModels {
        viewModelFactory {
            DashboardViewModel(
                foodRepository = app.foodRepository,
                activityRepository = app.activityRepository,
                routineRepository = app.routineRepository,
                weightRepository = app.weightRepository,
                bodyCompositionRepository = app.bodyCompositionRepository,
                userProfileRepository = app.userProfileRepository,
                waterRepository = app.waterRepository,
                apiKeyManager = app.apiKeyManager
            )
        }
    }

    private val routineViewModel: RoutineViewModel by viewModels {
        viewModelFactory {
            RoutineViewModel(
                routineRepository = app.routineRepository,
                exerciseRepository = app.exerciseRepository
            )
        }
    }

    private val liveWorkoutViewModel: LiveWorkoutViewModel by viewModels {
        viewModelFactory {
            LiveWorkoutViewModel(
                routineRepository = app.routineRepository,
                exerciseRepository = app.exerciseRepository
            )
        }
    }

    private val symmetryViewModel: SymmetryViewModel by viewModels {
        viewModelFactory {
            SymmetryViewModel(
                routineRepository = app.routineRepository
            )
        }
    }

    private val foodViewModel: FoodViewModel by viewModels {
        viewModelFactory {
            FoodViewModel(
                foodRepository = app.foodRepository,
                waterRepository = app.waterRepository,
                aiAssistantRepository = app.aiAssistantRepository
            )
        }
    }

    private val assistantViewModel: AssistantViewModel by viewModels {
        viewModelFactory {
            AssistantViewModel(
                aiAssistantRepository = app.aiAssistantRepository,
                routineRepository = app.routineRepository,
                apiKeyManager = app.apiKeyManager
            )
        }
    }

    private val wearableViewModel: WearableViewModel by viewModels {
        viewModelFactory {
            WearableViewModel(
                aiAssistantRepository = app.aiAssistantRepository,
                activityRepository = app.activityRepository
            )
        }
    }

    private val weightViewModel: WeightViewModel by viewModels {
        viewModelFactory {
            WeightViewModel(
                weightRepository = app.weightRepository
            )
        }
    }

    private val smartScaleViewModel: SmartScaleViewModel by viewModels {
        viewModelFactory {
            SmartScaleViewModel(
                aiAssistantRepository = app.aiAssistantRepository,
                bodyCompositionRepository = app.bodyCompositionRepository
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FitTrackerTheme {
                AppNavigation(
                    dashboardViewModel = dashboardViewModel,
                    routineViewModel = routineViewModel,
                    liveWorkoutViewModel = liveWorkoutViewModel,
                    symmetryViewModel = symmetryViewModel,
                    foodViewModel = foodViewModel,
                    assistantViewModel = assistantViewModel,
                    wearableViewModel = wearableViewModel,
                    weightViewModel = weightViewModel,
                    smartScaleViewModel = smartScaleViewModel
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        dashboardViewModel.checkAndSyncDate()
        foodViewModel.checkAndSyncDate()
        routineViewModel.checkAndSyncDate()
        symmetryViewModel.loadSymmetryData()
    }
}

@Suppress("UNCHECKED_CAST")
private inline fun <reified T : ViewModel> viewModelFactory(crossinline creator: () -> T): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        override fun <VM : ViewModel> create(modelClass: Class<VM>): VM {
            return creator() as VM
        }
    }
}
