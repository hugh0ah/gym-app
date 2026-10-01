package com.fittracker.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fittracker.app.data.local.entities.Exercise
import com.fittracker.app.data.local.entities.Routine
import com.fittracker.app.data.local.entities.RoutineExercise
import kotlinx.coroutines.flow.Flow

data class RoutineExerciseWithDetails(
    val id: Long,
    val routineId: Long,
    val exerciseId: String,
    val sets: Int,
    val reps: Int,
    val weight: Double,
    val order: Int,
    val dayOfWeek: Int,
    val exerciseName: String,
    val muscleGroup: String,
    val equipment: String,
    val imageUrl: String?
)

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines ORDER BY createdAt DESC")
    fun getAllRoutines(): Flow<List<Routine>>

    @Query("SELECT * FROM routines WHERE id = :id")
    suspend fun getRoutineById(id: Long): Routine?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: Routine): Long

    @Update
    suspend fun updateRoutine(routine: Routine)

    @Delete
    suspend fun deleteRoutine(routine: Routine)

    @Query("""
        SELECT re.id, re.routineId, re.exerciseId, re.sets, re.reps, re.weight, re.`order`, re.dayOfWeek,
               COALESCE(e.name, re.exerciseId) AS exerciseName,
               COALESCE(e.muscleGroup, 'General') AS muscleGroup,
               COALESCE(e.equipment, 'Gimnasio') AS equipment,
               e.imageUrl
        FROM routine_exercises re
        LEFT JOIN exercises e ON re.exerciseId = e.id
        WHERE re.routineId = :routineId
        ORDER BY re.dayOfWeek ASC, re.`order` ASC
    """)
    fun getRoutineExercisesWithDetails(routineId: Long): Flow<List<RoutineExerciseWithDetails>>

    @Query("""
        SELECT re.id, re.routineId, re.exerciseId, re.sets, re.reps, re.weight, re.`order`, re.dayOfWeek,
               COALESCE(e.name, re.exerciseId) AS exerciseName,
               COALESCE(e.muscleGroup, 'General') AS muscleGroup,
               COALESCE(e.equipment, 'Gimnasio') AS equipment,
               e.imageUrl
        FROM routine_exercises re
        LEFT JOIN exercises e ON re.exerciseId = e.id
        WHERE re.routineId = :routineId AND re.dayOfWeek = :dayOfWeek
        ORDER BY re.`order` ASC
    """)
    fun getExercisesForDay(routineId: Long, dayOfWeek: Int): Flow<List<RoutineExerciseWithDetails>>

    @Query("""
        SELECT re.id, re.routineId, re.exerciseId, re.sets, re.reps, re.weight, re.`order`, re.dayOfWeek,
               COALESCE(e.name, re.exerciseId) AS exerciseName,
               COALESCE(e.muscleGroup, 'General') AS muscleGroup,
               COALESCE(e.equipment, 'Gimnasio') AS equipment,
               e.imageUrl
        FROM routine_exercises re
        LEFT JOIN exercises e ON re.exerciseId = e.id
        WHERE re.routineId = :routineId
        ORDER BY re.dayOfWeek ASC, re.`order` ASC
    """)
    suspend fun getRoutineExercisesDirect(routineId: Long): List<RoutineExerciseWithDetails>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineExercise(routineExercise: RoutineExercise): Long

    @Update
    suspend fun updateRoutineExercise(routineExercise: RoutineExercise)

    @Query("UPDATE routine_exercises SET sets = :sets, reps = :reps, weight = :weight WHERE id = :id")
    suspend fun updateRoutineExerciseValues(id: Long, sets: Int, reps: Int, weight: Double)

    @Delete
    suspend fun deleteRoutineExercise(routineExercise: RoutineExercise)

    @Query("DELETE FROM routine_exercises WHERE id = :id")
    suspend fun deleteRoutineExerciseById(id: Long)

    @Query("DELETE FROM routine_exercises WHERE routineId = :routineId")
    suspend fun deleteAllExercisesForRoutine(routineId: Long)

    @Query("DELETE FROM routine_exercises")
    suspend fun deleteAllRoutineExercises()

    @Query("DELETE FROM routines")
    suspend fun deleteAllRoutines()
}
