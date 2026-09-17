package com.fittracker.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_targets")
data class UserTargets(
    @PrimaryKey
    val id: Int = 1,
    val calorieTarget: Double = 2200.0,
    val proteinTarget: Double = 160.0,
    val carbTarget: Double = 240.0,
    val fatTarget: Double = 65.0
)
