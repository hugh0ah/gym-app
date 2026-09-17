package com.fittracker.app.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_logs",
    indices = [
        Index("exerciseId"),
        Index("date")
    ]
)
data class WorkoutLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val routineExerciseId: Long? = null,
    val exerciseId: String,
    val date: String, // YYYY-MM-DD
    val actualSets: Int,
    val actualReps: Int,
    val actualWeight: Double,
    val notes: String? = null
)
