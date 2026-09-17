package com.fittracker.app.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "water_logs",
    indices = [
        Index("date")
    ]
)
data class WaterLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val amountMl: Int,
    val timestamp: Long = System.currentTimeMillis()
)
