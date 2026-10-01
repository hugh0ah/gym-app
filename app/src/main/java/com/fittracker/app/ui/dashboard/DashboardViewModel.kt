package com.fittracker.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fittracker.app.data.local.ApiKeyManager
import com.fittracker.app.data.local.dao.WorkoutLogWithExercise
import com.fittracker.app.data.local.entities.BodyCompositionLog
import com.fittracker.app.data.local.entities.DailyActivitySummary
import com.fittracker.app.data.local.entities.UserProfile
import com.fittracker.app.data.local.entities.UserTargets
import com.fittracker.app.data.local.entities.WeightLog
import com.fittracker.app.data.repository.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DayActivitySummary(
    val dayKey: String,       // "L", "M", "X", "J", "V", "S", "D"
    val dayName: String,      // "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"
    val dayNumber: Int,       // 1..7 (Mon=1, Sun=7)
    val date: String,         // "YYYY-MM-DD"
    val isToday: Boolean,
    val isSelected: Boolean,
    val isCompleted: Boolean,
    val workoutMinutes: Int,
    val caloriesBurned: Int
)

data class DashboardUiState(
    val date: String = LocalDate.now().toString(),
    val isViewingToday: Boolean = true,
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
    val waterGoalMl: Int = 2500,
    val weeklyActivity: List<DayActivitySummary> = emptyList()
) {
    val netCalories: Double
        get() = caloriesConsumed - activeCaloriesBurned

    val totalExerciseMinutes: Int
        get() = (workoutCount * 12) + cardioMinutes

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

    private val _selectedDate = MutableStateFlow(LocalDate.now().toString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private var lastKnownTodayDate: String = LocalDate.now().toString()

    private val _uiState = MutableStateFlow(
        DashboardUiState(
            date = _selectedDate.value,
            isViewingToday = true,
            isApiKeyConfigured = apiKeyManager.hasValidKey()
        )
    )
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    val currentApiKey: String
        get() = apiKeyManager.getApiKey()

    private var waterJob: Job? = null
    private var foodJob: Job? = null
    private var activityJob: Job? = null
    private var workoutJob: Job? = null

    // Cache de actividades y entrenamientos para cálculo de semana y racha
    private var cachedActivities: List<DailyActivitySummary> = emptyList()
    private var cachedWorkouts: List<WorkoutLogWithExercise> = emptyList()

    init {
        // Inicializar observación reactiva del día seleccionado
        observeDateData(_selectedDate.value)

        // Observar actividades y entrenamientos para la semana y racha
        observeWeeklyAndStreak()

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

        // Iniciar detector periódico de cambio de medianoche (00:00)
        startMidnightWatcher()
    }

    private fun observeDateData(date: String) {
        val today = LocalDate.now().toString()
        _uiState.update {
            it.copy(
                date = date,
                isViewingToday = (date == today)
            )
        }

        // Observar ingesta de agua para la fecha seleccionada
        waterJob?.cancel()
        waterJob = viewModelScope.launch {
            waterRepository.getWaterForDate(date).collect { water ->
                _uiState.update { it.copy(waterConsumedMl = water) }
            }
        }

        // Observar macros y objetivos de nutrición para la fecha seleccionada
        foodJob?.cancel()
        foodJob = viewModelScope.launch {
            combine(
                foodRepository.userTargets,
                foodRepository.getMacroTotalsForDate(date)
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

        // Observar actividad diaria para la fecha seleccionada (se reinicia automáticamente si no hay datos)
        activityJob?.cancel()
        activityJob = viewModelScope.launch {
            activityRepository.getActivityForDate(date).collect { activity ->
                _uiState.update {
                    it.copy(
                        activeCaloriesBurned = activity?.activeCalories ?: 0.0,
                        steps = activity?.steps ?: 0,
                        cardioMinutes = activity?.exerciseMinutes ?: 0,
                        cardioType = activity?.exerciseType ?: ""
                    )
                }
                // Refrescar semana para actualizar minutos/completado de hoy en tiempo real
                recalculateWeeklySummary()
            }
        }

        // Observar conteo de ejercicios de gimnasio para la fecha seleccionada
        workoutJob?.cancel()
        workoutJob = viewModelScope.launch {
            routineRepository.getWorkoutCountForDate(date).collect { count ->
                _uiState.update { it.copy(workoutCount = count) }
                // Refrescar semana para actualizar minutos/completado de hoy en tiempo real
                recalculateWeeklySummary()
            }
        }
    }

    private fun observeWeeklyAndStreak() {
        viewModelScope.launch {
            combine(
                activityRepository.getAllActivitiesFlow(),
                routineRepository.getAllWorkoutHistorySinceFlow(LocalDate.now().minusWeeks(26).toString())
            ) { activities, workouts ->
                Pair(activities, workouts)
            }.collect { (activities, workouts) ->
                cachedActivities = activities
                cachedWorkouts = workouts
                recalculateWeeklySummary()
                recalculateStreak()
            }
        }
    }

    private fun recalculateWeeklySummary() {
        val today = LocalDate.now()
        val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
        val days = listOf(
            Pair("L", "Lun"),
            Pair("M", "Mar"),
            Pair("X", "Mié"),
            Pair("J", "Jue"),
            Pair("V", "Vie"),
            Pair("S", "Sáb"),
            Pair("D", "Dom")
        )

        val activitiesMap = cachedActivities.associateBy { it.date }
        val workoutsMap = cachedWorkouts.groupBy { it.date }

        val weeklyList = days.mapIndexed { index, (key, name) ->
            val dayDate = monday.plusDays(index.toLong())
            val dayDateStr = dayDate.toString()
            val isDayToday = dayDate == today
            val isDaySelected = dayDateStr == _selectedDate.value

            val hasOccurred = !dayDate.isAfter(today)
            val activity = activitiesMap[dayDateStr]
            val workoutsForDay = if (hasOccurred) workoutsMap[dayDateStr].orEmpty() else emptyList()
            val workoutCount = workoutsForDay.size

            val minutes = if (hasOccurred) (activity?.exerciseMinutes ?: 0) + (workoutCount * 12) else 0
            val calories = if (hasOccurred) activity?.activeCalories?.toInt() ?: 0 else 0
            val isCompleted = hasOccurred && (workoutCount > 0 ||
                    (activity?.steps ?: 0) > 0 ||
                    (activity?.exerciseMinutes ?: 0) > 0 ||
                    (activity?.activeCalories ?: 0.0) > 0)

            DayActivitySummary(
                dayKey = key,
                dayName = name,
                dayNumber = index + 1,
                date = dayDateStr,
                isToday = isDayToday,
                isSelected = isDaySelected,
                isCompleted = isCompleted,
                workoutMinutes = minutes,
                caloriesBurned = calories
            )
        }

        _uiState.update { it.copy(weeklyActivity = weeklyList) }
    }

    private fun recalculateStreak() {
        val today = LocalDate.now()
        val activeDates = mutableSetOf<LocalDate>()

        for (act in cachedActivities) {
            if (act.steps > 0 || act.activeCalories > 0 || act.exerciseMinutes > 0) {
                try {
                    LocalDate.parse(act.date).takeIf { !it.isAfter(today) }?.let(activeDates::add)
                } catch (_: Exception) {}
            }
        }
        for (w in cachedWorkouts) {
            try {
                LocalDate.parse(w.date).takeIf { !it.isAfter(today) }?.let(activeDates::add)
            } catch (_: Exception) {}
        }

        if (activeDates.isEmpty()) {
            _uiState.update { it.copy(currentStreakDays = 0, bestStreakDays = 0) }
            return
        }

        // Racha actual: cuenta hacia atrás desde hoy o ayer
        var currentStreak = 0
        var checkDate = if (activeDates.contains(today)) today else today.minusDays(1)
        while (activeDates.contains(checkDate)) {
            currentStreak++
            checkDate = checkDate.minusDays(1)
        }

        // Mejor racha histórica
        var bestStreak = 0
        var currentRun = 0
        var prevDate: LocalDate? = null

        for (d in activeDates.sorted()) {
            if (prevDate == null || d == prevDate.plusDays(1)) {
                currentRun++
            } else if (d != prevDate) {
                currentRun = 1
            }
            if (currentRun > bestStreak) {
                bestStreak = currentRun
            }
            prevDate = d
        }
        if (currentStreak > bestStreak) {
            bestStreak = currentStreak
        }

        _uiState.update {
            it.copy(
                currentStreakDays = currentStreak,
                bestStreakDays = bestStreak
            )
        }
    }

    /**
     * Sincroniza la fecha al volver a la app o al pasar la medianoche.
     * Si el usuario estaba en "Hoy", avanza automáticamente al nuevo día.
     */
    fun checkAndSyncDate() {
        val today = LocalDate.now().toString()
        if (today != lastKnownTodayDate) {
            val wasViewingOldToday = (_selectedDate.value == lastKnownTodayDate)
            lastKnownTodayDate = today
            if (wasViewingOldToday) {
                selectDate(today)
            } else {
                _uiState.update { it.copy(isViewingToday = (_selectedDate.value == today)) }
                recalculateWeeklySummary()
                recalculateStreak()
            }
        }
    }

    private fun startMidnightWatcher() {
        viewModelScope.launch {
            while (isActive) {
                delay(30_000) // Comprobación cada 30 segundos
                checkAndSyncDate()
            }
        }
    }

    fun selectDate(date: String) {
        val selectedDate = try {
            LocalDate.parse(date)
        } catch (_: Exception) {
            LocalDate.now()
        }.coerceAtMost(LocalDate.now()).toString()

        if (_selectedDate.value == selectedDate) return
        _selectedDate.value = selectedDate
        observeDateData(selectedDate)
        recalculateWeeklySummary()
    }

    fun changeDate(offsetDays: Long) {
        val current = try {
            LocalDate.parse(_selectedDate.value)
        } catch (_: Exception) {
            LocalDate.now()
        }
        selectDate(current.plusDays(offsetDays).toString())
    }

    fun selectToday() {
        selectDate(LocalDate.now().toString())
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
            waterRepository.addWater(_selectedDate.value, amountMl)
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
                    bodyCompositionLogs = emptyList(),
                    weeklyActivity = emptyList()
                )
            }
        }
    }
}
