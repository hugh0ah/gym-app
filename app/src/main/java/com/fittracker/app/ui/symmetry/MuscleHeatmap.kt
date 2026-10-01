package com.fittracker.app.ui.symmetry

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.fittracker.app.ui.theme.*

/**
 * MuscleHeatmap
 *
 * Componente visual premium estilo "Symmetry" y "RP Hypertrophy".
 * Renderiza un modelo anatómico humano vectorial (Frontal y Dorsal) basado en curvas
 * SVG/Path orgánicas con codificación cromática de fatiga e intensidad de volumen:
 * - Gris / Carbón (#263248): Sin entrenar / Fuera de foco (< MEV)
 * - Verde Neón (NeonMint): Volumen Mínimo Efectivo (MEV)
 * - Cian Eléctrico (ElectricCyan): Volumen Adaptativo Óptimo (MAV)
 * - Rojo Alerta (AlertRed): Límite de Recuperación / Máximo Volumen Recuperable (MRV)
 */
@Composable
fun MuscleHeatmap(
    viewMode: BodyViewMode,
    muscles: List<MuscleData>,
    selectedMuscleId: String?,
    onSelectMuscle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val muscleMap = remember(muscles) { muscles.associateBy { it.id } }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(400.dp)
    ) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(viewMode) {
                    detectTapGestures { offset ->
                        val normX = offset.x / size.width
                        val normY = offset.y / size.height
                        val clickedId = detectMuscleTap(viewMode, normX, normY)
                        if (clickedId != null) {
                            onSelectMuscle(clickedId)
                        }
                    }
                }
        ) {
            val w = size.width
            val h = size.height
            val cx = w / 2f

            // 1. Dibujar silueta base humana
            drawHumanBaseSilhouette(cx, h)

            // 2. Dibujar grupos musculares vectoriales según vista
            if (viewMode == BodyViewMode.ANTERIOR) {
                drawAnteriorHeatmap(cx, h, muscleMap, selectedMuscleId)
            } else {
                drawPosteriorHeatmap(cx, h, muscleMap, selectedMuscleId)
            }
        }
    }
}

// ==========================================
// SILUETA ANATÓMICA BASE (CABEZA, CUELLO, ARTICULACIONES)
// ==========================================
private fun DrawScope.drawHumanBaseSilhouette(cx: Float, h: Float) {
    // Fondo sutil del cuerpo para dar volumen 3D
    val baseGhostColor = DarkSurfaceElevated.copy(alpha = 0.6f)
    val contourStroke = DarkCardBorder.copy(alpha = 0.5f)

    // Cabeza simétrica
    val headCenter = Offset(cx, h * 0.065f)
    val headRadiusX = h * 0.038f
    val headRadiusY = h * 0.048f

    val headPath = Path().apply {
        addOval(Rect(headCenter.x - headRadiusX, headCenter.y - headRadiusY, headCenter.x + headRadiusX, headCenter.y + headRadiusY))
    }
    drawPath(headPath, color = baseGhostColor)
    drawPath(headPath, color = contourStroke, style = Stroke(width = 1.2f))

    // Cuello
    val neckPath = Path().apply {
        moveTo(cx - h * 0.018f, h * 0.108f)
        lineTo(cx + h * 0.018f, h * 0.108f)
        lineTo(cx + h * 0.024f, h * 0.138f)
        lineTo(cx - h * 0.024f, h * 0.138f)
        close()
    }
    drawPath(neckPath, color = baseGhostColor)
    drawPath(neckPath, color = contourStroke, style = Stroke(width = 1f))
}

// ==========================================
// VISTA ANTERIOR (FRONTAL)
// ==========================================
private fun DrawScope.drawAnteriorHeatmap(
    cx: Float,
    h: Float,
    muscles: Map<String, MuscleData>,
    selectedId: String?
) {
    // 1. HOMBROS (Deltoides Anteriores y Laterales)
    val shoulderColor = muscles["shoulders"]?.status?.color ?: Color(0xFF263248)
    val shoulderSelected = selectedId == "shoulders"
    drawDeltoidsAnterior(cx, h, shoulderColor, shoulderSelected)

    // 2. PECTORALES (Superior / Clavicular y Medio-Inferior / Esternal)
    val chestColor = muscles["chest"]?.status?.color ?: Color(0xFF263248)
    val chestSelected = selectedId == "chest"
    drawChestPectorals(cx, h, chestColor, chestSelected)

    // 3. BÍCEPS (Bíceps Braquial)
    val bicepsColor = muscles["biceps"]?.status?.color ?: Color(0xFF263248)
    val bicepsSelected = selectedId == "biceps"
    drawBicepsAnterior(cx, h, bicepsColor, bicepsSelected)

    // 4. ANTEBRAZOS (Braquiorradial y flexores)
    val forearmsColor = muscles["forearms"]?.status?.color ?: Color(0xFF263248)
    val forearmsSelected = selectedId == "forearms"
    drawForearmsAnterior(cx, h, forearmsColor, forearmsSelected)

    // 5. ABDOMEN / CORE (Recto abdominal 6-pack y oblicuos)
    val coreColor = muscles["core"]?.status?.color ?: Color(0xFF263248)
    val coreSelected = selectedId == "core"
    drawAbsAndCore(cx, h, coreColor, coreSelected)

    // 6. CUÁDRICEPS (Recto femoral, vasto externo y vasto interno)
    val quadsColor = muscles["quads"]?.status?.color ?: Color(0xFF263248)
    val quadsSelected = selectedId == "quads"
    drawQuadriceps(cx, h, quadsColor, quadsSelected)

    // 7. GEMELOS / PANTORRILLAS (Tibial anterior y gemelo frontal)
    val calvesColor = muscles["calves"]?.status?.color ?: Color(0xFF263248)
    val calvesSelected = selectedId == "calves"
    drawCalvesAnterior(cx, h, calvesColor, calvesSelected)
}

// ==========================================
// VISTA POSTERIOR (DORSAL)
// ==========================================
private fun DrawScope.drawPosteriorHeatmap(
    cx: Float,
    h: Float,
    muscles: Map<String, MuscleData>,
    selectedId: String?
) {
    // 1. ESPALDA: TRAPECIOS (Superior, Medio e Inferior)
    val backColor = muscles["back"]?.status?.color ?: Color(0xFF263248)
    val backSelected = selectedId == "back"
    drawTrapeziusPosterior(cx, h, backColor, backSelected)

    // 2. DELTOIDES POSTERIORES
    val shoulderColor = muscles["shoulders"]?.status?.color ?: Color(0xFF263248)
    val shoulderSelected = selectedId == "shoulders"
    drawRearDeltoids(cx, h, shoulderColor, shoulderSelected)

    // 3. TRÍCEPS (Cabeza larga y lateral en herradura)
    val tricepsColor = muscles["triceps"]?.status?.color ?: Color(0xFF263248)
    val tricepsSelected = selectedId == "triceps"
    drawTricepsPosterior(cx, h, tricepsColor, tricepsSelected)

    // 4. ESPALDA: DORSALES ANCHOS (Lats 'V-Taper')
    drawLatissimusDorsi(cx, h, backColor, backSelected)

    // 5. ESPALDA BAJA / LUMBARES (Erectores de la columna)
    drawLowerBack(cx, h, backColor, backSelected)

    // 6. GLÚTEOS (Glúteo Mayor y Medio)
    val glutesColor = muscles["glutes"]?.status?.color ?: Color(0xFF263248)
    val glutesSelected = selectedId == "glutes"
    drawGlutesPosterior(cx, h, glutesColor, glutesSelected)

    // 7. ISQUIOSURALES / FEMORALES (Bíceps Femoral y Semitendinoso)
    val hamstringsColor = muscles["hamstrings"]?.status?.color ?: Color(0xFF263248)
    val hamstringsSelected = selectedId == "hamstrings"
    drawHamstringsPosterior(cx, h, hamstringsColor, hamstringsSelected)

    // 8. GEMELOS POSTERIORES (Gastrocnemio lateral y medial)
    val calvesColor = muscles["calves"]?.status?.color ?: Color(0xFF263248)
    val calvesSelected = selectedId == "calves"
    drawCalvesPosterior(cx, h, calvesColor, calvesSelected)
}

// ==========================================
// FUNCIONES VECTORIALES ESPECÍFICAS
// ==========================================

// Pectorales con curva anatómica orgánica (clavicular + esternal)
private fun DrawScope.drawChestPectorals(cx: Float, h: Float, color: Color, isSelected: Boolean) {
    // Pectoral Izquierdo
    val leftPec = Path().apply {
        moveTo(cx - h * 0.006f, h * 0.150f)
        lineTo(cx - h * 0.070f, h * 0.145f)
        cubicTo(
            cx - h * 0.082f, h * 0.165f,
            cx - h * 0.080f, h * 0.205f,
            cx - h * 0.048f, h * 0.222f
        )
        cubicTo(
            cx - h * 0.025f, h * 0.226f,
            cx - h * 0.008f, h * 0.218f,
            cx - h * 0.006f, h * 0.205f
        )
        close()
    }

    // Pectoral Derecho
    val rightPec = Path().apply {
        moveTo(cx + h * 0.006f, h * 0.150f)
        lineTo(cx + h * 0.070f, h * 0.145f)
        cubicTo(
            cx + h * 0.082f, h * 0.165f,
            cx + h * 0.080f, h * 0.205f,
            cx + h * 0.048f, h * 0.222f
        )
        cubicTo(
            cx + h * 0.025f, h * 0.226f,
            cx + h * 0.008f, h * 0.218f,
            cx + h * 0.006f, h * 0.205f
        )
        close()
    }

    renderMusclePath(leftPec, color, isSelected)
    renderMusclePath(rightPec, color, isSelected)
}

// Deltoides Frontales (hombros anatómicos con curva de inserción)
private fun DrawScope.drawDeltoidsAnterior(cx: Float, h: Float, color: Color, isSelected: Boolean) {
    val leftDelt = Path().apply {
        moveTo(cx - h * 0.072f, h * 0.143f)
        cubicTo(
            cx - h * 0.115f, h * 0.145f,
            cx - h * 0.125f, h * 0.180f,
            cx - h * 0.098f, h * 0.212f
        )
        cubicTo(
            cx - h * 0.088f, h * 0.190f,
            cx - h * 0.078f, h * 0.165f,
            cx - h * 0.072f, h * 0.143f
        )
        close()
    }

    val rightDelt = Path().apply {
        moveTo(cx + h * 0.072f, h * 0.143f)
        cubicTo(
            cx + h * 0.115f, h * 0.145f,
            cx + h * 0.125f, h * 0.180f,
            cx + h * 0.098f, h * 0.212f
        )
        cubicTo(
            cx + h * 0.088f, h * 0.190f,
            cx + h * 0.078f, h * 0.165f,
            cx + h * 0.072f, h * 0.143f
        )
        close()
    }

    renderMusclePath(leftDelt, color, isSelected)
    renderMusclePath(rightDelt, color, isSelected)
}

// Bíceps Frontales
private fun DrawScope.drawBicepsAnterior(cx: Float, h: Float, color: Color, isSelected: Boolean) {
    val leftBicep = Path().apply {
        moveTo(cx - h * 0.100f, h * 0.218f)
        cubicTo(
            cx - h * 0.138f, h * 0.245f,
            cx - h * 0.132f, h * 0.285f,
            cx - h * 0.104f, h * 0.305f
        )
        cubicTo(
            cx - h * 0.090f, h * 0.280f,
            cx - h * 0.088f, h * 0.240f,
            cx - h * 0.100f, h * 0.218f
        )
        close()
    }

    val rightBicep = Path().apply {
        moveTo(cx + h * 0.100f, h * 0.218f)
        cubicTo(
            cx + h * 0.138f, h * 0.245f,
            cx + h * 0.132f, h * 0.285f,
            cx + h * 0.104f, h * 0.305f
        )
        cubicTo(
            cx + h * 0.090f, h * 0.280f,
            cx + h * 0.088f, h * 0.240f,
            cx + h * 0.100f, h * 0.218f
        )
        close()
    }

    renderMusclePath(leftBicep, color, isSelected)
    renderMusclePath(rightBicep, color, isSelected)
}

// Antebrazos Frontales
private fun DrawScope.drawForearmsAnterior(cx: Float, h: Float, color: Color, isSelected: Boolean) {
    val leftForearm = Path().apply {
        moveTo(cx - h * 0.105f, h * 0.312f)
        cubicTo(
            cx - h * 0.145f, h * 0.340f,
            cx - h * 0.140f, h * 0.395f,
            cx - h * 0.122f, h * 0.420f
        )
        lineTo(cx - h * 0.102f, h * 0.418f)
        cubicTo(
            cx - h * 0.095f, h * 0.370f,
            cx - h * 0.092f, h * 0.330f,
            cx - h * 0.105f, h * 0.312f
        )
        close()
    }

    val rightForearm = Path().apply {
        moveTo(cx + h * 0.105f, h * 0.312f)
        cubicTo(
            cx + h * 0.145f, h * 0.340f,
            cx + h * 0.140f, h * 0.395f,
            cx + h * 0.122f, h * 0.420f
        )
        lineTo(cx + h * 0.102f, h * 0.418f)
        cubicTo(
            cx + h * 0.095f, h * 0.370f,
            cx + h * 0.092f, h * 0.330f,
            cx + h * 0.105f, h * 0.312f
        )
        close()
    }

    renderMusclePath(leftForearm, color, isSelected)
    renderMusclePath(rightForearm, color, isSelected)
}

// Abdominales (Recto abdominal 6-pack + Oblicuos)
private fun DrawScope.drawAbsAndCore(cx: Float, h: Float, color: Color, isSelected: Boolean) {
    // 3 pares de tabletas abdominales
    for (row in 0..2) {
        val yTop = h * (0.235f + row * 0.045f)
        val hBox = h * 0.038f
        val wBox = h * 0.034f

        val leftAb = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(cx - h * 0.038f, yTop, cx - h * 0.005f, yTop + hBox),
                    cornerRadius = CornerRadius(8f, 8f)
                )
            )
        }
        val rightAb = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(cx + h * 0.005f, yTop, cx + h * 0.038f, yTop + hBox),
                    cornerRadius = CornerRadius(8f, 8f)
                )
            )
        }

        renderMusclePath(leftAb, color, isSelected)
        renderMusclePath(rightAb, color, isSelected)
    }

    // Oblicuos laterales
    val leftOblique = Path().apply {
        moveTo(cx - h * 0.045f, h * 0.240f)
        lineTo(cx - h * 0.072f, h * 0.250f)
        cubicTo(cx - h * 0.075f, h * 0.310f, cx - h * 0.060f, h * 0.360f, cx - h * 0.045f, h * 0.370f)
        lineTo(cx - h * 0.042f, h * 0.320f)
        close()
    }

    val rightOblique = Path().apply {
        moveTo(cx + h * 0.045f, h * 0.240f)
        lineTo(cx + h * 0.072f, h * 0.250f)
        cubicTo(cx + h * 0.075f, h * 0.310f, cx + h * 0.060f, h * 0.360f, cx + h * 0.045f, h * 0.370f)
        lineTo(cx + h * 0.042f, h * 0.320f)
        close()
    }

    renderMusclePath(leftOblique, color.copy(alpha = 0.9f), isSelected)
    renderMusclePath(rightOblique, color.copy(alpha = 0.9f), isSelected)
}

// Cuádriceps (Vasto externo, vasto medial y recto femoral)
private fun DrawScope.drawQuadriceps(cx: Float, h: Float, color: Color, isSelected: Boolean) {
    // Muslo Izquierdo
    val leftQuad = Path().apply {
        moveTo(cx - h * 0.010f, h * 0.405f)
        lineTo(cx - h * 0.068f, h * 0.410f)
        cubicTo(
            cx - h * 0.088f, h * 0.470f,
            cx - h * 0.078f, h * 0.570f,
            cx - h * 0.054f, h * 0.620f
        )
        // Vasto interno encima de la rodilla
        cubicTo(
            cx - h * 0.038f, h * 0.628f,
            cx - h * 0.024f, h * 0.605f,
            cx - h * 0.012f, h * 0.520f
        )
        close()
    }

    // Muslo Derecho
    val rightQuad = Path().apply {
        moveTo(cx + h * 0.010f, h * 0.405f)
        lineTo(cx + h * 0.068f, h * 0.410f)
        cubicTo(
            cx + h * 0.088f, h * 0.470f,
            cx + h * 0.078f, h * 0.570f,
            cx + h * 0.054f, h * 0.620f
        )
        cubicTo(
            cx + h * 0.038f, h * 0.628f,
            cx + h * 0.024f, h * 0.605f,
            cx + h * 0.012f, h * 0.520f
        )
        close()
    }

    renderMusclePath(leftQuad, color, isSelected)
    renderMusclePath(rightQuad, color, isSelected)
}

// Gemelos / Pantorrillas Frontales
private fun DrawScope.drawCalvesAnterior(cx: Float, h: Float, color: Color, isSelected: Boolean) {
    val leftCalf = Path().apply {
        moveTo(cx - h * 0.025f, h * 0.645f)
        cubicTo(
            cx - h * 0.062f, h * 0.680f,
            cx - h * 0.065f, h * 0.745f,
            cx - h * 0.040f, h * 0.810f
        )
        lineTo(cx - h * 0.028f, h * 0.810f)
        cubicTo(
            cx - h * 0.020f, h * 0.750f,
            cx - h * 0.018f, h * 0.685f,
            cx - h * 0.025f, h * 0.645f
        )
        close()
    }

    val rightCalf = Path().apply {
        moveTo(cx + h * 0.025f, h * 0.645f)
        cubicTo(
            cx + h * 0.062f, h * 0.680f,
            cx + h * 0.065f, h * 0.745f,
            cx + h * 0.040f, h * 0.810f
        )
        lineTo(cx + h * 0.028f, h * 0.810f)
        cubicTo(
            cx + h * 0.020f, h * 0.750f,
            cx + h * 0.018f, h * 0.685f,
            cx + h * 0.025f, h * 0.645f
        )
        close()
    }

    renderMusclePath(leftCalf, color, isSelected)
    renderMusclePath(rightCalf, color, isSelected)
}

// ==========================================
// VISTAS POSTERIORES
// ==========================================

// Trapecios (forma de diamante/cometa desde el cuello hasta mitad de la espalda)
private fun DrawScope.drawTrapeziusPosterior(cx: Float, h: Float, color: Color, isSelected: Boolean) {
    val trap = Path().apply {
        moveTo(cx, h * 0.118f)
        lineTo(cx + h * 0.068f, h * 0.145f)
        lineTo(cx, h * 0.228f)
        lineTo(cx - h * 0.068f, h * 0.145f)
        close()
    }
    renderMusclePath(trap, color, isSelected)
}

// Deltoides Posteriores
private fun DrawScope.drawRearDeltoids(cx: Float, h: Float, color: Color, isSelected: Boolean) {
    val leftRearDelt = Path().apply {
        moveTo(cx - h * 0.070f, h * 0.144f)
        cubicTo(
            cx - h * 0.118f, h * 0.150f,
            cx - h * 0.125f, h * 0.185f,
            cx - h * 0.096f, h * 0.215f
        )
        cubicTo(
            cx - h * 0.088f, h * 0.190f,
            cx - h * 0.078f, h * 0.165f,
            cx - h * 0.070f, h * 0.144f
        )
        close()
    }

    val rightRearDelt = Path().apply {
        moveTo(cx + h * 0.070f, h * 0.144f)
        cubicTo(
            cx + h * 0.118f, h * 0.150f,
            cx + h * 0.125f, h * 0.185f,
            cx + h * 0.096f, h * 0.215f
        )
        cubicTo(
            cx + h * 0.088f, h * 0.190f,
            cx + h * 0.078f, h * 0.165f,
            cx + h * 0.070f, h * 0.144f
        )
        close()
    }

    renderMusclePath(leftRearDelt, color, isSelected)
    renderMusclePath(rightRearDelt, color, isSelected)
}

// Tríceps posteriores (herradura)
private fun DrawScope.drawTricepsPosterior(cx: Float, h: Float, color: Color, isSelected: Boolean) {
    val leftTricep = Path().apply {
        moveTo(cx - h * 0.098f, h * 0.218f)
        cubicTo(
            cx - h * 0.138f, h * 0.235f,
            cx - h * 0.132f, h * 0.285f,
            cx - h * 0.104f, h * 0.305f
        )
        lineTo(cx - h * 0.090f, h * 0.285f)
        close()
    }

    val rightTricep = Path().apply {
        moveTo(cx + h * 0.098f, h * 0.218f)
        cubicTo(
            cx + h * 0.138f, h * 0.235f,
            cx + h * 0.132f, h * 0.285f,
            cx + h * 0.104f, h * 0.305f
        )
        lineTo(cx + h * 0.090f, h * 0.285f)
        close()
    }

    renderMusclePath(leftTricep, color, isSelected)
    renderMusclePath(rightTricep, color, isSelected)
}

// Dorsales anchos (Lats V-Taper)
private fun DrawScope.drawLatissimusDorsi(cx: Float, h: Float, color: Color, isSelected: Boolean) {
    val leftLat = Path().apply {
        moveTo(cx - h * 0.008f, h * 0.232f)
        lineTo(cx - h * 0.065f, h * 0.185f)
        cubicTo(
            cx - h * 0.088f, h * 0.225f,
            cx - h * 0.080f, h * 0.295f,
            cx - h * 0.038f, h * 0.335f
        )
        lineTo(cx - h * 0.008f, h * 0.315f)
        close()
    }

    val rightLat = Path().apply {
        moveTo(cx + h * 0.008f, h * 0.232f)
        lineTo(cx + h * 0.065f, h * 0.185f)
        cubicTo(
            cx + h * 0.088f, h * 0.225f,
            cx + h * 0.080f, h * 0.295f,
            cx + h * 0.038f, h * 0.335f
        )
        lineTo(cx + h * 0.008f, h * 0.315f)
        close()
    }

    renderMusclePath(leftLat, color, isSelected)
    renderMusclePath(rightLat, color, isSelected)
}

// Espalda baja / Lumbar
private fun DrawScope.drawLowerBack(cx: Float, h: Float, color: Color, isSelected: Boolean) {
    val lowerBack = Path().apply {
        moveTo(cx - h * 0.034f, h * 0.335f)
        lineTo(cx + h * 0.034f, h * 0.335f)
        lineTo(cx + h * 0.028f, h * 0.385f)
        lineTo(cx - h * 0.028f, h * 0.385f)
        close()
    }
    renderMusclePath(lowerBack, color.copy(alpha = 0.88f), isSelected)
}

// Glúteos
private fun DrawScope.drawGlutesPosterior(cx: Float, h: Float, color: Color, isSelected: Boolean) {
    val leftGlute = Path().apply {
        moveTo(cx - h * 0.005f, h * 0.390f)
        lineTo(cx - h * 0.065f, h * 0.395f)
        cubicTo(
            cx - h * 0.082f, h * 0.440f,
            cx - h * 0.075f, h * 0.485f,
            cx - h * 0.035f, h * 0.505f
        )
        lineTo(cx - h * 0.005f, h * 0.480f)
        close()
    }

    val rightGlute = Path().apply {
        moveTo(cx + h * 0.005f, h * 0.390f)
        lineTo(cx + h * 0.065f, h * 0.395f)
        cubicTo(
            cx + h * 0.082f, h * 0.440f,
            cx + h * 0.075f, h * 0.485f,
            cx + h * 0.035f, h * 0.505f
        )
        lineTo(cx + h * 0.005f, h * 0.480f)
        close()
    }

    renderMusclePath(leftGlute, color, isSelected)
    renderMusclePath(rightGlute, color, isSelected)
}

// Isquiosurales / Femorales
private fun DrawScope.drawHamstringsPosterior(cx: Float, h: Float, color: Color, isSelected: Boolean) {
    val leftHamstring = Path().apply {
        moveTo(cx - h * 0.010f, h * 0.490f)
        lineTo(cx - h * 0.065f, h * 0.510f)
        cubicTo(
            cx - h * 0.072f, h * 0.560f,
            cx - h * 0.065f, h * 0.615f,
            cx - h * 0.045f, h * 0.635f
        )
        lineTo(cx - h * 0.018f, h * 0.615f)
        close()
    }

    val rightHamstring = Path().apply {
        moveTo(cx + h * 0.010f, h * 0.490f)
        lineTo(cx + h * 0.065f, h * 0.510f)
        cubicTo(
            cx + h * 0.072f, h * 0.560f,
            cx + h * 0.065f, h * 0.615f,
            cx + h * 0.045f, h * 0.635f
        )
        lineTo(cx + h * 0.018f, h * 0.615f)
        close()
    }

    renderMusclePath(leftHamstring, color, isSelected)
    renderMusclePath(rightHamstring, color, isSelected)
}

// Gemelos Posteriores (gemelo lateral y medial)
private fun DrawScope.drawCalvesPosterior(cx: Float, h: Float, color: Color, isSelected: Boolean) {
    val leftCalf = Path().apply {
        moveTo(cx - h * 0.024f, h * 0.648f)
        cubicTo(
            cx - h * 0.068f, h * 0.680f,
            cx - h * 0.065f, h * 0.745f,
            cx - h * 0.040f, h * 0.810f
        )
        lineTo(cx - h * 0.026f, h * 0.810f)
        cubicTo(
            cx - h * 0.018f, h * 0.750f,
            cx - h * 0.016f, h * 0.685f,
            cx - h * 0.024f, h * 0.648f
        )
        close()
    }

    val rightCalf = Path().apply {
        moveTo(cx + h * 0.024f, h * 0.648f)
        cubicTo(
            cx + h * 0.068f, h * 0.680f,
            cx + h * 0.065f, h * 0.745f,
            cx + h * 0.040f, h * 0.810f
        )
        lineTo(cx + h * 0.026f, h * 0.810f)
        cubicTo(
            cx + h * 0.020f, h * 0.750f,
            cx + h * 0.016f, h * 0.685f,
            cx + h * 0.024f, h * 0.648f
        )
        close()
    }

    renderMusclePath(leftCalf, color, isSelected)
    renderMusclePath(rightCalf, color, isSelected)
}

// Renderizado con sombreado, gradiente sutil y halo cuando está seleccionado
private fun DrawScope.renderMusclePath(path: Path, baseColor: Color, isSelected: Boolean) {
    val alpha = if (isSelected) 1.0f else 0.88f
    val fillCol = baseColor.copy(alpha = alpha)

    // Relleno principal
    drawPath(path, color = fillCol)

    // Si está seleccionado, dibujar resplandor exterior y contorno neón
    if (isSelected) {
        drawPath(
            path = path,
            color = Color.White,
            style = Stroke(
                width = 2.4f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
        drawPath(
            path = path,
            color = baseColor.copy(alpha = 0.5f),
            style = Stroke(
                width = 4.8f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    } else {
        // Borde fino de definición anatómica
        drawPath(
            path = path,
            color = DarkCardBorder.copy(alpha = 0.65f),
            style = Stroke(
                width = 1.0f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

// Detección de toques sobre los músculos normalizados (0f..1f)
private fun detectMuscleTap(viewMode: BodyViewMode, normX: Float, normY: Float): String? {
    return if (viewMode == BodyViewMode.ANTERIOR) {
        when {
            normY in 0.13f..0.21f && (normX < 0.38f || normX > 0.62f) -> "shoulders"
            normY in 0.14f..0.23f && normX in 0.35f..0.65f -> "chest"
            normY in 0.21f..0.31f && (normX < 0.38f || normX > 0.62f) -> "biceps"
            normY in 0.31f..0.43f && (normX < 0.36f || normX > 0.64f) -> "forearms"
            normY in 0.23f..0.39f && normX in 0.38f..0.62f -> "core"
            normY in 0.40f..0.63f && normX in 0.32f..0.68f -> "quads"
            normY in 0.64f..0.85f && normX in 0.30f..0.70f -> "calves"
            else -> null
        }
    } else {
        when {
            normY in 0.11f..0.23f && normX in 0.36f..0.64f -> "back" // Trapecios
            normY in 0.23f..0.38f && normX in 0.32f..0.68f -> "back" // Dorsales y lumbar
            normY in 0.13f..0.21f && (normX < 0.38f || normX > 0.62f) -> "shoulders"
            normY in 0.21f..0.31f && (normX < 0.38f || normX > 0.62f) -> "triceps"
            normY in 0.38f..0.49f && normX in 0.32f..0.68f -> "glutes"
            normY in 0.49f..0.64f && normX in 0.32f..0.68f -> "hamstrings"
            normY in 0.64f..0.85f && normX in 0.30f..0.70f -> "calves"
            else -> null
        }
    }
}
