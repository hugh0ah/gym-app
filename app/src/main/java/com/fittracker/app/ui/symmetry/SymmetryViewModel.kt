package com.fittracker.app.ui.symmetry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fittracker.app.data.local.dao.WorkoutLogWithExercise
import com.fittracker.app.data.repository.RoutineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class SymmetryUiState(
    val viewMode: BodyViewMode = BodyViewMode.ANTERIOR,
    val selectedMuscleId: String = "chest",
    val muscles: List<MuscleData> = emptyList(),
    val balance: SymmetryBalance = SymmetryBalance(),
    val totalWeeklySets: Int = 0,
    val isLoading: Boolean = true
)

class SymmetryViewModel(
    private val routineRepository: RoutineRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SymmetryUiState())
    val uiState: StateFlow<SymmetryUiState> = _uiState.asStateFlow()

    init {
        loadSymmetryData()
    }

    fun setViewMode(mode: BodyViewMode) {
        _uiState.update { it.copy(viewMode = mode) }
    }

    fun selectMuscle(muscleId: String) {
        _uiState.update { it.copy(selectedMuscleId = muscleId) }
    }

    fun loadSymmetryData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val oneWeekAgo = LocalDate.now().minusDays(7).toString()

            routineRepository.getAllWorkoutHistorySinceFlow(oneWeekAgo).collect { logs ->
                processMuscleVolumes(logs)
            }
        }
    }

    private fun processMuscleVolumes(logs: List<WorkoutLogWithExercise>) {
        // Mapa base de músculos
        val baseMuscles = listOf(
            MuscleData("chest", "Chest", "Pectorales (Pecho)", isUpperBody = true, isPush = true, isFront = true, mev = 8, mav = 14, mrv = 22),
            MuscleData("back", "Back", "Espalda y Dorsales", isUpperBody = true, isPush = false, isFront = false, mev = 10, mav = 16, mrv = 24),
            MuscleData("shoulders", "Shoulders", "Hombros (Deltoides)", isUpperBody = true, isPush = true, isFront = true, mev = 8, mav = 14, mrv = 20),
            MuscleData("biceps", "Biceps", "Bíceps", isUpperBody = true, isPush = false, isFront = true, mev = 6, mav = 12, mrv = 18),
            MuscleData("triceps", "Triceps", "Tríceps", isUpperBody = true, isPush = true, isFront = false, mev = 6, mav = 12, mrv = 18),
            MuscleData("forearms", "Forearms", "Antebrazos", isUpperBody = true, isPush = false, isFront = true, mev = 4, mav = 8, mrv = 14),
            MuscleData("core", "Core", "Abdomen y Core", isUpperBody = true, isPush = null, isFront = true, mev = 6, mav = 12, mrv = 18),
            MuscleData("quads", "Quads", "Cuádriceps", isUpperBody = false, isPush = null, isFront = true, mev = 8, mav = 14, mrv = 20),
            MuscleData("hamstrings", "Hamstrings", "Isquiosurales / Femorales", isUpperBody = false, isPush = null, isFront = false, mev = 6, mav = 12, mrv = 18),
            MuscleData("glutes", "Glutes", "Glúteos", isUpperBody = false, isPush = null, isFront = false, mev = 6, mav = 12, mrv = 18),
            MuscleData("calves", "Calves", "Gemelos y Pantorrillas", isUpperBody = false, isPush = null, isFront = true, mev = 6, mav = 10, mrv = 16)
        )

        val volumeMap = mutableMapOf<String, Int>()
        val exercisesMap = mutableMapOf<String, MutableList<String>>()

        baseMuscles.forEach {
            volumeMap[it.id] = 0
            exercisesMap[it.id] = mutableListOf()
        }

        // Procesar registros reales de la última semana
        logs.forEach { log ->
            val muscle = log.muscleGroup.lowercase()
            val targetKey = when {
                muscle.contains("pecho") || muscle.contains("chest") -> "chest"
                muscle.contains("espalda") || muscle.contains("back") || muscle.contains("dorsal") || muscle.contains("trap") || muscle.contains("lat") -> "back"
                muscle.contains("hombro") || muscle.contains("shoulder") || muscle.contains("delt") -> "shoulders"
                muscle.contains("bicep") || muscle.contains("bícep") -> "biceps"
                muscle.contains("tricep") || muscle.contains("trícep") -> "triceps"
                muscle.contains("antebrazo") || muscle.contains("forearm") -> "forearms"
                muscle.contains("core") || muscle.contains("abs") || muscle.contains("abdom") -> "core"
                muscle.contains("quad") || muscle.contains("cuádr") || muscle.contains("cuadr") -> "quads"
                muscle.contains("isquio") || muscle.contains("hamstring") || muscle.contains("femoral") -> "hamstrings"
                muscle.contains("glute") -> "glutes"
                muscle.contains("gemelo") || muscle.contains("calf") -> "calves"
                else -> null
            }

            if (targetKey != null) {
                volumeMap[targetKey] = (volumeMap[targetKey] ?: 0) + log.actualSets.coerceAtLeast(1)
                val list = exercisesMap[targetKey] ?: mutableListOf()
                if (!list.contains(log.exerciseName)) {
                    list.add(log.exerciseName)
                }
                exercisesMap[targetKey] = list
            }
        }

        val updatedMuscles = baseMuscles.map { base ->
            base.copy(
                weeklySets = volumeMap[base.id] ?: 0,
                targetExercises = exercisesMap[base.id] ?: emptyList()
            )
        }

        // Ratios de balance
        val pushSets = updatedMuscles.filter { it.isPush == true }.sumOf { it.weeklySets }
        val pullSets = updatedMuscles.filter { it.isPush == false }.sumOf { it.weeklySets }
        val upperSets = updatedMuscles.filter { it.isUpperBody }.sumOf { it.weeklySets }
        val lowerSets = updatedMuscles.filter { !it.isUpperBody }.sumOf { it.weeklySets }
        val legsSets = lowerSets
        val coreSets = updatedMuscles.find { it.id == "core" }?.weeklySets ?: 0

        val balance = SymmetryBalance(
            pushSets = pushSets,
            pullSets = pullSets,
            legsSets = legsSets,
            coreSets = coreSets,
            upperSets = upperSets,
            lowerSets = lowerSets
        )

        _uiState.update {
            it.copy(
                muscles = updatedMuscles,
                balance = balance,
                totalWeeklySets = updatedMuscles.sumOf { m -> m.weeklySets },
                isLoading = false
            )
        }
    }
}
