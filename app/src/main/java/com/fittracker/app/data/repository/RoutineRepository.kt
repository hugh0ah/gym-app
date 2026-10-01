package com.fittracker.app.data.repository

import com.fittracker.app.data.local.dao.RoutineDao
import com.fittracker.app.data.local.dao.RoutineExerciseWithDetails
import com.fittracker.app.data.local.dao.WorkoutLogDao
import com.fittracker.app.data.local.dao.WorkoutLogWithExercise
import com.fittracker.app.data.local.entities.Routine
import com.fittracker.app.data.local.entities.RoutineExercise
import com.fittracker.app.data.local.entities.WorkoutLog
import com.fittracker.app.data.remote.gemini.RoutineChangeProposal
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class RoutineRepository(
    private val routineDao: RoutineDao,
    private val workoutLogDao: WorkoutLogDao
) {
    val allRoutines: Flow<List<Routine>> = routineDao.getAllRoutines()

    fun getExercisesForDay(routineId: Long, dayOfWeek: Int): Flow<List<RoutineExerciseWithDetails>> {
        return routineDao.getExercisesForDay(routineId, dayOfWeek)
    }

    fun getRoutineExercises(routineId: Long): Flow<List<RoutineExerciseWithDetails>> {
        return routineDao.getRoutineExercisesWithDetails(routineId)
    }

    suspend fun getRoutineExercisesDirect(routineId: Long): List<RoutineExerciseWithDetails> {
        return routineDao.getRoutineExercisesDirect(routineId)
    }

    suspend fun getRoutineById(id: Long): Routine? {
        return routineDao.getRoutineById(id)
    }

    suspend fun createRoutine(name: String): Long {
        return routineDao.insertRoutine(Routine(name = name))
    }

    suspend fun deleteRoutine(routine: Routine) {
        routineDao.deleteRoutine(routine)
    }

    suspend fun addExerciseToRoutine(
        routineId: Long,
        exerciseId: String,
        sets: Int,
        reps: Int,
        weight: Double,
        order: Int,
        dayOfWeek: Int
    ): Long {
        return routineDao.insertRoutineExercise(
            RoutineExercise(
                routineId = routineId,
                exerciseId = exerciseId,
                sets = sets,
                reps = reps,
                weight = weight,
                order = order,
                dayOfWeek = dayOfWeek
            )
        )
    }

    suspend fun updateRoutineExerciseValues(id: Long, sets: Int, reps: Int, weight: Double) {
        routineDao.updateRoutineExerciseValues(id, sets, reps, weight)
    }

    suspend fun deleteRoutineExercise(id: Long) {
        routineDao.deleteRoutineExerciseById(id)
    }

    fun getWorkoutLogsByDate(date: String): Flow<List<WorkoutLogWithExercise>> {
        return workoutLogDao.getLogsByDate(date)
    }

    fun getWorkoutCountForDate(date: String): Flow<Int> {
        return workoutLogDao.getWorkoutCountForDate(date)
    }

    suspend fun logWorkout(
        exerciseId: String,
        date: String,
        actualSets: Int,
        actualReps: Int,
        actualWeight: Double,
        notes: String? = null,
        routineExerciseId: Long? = null
    ): Long {
        return workoutLogDao.insertLog(
            WorkoutLog(
                routineExerciseId = routineExerciseId,
                exerciseId = exerciseId,
                date = date,
                actualSets = actualSets,
                actualReps = actualReps,
                actualWeight = actualWeight,
                notes = notes
            )
        )
    }

    suspend fun deleteWorkoutLog(id: Long) {
        workoutLogDao.deleteLogById(id)
    }

    suspend fun getExerciseHistory(exerciseId: String, weeks: Int): List<WorkoutLogWithExercise> {
        val sinceDate = LocalDate.now().minusWeeks(weeks.toLong()).toString()
        return workoutLogDao.getExerciseHistorySince(exerciseId, sinceDate)
    }

    suspend fun getAllWorkoutHistorySince(sinceDate: String): List<WorkoutLogWithExercise> {
        return workoutLogDao.getAllWorkoutHistorySince(sinceDate)
    }

    fun getAllWorkoutHistorySinceFlow(sinceDate: String): Flow<List<WorkoutLogWithExercise>> {
        return workoutLogDao.getAllWorkoutHistorySinceFlow(sinceDate)
    }

    fun getWorkoutDatesSinceFlow(weeks: Int): Flow<List<String>> {
        val sinceDate = LocalDate.now().minusWeeks(weeks.toLong()).toString()
        return workoutLogDao.getWorkoutDatesSinceFlow(sinceDate)
    }

    fun getAllWorkoutDatesFlow(): Flow<List<String>> {
        return workoutLogDao.getAllWorkoutDatesFlow()
    }

    /**
     * Aplica la propuesta sugerida por la IA tras la confirmación explícita del usuario.
     */
    suspend fun applyProposalChange(proposal: RoutineChangeProposal) {
        val existing = routineDao.getRoutineExercisesDirect(proposal.routineId)
            .find { it.exerciseId == proposal.exerciseId && it.dayOfWeek == proposal.dayOfWeek }

        if (existing != null) {
            routineDao.updateRoutineExercise(
                RoutineExercise(
                    id = existing.id,
                    routineId = proposal.routineId,
                    exerciseId = proposal.exerciseId,
                    sets = proposal.proposedSets,
                    reps = proposal.proposedReps,
                    weight = proposal.proposedWeight,
                    order = existing.order,
                    dayOfWeek = proposal.dayOfWeek
                )
            )
        } else {
            val maxOrder = routineDao.getRoutineExercisesDirect(proposal.routineId)
                .filter { it.dayOfWeek == proposal.dayOfWeek }
                .maxOfOrNull { it.order } ?: 0

            routineDao.insertRoutineExercise(
                RoutineExercise(
                    routineId = proposal.routineId,
                    exerciseId = proposal.exerciseId,
                    sets = proposal.proposedSets,
                    reps = proposal.proposedReps,
                    weight = proposal.proposedWeight,
                    order = maxOrder + 1,
                    dayOfWeek = proposal.dayOfWeek
                )
            )
        }
    }

    suspend fun clearAllRoutinesAndWorkouts() {
        routineDao.deleteAllRoutineExercises()
        routineDao.deleteAllRoutines()
        workoutLogDao.deleteAll()
    }
}
