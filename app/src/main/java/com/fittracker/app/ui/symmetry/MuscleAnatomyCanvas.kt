package com.fittracker.app.ui.symmetry

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.fittracker.app.ui.theme.*

@Composable
fun MuscleAnatomyCanvas(
    viewMode: BodyViewMode,
    muscles: List<MuscleData>,
    selectedMuscleId: String?,
    onSelectMuscle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val muscleMap = muscles.associateBy { it.id }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(380.dp)
            .pointerInput(viewMode) {
                detectTapGestures { offset ->
                    val w = size.width
                    val h = size.height
                    val normX = offset.x / w
                    val normY = offset.y / h

                    val clickedId = findMuscleAtPosition(viewMode, normX, normY)
                    if (clickedId != null) {
                        onSelectMuscle(clickedId)
                    }
                }
            }
    ) {
        val w = size.width
        val h = size.height
        val cx = w / 2f

        // Silueta base suave
        drawBodyContour(cx, h)

        if (viewMode == BodyViewMode.ANTERIOR) {
            drawAnteriorMuscles(cx, h, muscleMap, selectedMuscleId)
        } else {
            drawPosteriorMuscles(cx, h, muscleMap, selectedMuscleId)
        }
    }
}

private fun DrawScope.drawBodyContour(cx: Float, h: Float) {
    // Cabeza
    drawCircle(
        color = DarkSurfaceElevated,
        radius = h * 0.055f,
        center = Offset(cx, h * 0.075f)
    )
    drawCircle(
        color = DarkCardBorder,
        radius = h * 0.055f,
        center = Offset(cx, h * 0.075f),
        style = Stroke(width = 1.5f)
    )

    // Cuello
    drawRect(
        color = DarkSurfaceElevated,
        topLeft = Offset(cx - h * 0.02f, h * 0.12f),
        size = Size(h * 0.04f, h * 0.035f)
    )
}

private fun DrawScope.drawAnteriorMuscles(
    cx: Float,
    h: Float,
    muscles: Map<String, MuscleData>,
    selectedId: String?
) {
    // 1. Hombros / Deltoides Frontales (izq y der)
    val shoulderColor = muscles["shoulders"]?.status?.color ?: DarkSurfaceVariant
    val shoulderSelected = selectedId == "shoulders"
    drawMuscleCapsule(cx - h * 0.12f, h * 0.155f, h * 0.045f, h * 0.065f, shoulderColor, shoulderSelected)
    drawMuscleCapsule(cx + h * 0.075f, h * 0.155f, h * 0.045f, h * 0.065f, shoulderColor, shoulderSelected)

    // 2. Pectorales (Pecho)
    val chestColor = muscles["chest"]?.status?.color ?: DarkSurfaceVariant
    val chestSelected = selectedId == "chest"
    drawMuscleCapsule(cx - h * 0.072f, h * 0.165f, h * 0.065f, h * 0.08f, chestColor, chestSelected)
    drawMuscleCapsule(cx + h * 0.007f, h * 0.165f, h * 0.065f, h * 0.08f, chestColor, chestSelected)

    // 3. Bíceps (izq y der)
    val bicepsColor = muscles["biceps"]?.status?.color ?: DarkSurfaceVariant
    val bicepsSelected = selectedId == "biceps"
    drawMuscleCapsule(cx - h * 0.145f, h * 0.23f, h * 0.038f, h * 0.085f, bicepsColor, bicepsSelected)
    drawMuscleCapsule(cx + h * 0.107f, h * 0.23f, h * 0.038f, h * 0.085f, bicepsColor, bicepsSelected)

    // 4. Antebrazos (izq y der)
    val forearmColor = muscles["forearms"]?.status?.color ?: DarkSurfaceVariant
    val forearmSelected = selectedId == "forearms"
    drawMuscleCapsule(cx - h * 0.165f, h * 0.33f, h * 0.032f, h * 0.1f, forearmColor, forearmSelected)
    drawMuscleCapsule(cx + h * 0.133f, h * 0.33f, h * 0.032f, h * 0.1f, forearmColor, forearmSelected)

    // 5. Abdominales / Core
    val coreColor = muscles["core"]?.status?.color ?: DarkSurfaceVariant
    val coreSelected = selectedId == "core"
    for (i in 0..2) {
        val yOffset = h * (0.26f + i * 0.042f)
        drawMuscleCapsule(cx - h * 0.048f, yOffset, h * 0.043f, h * 0.035f, coreColor, coreSelected)
        drawMuscleCapsule(cx + h * 0.005f, yOffset, h * 0.043f, h * 0.035f, coreColor, coreSelected)
    }

    // 6. Cuádriceps (izq y der)
    val quadsColor = muscles["quads"]?.status?.color ?: DarkSurfaceVariant
    val quadsSelected = selectedId == "quads"
    drawMuscleCapsule(cx - h * 0.082f, h * 0.44f, h * 0.075f, h * 0.22f, quadsColor, quadsSelected)
    drawMuscleCapsule(cx + h * 0.007f, h * 0.44f, h * 0.075f, h * 0.22f, quadsColor, quadsSelected)

    // 7. Gemelos / Pantorrillas frontales
    val calvesColor = muscles["calves"]?.status?.color ?: DarkSurfaceVariant
    val calvesSelected = selectedId == "calves"
    drawMuscleCapsule(cx - h * 0.072f, h * 0.70f, h * 0.055f, h * 0.18f, calvesColor, calvesSelected)
    drawMuscleCapsule(cx + h * 0.017f, h * 0.70f, h * 0.055f, h * 0.18f, calvesColor, calvesSelected)
}

private fun DrawScope.drawPosteriorMuscles(
    cx: Float,
    h: Float,
    muscles: Map<String, MuscleData>,
    selectedId: String?
) {
    // 1. Trapecios y Espalda Alta
    val backColor = muscles["back"]?.status?.color ?: DarkSurfaceVariant
    val backSelected = selectedId == "back"
    drawMuscleCapsule(cx - h * 0.06f, h * 0.14f, h * 0.12f, h * 0.065f, backColor, backSelected)

    // 2. Deltoides Posteriores (Hombros posteriores)
    val shoulderColor = muscles["shoulders"]?.status?.color ?: DarkSurfaceVariant
    val shoulderSelected = selectedId == "shoulders"
    drawMuscleCapsule(cx - h * 0.125f, h * 0.155f, h * 0.045f, h * 0.065f, shoulderColor, shoulderSelected)
    drawMuscleCapsule(cx + h * 0.08f, h * 0.155f, h * 0.045f, h * 0.065f, shoulderColor, shoulderSelected)

    // 3. Tríceps (izq y der)
    val tricepsColor = muscles["triceps"]?.status?.color ?: DarkSurfaceVariant
    val tricepsSelected = selectedId == "triceps"
    drawMuscleCapsule(cx - h * 0.145f, h * 0.225f, h * 0.038f, h * 0.09f, tricepsColor, tricepsSelected)
    drawMuscleCapsule(cx + h * 0.107f, h * 0.225f, h * 0.038f, h * 0.09f, tricepsColor, tricepsSelected)

    // 4. Dorsales (Lats)
    drawMuscleCapsule(cx - h * 0.085f, h * 0.21f, h * 0.075f, h * 0.13f, backColor, backSelected)
    drawMuscleCapsule(cx + h * 0.01f, h * 0.21f, h * 0.075f, h * 0.13f, backColor, backSelected)

    // 5. Zona Lumbar / Espalda Baja
    drawMuscleCapsule(cx - h * 0.045f, h * 0.35f, h * 0.09f, h * 0.055f, backColor, backSelected)

    // 6. Glúteos (izq y der)
    val glutesColor = muscles["glutes"]?.status?.color ?: DarkSurfaceVariant
    val glutesSelected = selectedId == "glutes"
    drawMuscleCapsule(cx - h * 0.082f, h * 0.415f, h * 0.078f, h * 0.09f, glutesColor, glutesSelected)
    drawMuscleCapsule(cx + h * 0.004f, h * 0.415f, h * 0.078f, h * 0.09f, glutesColor, glutesSelected)

    // 7. Isquiosurales / Femorales
    val hamstringsColor = muscles["hamstrings"]?.status?.color ?: DarkSurfaceVariant
    val hamstringsSelected = selectedId == "hamstrings"
    drawMuscleCapsule(cx - h * 0.08f, h * 0.52f, h * 0.072f, h * 0.155f, hamstringsColor, hamstringsSelected)
    drawMuscleCapsule(cx + h * 0.008f, h * 0.52f, h * 0.072f, h * 0.155f, hamstringsColor, hamstringsSelected)

    // 8. Gemelos / Pantorrillas (Posterior)
    val calvesColor = muscles["calves"]?.status?.color ?: DarkSurfaceVariant
    val calvesSelected = selectedId == "calves"
    drawMuscleCapsule(cx - h * 0.075f, h * 0.70f, h * 0.06f, h * 0.17f, calvesColor, calvesSelected)
    drawMuscleCapsule(cx + h * 0.015f, h * 0.70f, h * 0.06f, h * 0.17f, calvesColor, calvesSelected)
}

private fun DrawScope.drawMuscleCapsule(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    color: Color,
    isSelected: Boolean
) {
    val cornerRadius = CornerRadius(14f, 14f)
    drawRoundRect(
        color = color.copy(alpha = if (isSelected) 0.95f else 0.82f),
        topLeft = Offset(x, y),
        size = Size(width, height),
        cornerRadius = cornerRadius
    )

    // Resplandor o borde sutil
    val strokeColor = if (isSelected) Color.White else color.copy(alpha = 0.5f)
    val strokeWidth = if (isSelected) 2.5f else 1f
    drawRoundRect(
        color = strokeColor,
        topLeft = Offset(x, y),
        size = Size(width, height),
        cornerRadius = cornerRadius,
        style = Stroke(width = strokeWidth)
    )
}

private fun findMuscleAtPosition(viewMode: BodyViewMode, normX: Float, normY: Float): String? {
    return if (viewMode == BodyViewMode.ANTERIOR) {
        when {
            normY in 0.14f..0.22f && (normX < 0.38f || normX > 0.62f) -> "shoulders"
            normY in 0.16f..0.25f && normX in 0.35f..0.65f -> "chest"
            normY in 0.22f..0.33f && (normX < 0.38f || normX > 0.62f) -> "biceps"
            normY in 0.33f..0.45f && (normX < 0.35f || normX > 0.65f) -> "forearms"
            normY in 0.25f..0.42f && normX in 0.38f..0.62f -> "core"
            normY in 0.43f..0.68f && normX in 0.32f..0.68f -> "quads"
            normY in 0.69f..0.92f && normX in 0.32f..0.68f -> "calves"
            else -> null
        }
    } else {
        when {
            normY in 0.14f..0.35f && normX in 0.34f..0.66f -> "back"
            normY in 0.14f..0.22f && (normX < 0.38f || normX > 0.62f) -> "shoulders"
            normY in 0.22f..0.33f && (normX < 0.38f || normX > 0.62f) -> "triceps"
            normY in 0.40f..0.51f && normX in 0.32f..0.68f -> "glutes"
            normY in 0.51f..0.68f && normX in 0.32f..0.68f -> "hamstrings"
            normY in 0.69f..0.92f && normX in 0.32f..0.68f -> "calves"
            else -> null
        }
    }
}
