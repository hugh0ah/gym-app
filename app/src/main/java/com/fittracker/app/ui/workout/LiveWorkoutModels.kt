package com.fittracker.app.ui.workout

import androidx.compose.runtime.Immutable
import java.time.LocalDateTime
import kotlin.math.roundToInt

enum class SetType(val code: String, val label: String, val shortLabel: String) {
    NORMAL("N", "Normal", "N"),
    WARMUP("W", "Calentamiento", "W"),
    FAILURE("F", "Al Fallo", "F"),
    DROP_SET("D", "Drop Set", "D")
}

@Immutable
data class LiveWorkoutSet(
    val id: String,
    val setNumber: Int,
    val type: SetType = SetType.NORMAL,
    val targetWeightKg: Double = 0.0,
    val targetReps: Int = 10,
    val weightKg: String = "",
    val reps: String = "",
    val previousGhost: String? = null, // e.g. "75 kg × 8"
    val rpe: Int? = 8,
    val isCompleted: Boolean = false
) {
    val estimated1RM: Double
        get() {
            val w = weightKg.toDoubleOrNull() ?: targetWeightKg
            val r = reps.toIntOrNull() ?: targetReps
            if (w <= 0.0 || r <= 0) return 0.0
            if (r == 1) return w
            // Brzycki formula
            val calculated = w * (36.0 / (37.0 - r.coerceIn(1, 30)))
            return (calculated * 10.0).roundToInt() / 10.0
        }
}

@Immutable
data class LiveWorkoutExercise(
    val exerciseId: String,
    val exerciseName: String,
    val muscleGroup: String,
    val sets: List<LiveWorkoutSet> = emptyList(),
    val notes: String = ""
)

@Immutable
data class ActiveWorkoutSession(
    val id: String,
    val routineId: Long? = null,
    val routineName: String,
    val startTime: LocalDateTime = LocalDateTime.now(),
    val exercises: List<LiveWorkoutExercise> = emptyList(),
    val elapsedSeconds: Long = 0L,
    val isPaused: Boolean = false,
    val restTimerRemainingSeconds: Int? = null,
    val restTimerTotalSeconds: Int = 90
) {
    val completedSetsCount: Int
        get() = exercises.sumOf { ex -> ex.sets.count { it.isCompleted } }

    val totalSetsCount: Int
        get() = exercises.sumOf { it.sets.size }

    val totalVolumeKg: Double
        get() = exercises.sumOf { ex ->
            ex.sets.filter { it.isCompleted }.sumOf { s ->
                val w = s.weightKg.toDoubleOrNull() ?: s.targetWeightKg
                val r = s.reps.toIntOrNull() ?: s.targetReps
                w * r
            }
        }
}
