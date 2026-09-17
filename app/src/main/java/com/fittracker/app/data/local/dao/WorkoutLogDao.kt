package com.fittracker.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fittracker.app.data.local.entities.WorkoutLog
import kotlinx.coroutines.flow.Flow

data class WorkoutLogWithExercise(
    val id: Long,
    val routineExerciseId: Long?,
    val exerciseId: String,
    val date: String,
    val actualSets: Int,
    val actualReps: Int,
    val actualWeight: Double,
    val notes: String?,
    val exerciseName: String,
    val muscleGroup: String
)

@Dao
interface WorkoutLogDao {
    @Query("""
        SELECT wl.id, wl.routineExerciseId, wl.exerciseId, wl.date, wl.actualSets, wl.actualReps,
               wl.actualWeight, wl.notes, e.name AS exerciseName, e.muscleGroup
        FROM workout_logs wl
        INNER JOIN exercises e ON wl.exerciseId = e.id
        WHERE wl.date = :date
        ORDER BY wl.id DESC
    """)
    fun getLogsByDate(date: String): Flow<List<WorkoutLogWithExercise>>

    @Query("""
        SELECT wl.id, wl.routineExerciseId, wl.exerciseId, wl.date, wl.actualSets, wl.actualReps,
               wl.actualWeight, wl.notes, e.name AS exerciseName, e.muscleGroup
        FROM workout_logs wl
        INNER JOIN exercises e ON wl.exerciseId = e.id
        WHERE wl.exerciseId = :exerciseId AND wl.date >= :sinceDate
        ORDER BY wl.date ASC, wl.id ASC
    """)
    suspend fun getExerciseHistorySince(exerciseId: String, sinceDate: String): List<WorkoutLogWithExercise>

    @Query("""
        SELECT wl.id, wl.routineExerciseId, wl.exerciseId, wl.date, wl.actualSets, wl.actualReps,
               wl.actualWeight, wl.notes, e.name AS exerciseName, e.muscleGroup
        FROM workout_logs wl
        INNER JOIN exercises e ON wl.exerciseId = e.id
        WHERE wl.date >= :sinceDate
        ORDER BY wl.date ASC, wl.id ASC
    """)
    suspend fun getAllWorkoutHistorySince(sinceDate: String): List<WorkoutLogWithExercise>

    @Query("SELECT COUNT(*) FROM workout_logs WHERE date = :date")
    fun getWorkoutCountForDate(date: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(workoutLog: WorkoutLog): Long

    @Delete
    suspend fun deleteLog(workoutLog: WorkoutLog)

    @Query("DELETE FROM workout_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("DELETE FROM workout_logs")
    suspend fun deleteAll()
}
