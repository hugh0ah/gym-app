package com.fittracker.app.ui.routine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fittracker.app.data.local.dao.RoutineExerciseWithDetails
import com.fittracker.app.data.local.entities.Exercise
import com.fittracker.app.data.local.entities.Routine
import com.fittracker.app.data.repository.ExerciseRepository
import com.fittracker.app.data.repository.RoutineRepository
import com.fittracker.app.ui.components.PresetRoutineTemplate
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class RoutineUiState(
    val routines: List<Routine> = emptyList(),
    val selectedRoutineId: Long? = null,
    val selectedDayOfWeek: Int = 1, // 1 = Lunes .. 7 = Domingo
    val exercisesForDay: List<RoutineExerciseWithDetails> = emptyList(),
    val allExercises: List<Exercise> = emptyList(),
    val isAddExerciseDialogOpen: Boolean = false,
    val isLogWorkoutDialogOpen: Boolean = false,
    val isCreateRoutineDialogOpen: Boolean = false,
    val isOneRepMaxDialogOpen: Boolean = false,
    val isPlateCalculatorDialogOpen: Boolean = false,
    val isExerciseExplorerDialogOpen: Boolean = false,
    val isPresetRoutinesDialogOpen: Boolean = false,
    val isProgressionDialogOpen: Boolean = false,
    val isMuscleVolumeDialogOpen: Boolean = false,
    val isWorkoutHeatmapDialogOpen: Boolean = false,
    val workoutDates: Set<String> = emptySet(),
    val selectedExerciseForLog: RoutineExerciseWithDetails? = null,
    val selectedExerciseFor1RM: Exercise? = null,
    val exerciseSearchQuery: String = "",
    val selectedMuscleFilter: String? = null
)

class RoutineViewModel(
    private val routineRepository: RoutineRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoutineUiState())
    val uiState: StateFlow<RoutineUiState> = _uiState.asStateFlow()

    private var exercisesJob: Job? = null

    init {
        // Observar lista de rutinas
        viewModelScope.launch {
            routineRepository.allRoutines.collect { routines ->
                _uiState.update { state ->
                    val selectedId = state.selectedRoutineId ?: routines.firstOrNull()?.id
                    state.copy(
                        routines = routines,
                        selectedRoutineId = selectedId
                    )
                }
                val currentSelectedId = _uiState.value.selectedRoutineId
                if (currentSelectedId != null) {
                    observeExercises(currentSelectedId, _uiState.value.selectedDayOfWeek)
                }
            }
        }

        // Observar catálogo de ejercicios de la base de datos (1.352 ejercicios de OpenGym)
        viewModelScope.launch {
            exerciseRepository.allExercises.collect { allEx ->
                _uiState.update { it.copy(allExercises = allEx) }
            }
        }

        // Cargar historial de entrenamientos para el Mapa de Calor OpenGym
        loadWorkoutDates()
    }

    private fun loadWorkoutDates() {
        viewModelScope.launch {
            val since = LocalDate.now().minusMonths(6).toString()
            val logs = routineRepository.getAllWorkoutHistorySince(since)
            _uiState.update { it.copy(workoutDates = logs.map { log -> log.date }.toSet()) }
        }
    }

    private fun observeExercises(routineId: Long, dayOfWeek: Int) {
        exercisesJob?.cancel()
        exercisesJob = viewModelScope.launch {
            routineRepository.getExercisesForDay(routineId, dayOfWeek).collect { exercises ->
                _uiState.update { it.copy(exercisesForDay = exercises) }
            }
        }
    }

    fun selectDay(day: Int) {
        _uiState.update { it.copy(selectedDayOfWeek = day) }
        val routineId = _uiState.value.selectedRoutineId
        if (routineId != null) {
            observeExercises(routineId, day)
        }
    }

    fun selectRoutine(id: Long) {
        _uiState.update { it.copy(selectedRoutineId = id) }
        observeExercises(id, _uiState.value.selectedDayOfWeek)
    }

    fun openCreateRoutineDialog() {
        _uiState.update { it.copy(isCreateRoutineDialogOpen = true) }
    }

    fun closeCreateRoutineDialog() {
        _uiState.update { it.copy(isCreateRoutineDialogOpen = false) }
    }

    fun createRoutine(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val newId = routineRepository.createRoutine(name)
            selectRoutine(newId)
            _uiState.update { it.copy(isCreateRoutineDialogOpen = false) }
        }
    }

    fun openAddExerciseDialog() {
        _uiState.update {
            it.copy(
                isAddExerciseDialogOpen = true,
                exerciseSearchQuery = "",
                selectedMuscleFilter = null
            )
        }
    }

    fun closeAddExerciseDialog() {
        _uiState.update { it.copy(isAddExerciseDialogOpen = false) }
    }

    fun openExerciseExplorer() {
        _uiState.update { it.copy(isExerciseExplorerDialogOpen = true) }
    }

    fun closeExerciseExplorer() {
        _uiState.update { it.copy(isExerciseExplorerDialogOpen = false) }
    }

    fun openOneRepMaxDialog(exercise: Exercise? = null) {
        _uiState.update {
            it.copy(
                isOneRepMaxDialogOpen = true,
                selectedExerciseFor1RM = exercise
            )
        }
    }

    fun closeOneRepMaxDialog() {
        _uiState.update {
            it.copy(
                isOneRepMaxDialogOpen = false,
                selectedExerciseFor1RM = null
            )
        }
    }

    fun openPlateCalculator() {
        _uiState.update { it.copy(isPlateCalculatorDialogOpen = true) }
    }

    fun closePlateCalculator() {
        _uiState.update { it.copy(isPlateCalculatorDialogOpen = false) }
    }

    fun openPresetRoutines() {
        _uiState.update { it.copy(isPresetRoutinesDialogOpen = true) }
    }

    fun closePresetRoutines() {
        _uiState.update { it.copy(isPresetRoutinesDialogOpen = false) }
    }

    fun updateExerciseSearch(query: String) {
        _uiState.update { it.copy(exerciseSearchQuery = query) }
    }

    fun selectMuscleFilter(muscle: String?) {
        _uiState.update { it.copy(selectedMuscleFilter = muscle) }
    }

    fun addExerciseToCurrentDay(exerciseId: String, sets: Int, reps: Int, weight: Double) {
        val routineId = _uiState.value.selectedRoutineId ?: return
        val currentDay = _uiState.value.selectedDayOfWeek
        val nextOrder = _uiState.value.exercisesForDay.size + 1

        viewModelScope.launch {
            routineRepository.addExerciseToRoutine(
                routineId = routineId,
                exerciseId = exerciseId,
                sets = sets,
                reps = reps,
                weight = weight,
                order = nextOrder,
                dayOfWeek = currentDay
            )
            _uiState.update {
                it.copy(
                    isAddExerciseDialogOpen = false,
                    isExerciseExplorerDialogOpen = false
                )
            }
        }
    }

    fun importPresetRoutine(template: PresetRoutineTemplate) {
        viewModelScope.launch {
            val newRoutineId = routineRepository.createRoutine(template.name)
            template.exercises.forEachIndexed { index, item ->
                routineRepository.addExerciseToRoutine(
                    routineId = newRoutineId,
                    exerciseId = item.id,
                    sets = item.sets,
                    reps = item.reps,
                    weight = item.weightKg,
                    order = index + 1,
                    dayOfWeek = item.dayOfWeek
                )
            }
            selectRoutine(newRoutineId)
            closePresetRoutines()
        }
    }

    fun deleteRoutineExercise(id: Long) {
        viewModelScope.launch {
            routineRepository.deleteRoutineExercise(id)
        }
    }

    fun openWorkoutLogger(exercise: RoutineExerciseWithDetails) {
        _uiState.update {
            it.copy(
                selectedExerciseForLog = exercise,
                isLogWorkoutDialogOpen = true
            )
        }
    }

    fun closeWorkoutLogger() {
        _uiState.update {
            it.copy(
                selectedExerciseForLog = null,
                isLogWorkoutDialogOpen = false
            )
        }
    }

    fun confirmWorkoutLog(actualSets: Int, actualReps: Int, actualWeight: Double, notes: String?) {
        val exercise = _uiState.value.selectedExerciseForLog ?: return
        val today = LocalDate.now().toString()

        viewModelScope.launch {
            routineRepository.logWorkout(
                exerciseId = exercise.exerciseId,
                date = today,
                actualSets = actualSets,
                actualReps = actualReps,
                actualWeight = actualWeight,
                notes = notes,
                routineExerciseId = exercise.id
            )
            _uiState.update { it.copy(workoutDates = it.workoutDates + today) }
            closeWorkoutLogger()
        }
    }

    fun openProgressionDialog() {
        _uiState.update { it.copy(isProgressionDialogOpen = true) }
    }

    fun closeProgressionDialog() {
        _uiState.update { it.copy(isProgressionDialogOpen = false) }
    }

    fun openMuscleVolumeDialog() {
        _uiState.update { it.copy(isMuscleVolumeDialogOpen = true) }
    }

    fun closeMuscleVolumeDialog() {
        _uiState.update { it.copy(isMuscleVolumeDialogOpen = false) }
    }

    fun openWorkoutHeatmapDialog() {
        _uiState.update { it.copy(isWorkoutHeatmapDialogOpen = true) }
    }

    fun closeWorkoutHeatmapDialog() {
        _uiState.update { it.copy(isWorkoutHeatmapDialogOpen = false) }
    }
}
