package com.fittracker.app.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "weight_logs",
    indices = [Index("date")]
)
data class WeightLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val weightKg: Double,
    val notes: String? = null,
    val loggedAt: Long = System.currentTimeMillis()
)
