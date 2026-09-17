package com.fittracker.app.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "routine_exercises",
    foreignKeys = [
        ForeignKey(
            entity = Routine::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("routineId"),
        Index("exerciseId")
    ]
)
data class RoutineExercise(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val routineId: Long,
    val exerciseId: String,
    val sets: Int,
    val reps: Int,
    val weight: Double,
    val order: Int,
    val dayOfWeek: Int // 1 = Lunes, 2 = Martes, ..., 7 = Domingo
)
