package com.fittracker.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.math.roundToInt

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    val id: Int = 1,
    val gender: String = "MALE", // "MALE", "FEMALE"
    val age: Int = 26,
    val heightCm: Double = 178.0,
    val weightKg: Double = 75.0,
    val activityLevel: String = "MODERATE", // "SEDENTARY", "LIGHT", "MODERATE", "ACTIVE", "VERY_ACTIVE"
    val goal: String = "MAINTENANCE" // "FAT_LOSS", "MAINTENANCE", "MUSCLE_GAIN"
) {
    /**
     * Tasa Metabólica Basal (BMR) calculada con la fórmula de Mifflin-St Jeor:
     * Hombre: (10 * peso) + (6.25 * altura) - (5 * edad) + 5
     * Mujer:  (10 * peso) + (6.25 * altura) - (5 * edad) - 161
     */
    fun calculateBmr(): Double {
        val base = (10.0 * weightKg) + (6.25 * heightCm) - (5.0 * age)
        return if (gender.equals("FEMALE", ignoreCase = true)) {
            base - 161.0
        } else {
            base + 5.0
        }
    }

    /**
     * Gasto Energético Total Diario (TDEE) según factor de actividad
     */
    fun calculateTdee(): Double {
        val multiplier = when (activityLevel.uppercase()) {
            "SEDENTARY" -> 1.2
            "LIGHT" -> 1.375
            "MODERATE" -> 1.55
            "ACTIVE" -> 1.725
            "VERY_ACTIVE" -> 1.9
            else -> 1.55
        }
        return calculateBmr() * multiplier
    }

    /**
     * Objetivos recomendados de calorías y macros en función del objetivo físico
     */
    fun calculateRecommendedTargets(): UserTargets {
        val tdee = calculateTdee()
        val calorieTarget = when (goal.uppercase()) {
            "FAT_LOSS" -> (tdee - 450.0).coerceAtLeast(1400.0)
            "MUSCLE_GAIN" -> tdee + 300.0
            else -> tdee // MAINTENANCE
        }

        // Reparto de macros recomendado para recomposición:
        // Proteínas: ~2.0g - 2.2g por kg de peso corporal
        val proteinG = (weightKg * 2.0).coerceAtLeast(100.0)
        // Grasas: ~0.9g por kg de peso
        val fatG = (weightKg * 0.9).coerceAtLeast(45.0)
        // Carbohidratos: el resto de calorías (1g carb = 4 kcal, 1g prot = 4 kcal, 1g fat = 9 kcal)
        val caloriesFromProtAndFat = (proteinG * 4.0) + (fatG * 9.0)
        val carbsCalories = (calorieTarget - caloriesFromProtAndFat).coerceAtLeast(400.0)
        val carbsG = carbsCalories / 4.0

        return UserTargets(
            id = 1,
            calorieTarget = (calorieTarget.roundToInt()).toDouble(),
            proteinTarget = (proteinG.roundToInt()).toDouble(),
            carbTarget = (carbsG.roundToInt()).toDouble(),
            fatTarget = (fatG.roundToInt()).toDouble()
        )
    }
}
