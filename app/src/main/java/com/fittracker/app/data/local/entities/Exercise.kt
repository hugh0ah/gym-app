package com.fittracker.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey
    val id: String,
    val name: String,
    val muscleGroup: String,
    val equipment: String,
    val level: String,
    val instructions: String,
    val imageUrl: String? = null
)
