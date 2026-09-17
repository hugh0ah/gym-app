package com.fittracker.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_cache")
data class FoodCache(
    @PrimaryKey
    val code: String,
    val name: String,
    val caloriesPer100g: Double,
    val proteinPer100g: Double,
    val carbsPer100g: Double,
    val fatPer100g: Double,
    val brand: String? = null,
    val cachedAt: Long = System.currentTimeMillis()
)
