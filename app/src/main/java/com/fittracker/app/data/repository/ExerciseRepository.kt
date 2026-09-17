package com.fittracker.app.data.repository

import com.fittracker.app.data.local.dao.ExerciseDao
import com.fittracker.app.data.local.entities.Exercise
import kotlinx.coroutines.flow.Flow

class ExerciseRepository(
    private val exerciseDao: ExerciseDao
) {
    val allExercises: Flow<List<Exercise>> = exerciseDao.getAllExercises()

    fun getExercisesByMuscle(muscleGroup: String): Flow<List<Exercise>> {
        return exerciseDao.getExercisesByMuscle(muscleGroup)
    }

    fun searchExercises(query: String): Flow<List<Exercise>> {
        return exerciseDao.searchExercises(query)
    }

    suspend fun getExerciseById(id: String): Exercise? {
        return exerciseDao.getExerciseById(id)
    }

    suspend fun insertExercise(exercise: Exercise) {
        exerciseDao.insertExercise(exercise)
    }
}
