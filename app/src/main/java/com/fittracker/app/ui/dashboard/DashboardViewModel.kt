package com.fittracker.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fittracker.app.data.local.ApiKeyManager
import com.fittracker.app.data.local.entities.BodyCompositionLog
import com.fittracker.app.data.local.entities.UserProfile
import com.fittracker.app.data.local.entities.UserTargets
import com.fittracker.app.data.local.entities.WeightLog
import com.fittracker.app.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DayActivitySummary(
    val dayKey: String,       // "L", "M", "X", "J", "V", "S", "D"
    val dayName: String,      // "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"
    val dayNumber: Int,       // 1..7 (Mon=1, Sun=7)
    val isToday: Boolean,
    val isCompleted: Boolean,
    val workoutMinutes: Int,
    val caloriesBurned: Int
)

data class DashboardUiState(
    val date: String = LocalDate.now().toString(),
    val calorieTarget: Double = 2200.0,
    val proteinTarget: Double = 160.0,
    val carbTarget: Double = 240.0,
    val fatTarget: Double = 65.0,
    val caloriesConsumed: Double = 0.0,
    val proteinConsumed: Double = 0.0,
    val carbsConsumed: Double = 0.0,
    val fatConsumed: Double = 0.0,
    val activeCaloriesBurned: Double = 0.0,
    val steps: Int = 0,
    val cardioMinutes: Int = 0,
    val cardioType: String = "",
    val workoutCount: Int = 0,
    val currentStreakDays: Int = 0,
    val bestStreakDays: Int = 0,
    val weeklyTargetDays: Int = 5,
    val latestWeight: WeightLog? = null,
    val latestBodyComposition: BodyCompositionLog? = null,
    val bodyCompositionLogs: List<BodyCompositionLog> = emptyList(),
    val userProfile: UserProfile = UserProfile(),
    val isApiKeyConfigured: Boolean = false,
    val isWearableDialogVisible: Boolean = false,
    val isWeightDialogVisible: Boolean = false,
    val isApiKeyDialogVisible: Boolean = false,
    val isBodyCompositionDialogVisible: Boolean = false,
    val isSmartScaleDialogVisible: Boolean = false,
    val isUserProfileDialogVisible: Boolean = false,
    val waterConsumedMl: Int = 0,
    val waterGoalMl: Int = 2500
) {
    val netCalories: Double
        get() = caloriesConsumed - activeCaloriesBurned

    val totalExerciseMinutes: Int
        get() = (workoutCount * 12) + cardioMinutes

    val weeklyActivity: List<DayActivitySummary>
        get() {
            val dayOfWeek = try {
                LocalDate.now().dayOfWeek.value
            } catch (e: Exception) {
                1
            }
            val days = listOf(
                Pair("L", "Lun"),
                Pair("M", "Mar"),
                Pair("X", "Mié"),
                Pair("J", "Jue"),
                Pair("V", "Vie"),
                Pair("S", "Sáb"),
                Pair("D", "Dom")
            )

            val todayMinutes = totalExerciseMinutes
            val todayCalories = activeCaloriesBurned.toInt()
            val todayHasActivity = workoutCount > 0 || cardioMinutes > 0 || steps > 0 || activeCaloriesBurned > 0

            return days.mapIndexed { index, (key, name) ->
                val dayNum = index + 1
                val isToday = dayNum == dayOfWeek
                val isCompleted = if (isToday) todayHasActivity else false
                val minutes = if (isToday) todayMinutes else 0
                val calories = if (isToday) todayCalories else 0

                DayActivitySummary(
                    dayKey = key,
                    dayName = name,
                    dayNumber = dayNum,
                    isToday = isToday,
                    isCompleted = isCompleted,
                    workoutMinutes = minutes,
                    caloriesBurned = calories
                )
            }
        }

    val weeklyCompletedCount: Int
        get() = weeklyActivity.count { it.isCompleted }

    val weeklyTotalMinutes: Int
        get() = weeklyActivity.sumOf { it.workoutMinutes }
}

class DashboardViewModel(
    private val foodRepository: FoodRepository,
    private val activityRepository: ActivityRepository,
    private val routineRepository: RoutineRepository,
    private val weightRepository: WeightRepository,
    private val bodyCompositionRepository: BodyCompositionRepository,
    private val userProfileRepository: UserProfileRepository,
    private val waterRepository: WaterRepository,
    private val apiKeyManager: ApiKeyManager
) : ViewModel() {

    private val currentDate = LocalDate.now().toString()

    private val _uiState = MutableStateFlow(
        DashboardUiState(
            date = currentDate,
            isApiKeyConfigured = apiKeyManager.hasValidKey()
        )
    )
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    val currentApiKey: String
        get() = apiKeyManager.getApiKey()

    init {
        // Observar ingesta de agua
        viewModelScope.launch {
            waterRepository.getWaterForDate(currentDate).collect { water ->
                _uiState.update { it.copy(waterConsumedMl = water) }
            }
        }

        // Observar macros y objetivos de nutrición
        viewModelScope.launch {
            combine(
                foodRepository.userTargets,
                foodRepository.getMacroTotalsForDate(currentDate)
            ) { targets, macros ->
                Pair(targets, macros)
            }.collect { (targets, macros) ->
                val userTargets = targets ?: UserTargets()
                _uiState.update {
                    it.copy(
                        calorieTarget = userTargets.calorieTarget,
                        proteinTarget = userTargets.proteinTarget,
                        carbTarget = userTargets.carbTarget,
                        fatTarget = userTargets.fatTarget,
                        caloriesConsumed = macros.totalCalories,
                        proteinConsumed = macros.totalProtein,
                        carbsConsumed = macros.totalCarbs,
                        fatConsumed = macros.totalFat
                    )
                }
            }
        }

        // Observar actividad diaria (Huawei Health)
        viewModelScope.launch {
            activityRepository.getActivityForDate(currentDate).collect { activity ->
                _uiState.update {
                    it.copy(
                        activeCaloriesBurned = activity?.activeCalories ?: 0.0,
                        steps = activity?.steps ?: 0,
                        cardioMinutes = activity?.exerciseMinutes ?: 0,
                        cardioType = activity?.exerciseType ?: ""
                    )
                }
            }
        }

        // Observar conteo de ejercicios de gimnasio
        viewModelScope.launch {
            routineRepository.getWorkoutCountForDate(currentDate).collect { count ->
                _uiState.update { it.copy(workoutCount = count) }
            }
        }

        // Observar peso corporal más reciente
        viewModelScope.launch {
            weightRepository.latestLog.collect { weight ->
                _uiState.update { it.copy(latestWeight = weight) }
            }
        }

        // Observar registros de composición corporal (báscula inteligente)
        viewModelScope.launch {
            bodyCompositionRepository.allLogs.collect { logs ->
                _uiState.update {
                    it.copy(
                        bodyCompositionLogs = logs,
                        latestBodyComposition = logs.firstOrNull()
                    )
                }
            }
        }

        // Observar perfil de usuario (Mifflin-St Jeor)
        viewModelScope.launch {
            userProfileRepository.userProfile.collect { profile ->
                _uiState.update {
                    it.copy(
                        userProfile = profile ?: UserProfile(),
                        isApiKeyConfigured = apiKeyManager.hasValidKey()
                    )
                }
            }
        }
    }

    fun openWearableDialog() {
        _uiState.update { it.copy(isWearableDialogVisible = true) }
    }

    fun closeWearableDialog() {
        _uiState.update { it.copy(isWearableDialogVisible = false) }
    }

    fun openWeightDialog() {
        _uiState.update { it.copy(isWeightDialogVisible = true) }
    }

    fun closeWeightDialog() {
        _uiState.update { it.copy(isWeightDialogVisible = false) }
    }

    fun openApiKeyDialog() {
        _uiState.update { it.copy(isApiKeyDialogVisible = true) }
    }

    fun closeApiKeyDialog() {
        _uiState.update { it.copy(isApiKeyDialogVisible = false) }
    }

    fun openBodyCompositionDialog() {
        _uiState.update { it.copy(isBodyCompositionDialogVisible = true) }
    }

    fun closeBodyCompositionDialog() {
        _uiState.update { it.copy(isBodyCompositionDialogVisible = false) }
    }

    fun openSmartScaleDialog() {
        _uiState.update { it.copy(isSmartScaleDialogVisible = true) }
    }

    fun closeSmartScaleDialog() {
        _uiState.update { it.copy(isSmartScaleDialogVisible = false) }
    }

    fun openUserProfileDialog() {
        _uiState.update { it.copy(isUserProfileDialogVisible = true) }
    }

    fun closeUserProfileDialog() {
        _uiState.update { it.copy(isUserProfileDialogVisible = false) }
    }

    fun saveUserProfile(profile: UserProfile) {
        viewModelScope.launch {
            userProfileRepository.updateProfile(profile, applyRecommendedTargets = true)
        }
    }

    fun deleteBodyCompositionLog(id: Long) {
        viewModelScope.launch {
            bodyCompositionRepository.deleteLog(id)
        }
    }

    fun saveApiKey(key: String) {
        apiKeyManager.setApiKey(key)
        _uiState.update { it.copy(isApiKeyConfigured = apiKeyManager.hasValidKey()) }
        closeApiKeyDialog()
    }

    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            waterRepository.addWater(currentDate, amountMl)
        }
    }

    fun resetAllDataToZero() {
        viewModelScope.launch {
            weightRepository.clearAllWeightLogs()
            bodyCompositionRepository.clearAllBodyCompositionLogs()
            foodRepository.clearAllFoodLogs()
            routineRepository.clearAllRoutinesAndWorkouts()
            activityRepository.clearAllActivities()
            waterRepository.clearAll()
            _uiState.update {
                it.copy(
                    caloriesConsumed = 0.0,
                    proteinConsumed = 0.0,
                    carbsConsumed = 0.0,
                    fatConsumed = 0.0,
                    waterConsumedMl = 0,
                    activeCaloriesBurned = 0.0,
                    steps = 0,
                    cardioMinutes = 0,
                    workoutCount = 0,
                    currentStreakDays = 0,
                    bestStreakDays = 0,
                    latestWeight = null,
                    latestBodyComposition = null,
                    bodyCompositionLogs = emptyList()
                )
            }
        }
    }
}
