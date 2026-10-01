package com.fittracker.app.ui.symmetry

import androidx.compose.ui.graphics.Color
import com.fittracker.app.ui.theme.*

enum class BodyViewMode {
    ANTERIOR, // Frontal
    POSTERIOR // Dorsal
}

enum class VolumeStatus(val label: String, val color: Color, val description: String) {
    UNTRAINED("Sin entrenar", Color(0xFF263248), "Menos del mínimo para retener masa muscular"),
    MEV("MEV (Mantenimiento)", NeonMint, "Volumen Mínimo Efectivo (6-9 series/semana)"),
    MAV("MAV (Hipertrofia Óptima)", ElectricCyan, "Volumen Adaptativo Máximo (10-18 series/semana)"),
    MRV("MRV (Límite / Fatiga)", AlertRed, "Máximo Volumen Recuperable (>18 series/semana)")
}

data class MuscleData(
    val id: String,
    val name: String,
    val spanishName: String,
    val isUpperBody: Boolean,
    val isPush: Boolean?, // true: push, false: pull, null: legs/core
    val isFront: Boolean,
    val mev: Int = 8,
    val mav: Int = 14,
    val mrv: Int = 20,
    val weeklySets: Int = 0,
    val targetExercises: List<String> = emptyList()
) {
    val status: VolumeStatus
        get() = when {
            weeklySets <= 0 -> VolumeStatus.UNTRAINED
            weeklySets < mev -> VolumeStatus.MEV
            weeklySets <= mav + 4 -> VolumeStatus.MAV
            else -> VolumeStatus.MRV
        }

    val progressFraction: Float
        get() = (weeklySets.toFloat() / mrv.toFloat()).coerceIn(0f, 1.2f)
}

data class SymmetryBalance(
    val pushSets: Int = 0,
    val pullSets: Int = 0,
    val legsSets: Int = 0,
    val coreSets: Int = 0,
    val upperSets: Int = 0,
    val lowerSets: Int = 0
) {
    val pushPullRatio: Float
        get() = if (pullSets == 0) 1f else (pushSets.toFloat() / pullSets.toFloat())

    val upperLowerRatio: Float
        get() = if (lowerSets == 0) 1f else (upperSets.toFloat() / lowerSets.toFloat())

    val overallScore: Int
        get() {
            if (upperSets == 0 && lowerSets == 0) return 0
            // Balance ideal: Push/Pull ~ 1.0 (0.8 - 1.2), Upper/Lower ~ 1.2 - 1.5
            var score = 100
            val ppDiff = kotlin.math.abs(pushPullRatio - 1.0f)
            score -= (ppDiff * 25).toInt()
            val ulDiff = kotlin.math.abs(upperLowerRatio - 1.3f)
            score -= (ulDiff * 20).toInt()
            return score.coerceIn(40, 100)
        }
}
