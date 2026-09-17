package com.fittracker.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_activity_summary")
data class DailyActivitySummary(
    @PrimaryKey
    val date: String, // YYYY-MM-DD
    val steps: Int,
    val activeCalories: Double,
    val exerciseMinutes: Int,
    val exerciseType: String
)
