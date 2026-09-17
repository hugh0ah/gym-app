package com.fittracker.app.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "body_composition_logs",
    indices = [Index("date")]
)
data class BodyCompositionLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val weightKg: Double, // Peso en kg
    val bmi: Double, // Índice de masa corporal (IMC)
    val bodyFatPercentage: Double, // % Grasa corporal
    val muscleMassKg: Double, // Masa muscular en kg
    val visceralFat: Int, // Nivel de grasa visceral (1-20)
    val bmrKcal: Double, // Tasa metabólica basal en kcal
    val bodyWaterPercentage: Double, // % Agua corporal
    val boneMassKg: Double, // Masa ósea en kg
    val proteinPercentage: Double, // % Proteínas corporales
    val notes: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
