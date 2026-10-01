package com.fittracker.app.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fittracker.app.data.local.dao.RoutineExerciseWithDetails
import com.fittracker.app.data.local.entities.Exercise
import com.fittracker.app.data.repository.ExerciseRepository
import com.fittracker.app.data.repository.RoutineRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class LiveWorkoutUiState(
    val activeSession: ActiveWorkoutSession? = null,
    val isSheetExpanded: Boolean = false,
    val isWorkoutFinishedDialogVisible: Boolean = false,
    val finishedWorkoutSummary: FinishedWorkoutSummary? = null
)

data class FinishedWorkoutSummary(
    val title: String,
    val durationMinutes: Int,
    val totalVolumeKg: Double,
    val totalCompletedSets: Int,
    val exercisesCompleted: Int
)

class LiveWorkoutViewModel(
    private val routineRepository: RoutineRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LiveWorkoutUiState())
    val uiState: StateFlow<LiveWorkoutUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    private fun startTicker() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { state ->
                    val session = state.activeSession ?: return@update state
                    if (session.isPaused) return@update state

                    val newElapsed = session.elapsedSeconds + 1
                    val newRest = session.restTimerRemainingSeconds?.let {
                        if (it > 1) it - 1 else null
                    }

                    state.copy(
                        activeSession = session.copy(
                            elapsedSeconds = newElapsed,
                            restTimerRemainingSeconds = newRest
                        )
                    )
                }
            }
        }
    }

    fun startWorkoutFromRoutine(
        routineName: String,
        routineId: Long?,
        routineExercises: List<RoutineExerciseWithDetails>
    ) {
        viewModelScope.launch {
            val liveExercises = routineExercises.map { re ->
                // Buscar si existe historial previo para el ghost text
                val previousHistory = routineRepository.getExerciseHistory(re.exerciseId, weeks = 8).lastOrNull()
                val ghostText = if (previousHistory != null) {
                    "${previousHistory.actualWeight.toInt()} kg × ${previousHistory.actualReps}"
                } else if (re.weight > 0) {
                    "${re.weight.toInt()} kg × ${re.reps}"
                } else null

                val setsCount = re.sets.coerceIn(1, 10)
                val sets = (1..setsCount).map { index ->
                    LiveWorkoutSet(
                        id = UUID.randomUUID().toString(),
                        setNumber = index,
                        type = SetType.NORMAL,
                        targetWeightKg = re.weight,
                        targetReps = re.reps,
                        weightKg = if (re.weight > 0) re.weight.toString() else "",
                        reps = re.reps.toString(),
                        previousGhost = ghostText,
                        isCompleted = false
                    )
                }
                LiveWorkoutExercise(
                    exerciseId = re.exerciseId,
                    exerciseName = re.exerciseName,
                    muscleGroup = re.muscleGroup,
                    sets = sets
                )
            }

            val session = ActiveWorkoutSession(
                id = UUID.randomUUID().toString(),
                routineId = routineId,
                routineName = routineName.ifBlank { "Entrenamiento de Hoy" },
                startTime = LocalDateTime.now(),
                exercises = liveExercises
            )

            _uiState.update {
                it.copy(
                    activeSession = session,
                    isSheetExpanded = true
                )
            }
            startTicker()
        }
    }

    fun startFreeWorkout() {
        val session = ActiveWorkoutSession(
            id = UUID.randomUUID().toString(),
            routineName = "Entrenamiento Libre",
            startTime = LocalDateTime.now(),
            exercises = emptyList()
        )
        _uiState.update {
            it.copy(
                activeSession = session,
                isSheetExpanded = true
            )
        }
        startTicker()
    }

    fun addExerciseToWorkout(exercise: Exercise) {
        viewModelScope.launch {
            val previous = routineRepository.getExerciseHistory(exercise.id, 8).lastOrNull()
            val ghost = previous?.let { "${it.actualWeight.toInt()} kg × ${it.actualReps}" }

            val initialSets = (1..3).map { index ->
                LiveWorkoutSet(
                    id = UUID.randomUUID().toString(),
                    setNumber = index,
                    type = SetType.NORMAL,
                    targetWeightKg = previous?.actualWeight ?: 20.0,
                    targetReps = previous?.actualReps ?: 10,
                    weightKg = previous?.actualWeight?.toString() ?: "",
                    reps = previous?.actualReps?.toString() ?: "10",
                    previousGhost = ghost
                )
            }

            val newExercise = LiveWorkoutExercise(
                exerciseId = exercise.id,
                exerciseName = exercise.name,
                muscleGroup = exercise.muscleGroup,
                sets = initialSets
            )

            _uiState.update { state ->
                val session = state.activeSession ?: return@update state
                state.copy(
                    activeSession = session.copy(
                        exercises = session.exercises + newExercise
                    )
                )
            }
        }
    }

    fun toggleSetCompletion(exerciseId: String, setId: String) {
        var startRest = false
        _uiState.update { state ->
            val session = state.activeSession ?: return@update state
            val updatedExercises = session.exercises.map { ex ->
                if (ex.exerciseId != exerciseId) ex
                else {
                    val updatedSets = ex.sets.map { s ->
                        if (s.id == setId) {
                            val newComplete = !s.isCompleted
                            if (newComplete) startRest = true
                            s.copy(isCompleted = newComplete)
                        } else s
                    }
                    ex.copy(sets = updatedSets)
                }
            }
            val rest = if (startRest) session.restTimerTotalSeconds else session.restTimerRemainingSeconds
            state.copy(
                activeSession = session.copy(
                    exercises = updatedExercises,
                    restTimerRemainingSeconds = rest
                )
            )
        }
    }

    fun updateSetWeight(exerciseId: String, setId: String, weight: String) {
        _uiState.update { state ->
            val session = state.activeSession ?: return@update state
            val updated = session.exercises.map { ex ->
                if (ex.exerciseId != exerciseId) ex
                else ex.copy(sets = ex.sets.map { s -> if (s.id == setId) s.copy(weightKg = weight) else s })
            }
            state.copy(activeSession = session.copy(exercises = updated))
        }
    }

    fun incrementSetWeight(exerciseId: String, setId: String, delta: Double) {
        _uiState.update { state ->
            val session = state.activeSession ?: return@update state
            val updated = session.exercises.map { ex ->
                if (ex.exerciseId != exerciseId) ex
                else ex.copy(sets = ex.sets.map { s ->
                    if (s.id == setId) {
                        val currentVal = s.weightKg.toDoubleOrNull() ?: s.targetWeightKg
                        val newVal = (currentVal + delta).coerceAtLeast(0.0)
                        val formatted = if (newVal % 1.0 == 0.0) newVal.toInt().toString() else String.format(java.util.Locale.US, "%.1f", newVal)
                        s.copy(weightKg = formatted)
                    } else s
                })
            }
            state.copy(activeSession = session.copy(exercises = updated))
        }
    }

    fun updateSetReps(exerciseId: String, setId: String, reps: String) {
        _uiState.update { state ->
            val session = state.activeSession ?: return@update state
            val updated = session.exercises.map { ex ->
                if (ex.exerciseId != exerciseId) ex
                else ex.copy(sets = ex.sets.map { s -> if (s.id == setId) s.copy(reps = reps) else s })
            }
            state.copy(activeSession = session.copy(exercises = updated))
        }
    }

    fun incrementSetReps(exerciseId: String, setId: String, delta: Int) {
        _uiState.update { state ->
            val session = state.activeSession ?: return@update state
            val updated = session.exercises.map { ex ->
                if (ex.exerciseId != exerciseId) ex
                else ex.copy(sets = ex.sets.map { s ->
                    if (s.id == setId) {
                        val currentVal = s.reps.toIntOrNull() ?: s.targetReps
                        val newVal = (currentVal + delta).coerceAtLeast(1)
                        s.copy(reps = newVal.toString())
                    } else s
                })
            }
            state.copy(activeSession = session.copy(exercises = updated))
        }
    }

    fun updateExerciseAllSetsWeightAndReps(exerciseId: String, weight: Double, reps: Int) {
        _uiState.update { state ->
            val session = state.activeSession ?: return@update state
            val weightStr = if (weight % 1.0 == 0.0) weight.toInt().toString() else String.format(java.util.Locale.US, "%.1f", weight)
            val repsStr = reps.toString()
            val updated = session.exercises.map { ex ->
                if (ex.exerciseId != exerciseId) ex
                else ex.copy(sets = ex.sets.map { s ->
                    s.copy(
                        targetWeightKg = weight,
                        targetReps = reps,
                        weightKg = weightStr,
                        reps = repsStr
                    )
                })
            }
            state.copy(activeSession = session.copy(exercises = updated))
        }
    }

    fun cycleSetType(exerciseId: String, setId: String) {
        _uiState.update { state ->
            val session = state.activeSession ?: return@update state
            val updated = session.exercises.map { ex ->
                if (ex.exerciseId != exerciseId) ex
                else ex.copy(sets = ex.sets.map { s ->
                    if (s.id == setId) {
                        val nextType = when (s.type) {
                            SetType.NORMAL -> SetType.WARMUP
                            SetType.WARMUP -> SetType.FAILURE
                            SetType.FAILURE -> SetType.DROP_SET
                            SetType.DROP_SET -> SetType.NORMAL
                        }
                        s.copy(type = nextType)
                    } else s
                })
            }
            state.copy(activeSession = session.copy(exercises = updated))
        }
    }

    fun addSet(exerciseId: String) {
        _uiState.update { state ->
            val session = state.activeSession ?: return@update state
            val updated = session.exercises.map { ex ->
                if (ex.exerciseId != exerciseId) ex
                else {
                    val last = ex.sets.lastOrNull()
                    val newSet = LiveWorkoutSet(
                        id = UUID.randomUUID().toString(),
                        setNumber = ex.sets.size + 1,
                        type = last?.type ?: SetType.NORMAL,
                        targetWeightKg = last?.targetWeightKg ?: 0.0,
                        targetReps = last?.targetReps ?: 10,
                        weightKg = last?.weightKg ?: "",
                        reps = last?.reps ?: "10",
                        previousGhost = last?.previousGhost
                    )
                    ex.copy(sets = ex.sets + newSet)
                }
            }
            state.copy(activeSession = session.copy(exercises = updated))
        }
    }

    fun removeSet(exerciseId: String, setId: String) {
        _uiState.update { state ->
            val session = state.activeSession ?: return@update state
            val updated = session.exercises.map { ex ->
                if (ex.exerciseId != exerciseId) ex
                else {
                    val filtered = ex.sets.filter { it.id != setId }
                    val renumbered = filtered.mapIndexed { idx, s -> s.copy(setNumber = idx + 1) }
                    ex.copy(sets = renumbered)
                }
            }
            state.copy(activeSession = session.copy(exercises = updated))
        }
    }

    fun removeExercise(exerciseId: String) {
        _uiState.update { state ->
            val session = state.activeSession ?: return@update state
            state.copy(
                activeSession = session.copy(
                    exercises = session.exercises.filter { it.exerciseId != exerciseId }
                )
            )
        }
    }

    fun startRestTimer(seconds: Int) {
        _uiState.update { state ->
            val session = state.activeSession ?: return@update state
            state.copy(
                activeSession = session.copy(
                    restTimerRemainingSeconds = seconds,
                    restTimerTotalSeconds = seconds
                )
            )
        }
    }

    fun adjustRestTimer(deltaSeconds: Int) {
        _uiState.update { state ->
            val session = state.activeSession ?: return@update state
            val current = session.restTimerRemainingSeconds ?: 0
            val next = (current + deltaSeconds).coerceAtLeast(0)
            state.copy(
                activeSession = session.copy(
                    restTimerRemainingSeconds = if (next == 0) null else next
                )
            )
        }
    }

    fun skipRestTimer() {
        _uiState.update { state ->
            val session = state.activeSession ?: return@update state
            state.copy(activeSession = session.copy(restTimerRemainingSeconds = null))
        }
    }

    fun expandSheet() {
        _uiState.update { it.copy(isSheetExpanded = true) }
    }

    fun collapseSheet() {
        _uiState.update { it.copy(isSheetExpanded = false) }
    }

    fun finishWorkout() {
        val session = _uiState.value.activeSession ?: return
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            var loggedSetsCount = 0

            // Guardar los ejercicios en Room
            session.exercises.forEach { ex ->
                val completedSets = ex.sets.filter { it.isCompleted }
                if (completedSets.isNotEmpty()) {
                    val setsCount = completedSets.size
                    val avgReps = (completedSets.sumOf { it.reps.toIntOrNull() ?: it.targetReps } / setsCount).coerceAtLeast(1)
                    val maxWeight = completedSets.maxOfOrNull { it.weightKg.toDoubleOrNull() ?: it.targetWeightKg } ?: 0.0

                    routineRepository.logWorkout(
                        exerciseId = ex.exerciseId,
                        date = today,
                        actualSets = setsCount,
                        actualReps = avgReps,
                        actualWeight = maxWeight,
                        notes = "Sesión en vivo: ${session.routineName}"
                    )
                    loggedSetsCount += setsCount
                }
            }

            timerJob?.cancel()

            val summary = FinishedWorkoutSummary(
                title = session.routineName,
                durationMinutes = (session.elapsedSeconds / 60).toInt().coerceAtLeast(1),
                totalVolumeKg = session.totalVolumeKg,
                totalCompletedSets = session.completedSetsCount,
                exercisesCompleted = session.exercises.count { ex -> ex.sets.any { it.isCompleted } }
            )

            _uiState.update {
                it.copy(
                    activeSession = null,
                    isSheetExpanded = false,
                    isWorkoutFinishedDialogVisible = true,
                    finishedWorkoutSummary = summary
                )
            }
        }
    }

    fun dismissFinishedDialog() {
        _uiState.update {
            it.copy(
                isWorkoutFinishedDialogVisible = false,
                finishedWorkoutSummary = null
            )
        }
    }

    fun cancelWorkout() {
        timerJob?.cancel()
        _uiState.update {
            it.copy(
                activeSession = null,
                isSheetExpanded = false
            )
        }
    }
}
