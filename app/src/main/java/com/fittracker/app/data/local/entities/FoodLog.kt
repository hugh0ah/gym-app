package com.fittracker.app.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "food_logs",
    indices = [
        Index("date")
    ]
)
data class FoodLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val foodName: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val quantityG: Double,
    val mealType: String = "Comida" // "Desayuno", "Comida", "Cena", "Snack"
)
