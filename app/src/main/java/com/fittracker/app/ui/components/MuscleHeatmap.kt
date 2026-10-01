package com.fittracker.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fittracker.app.ui.symmetry.BodyViewMode
import com.fittracker.app.ui.theme.*

/**
 * Representa una región anatómica muscular basada en coordenadas vectoriales tipo SVG.
 */
data class SvgMuscleRegion(
    val id: String,
    val name: String,
    val group: String,
    val pathData: String,
    val isFront: Boolean,
    val bounds: Rect // Coordenadas normalizadas aproximadas [0..1] para detección táctil rápida
)

/**
 * Componente MuscleHeatmap:
 * Visualizador anatómico de alta precisión basado en vectores SVG para Jetpack Compose.
 * Colorea cada grupo muscular según la intensidad del volumen de entrenamiento semanal:
 * - Gris carbón: Sin entrenar (<6 series)
 * - Verde Neón: MEV - Volumen Mínimo Efectivo (6-9 series)
 * - Cian Eléctrico: MAV - Hipertrofia Óptima (10-18 series)
 * - Rojo Alerta: MRV - Límite de Recuperación (>18 series)
 */
@Composable
fun MuscleHeatmap(
    modifier: Modifier = Modifier,
    viewMode: BodyViewMode = BodyViewMode.ANTERIOR,
    muscleVolumes: Map<String, Int> = emptyMap(),
    selectedMuscleId: String? = null,
    onMuscleSelected: ((String) -> Unit)? = null,
    showControls: Boolean = true
) {
    var currentMode by remember(viewMode) { mutableStateOf(viewMode) }

    // Pre-parsear los paths SVG escalables
    val regions = remember { SvgAnatomyData.getAllRegions() }
    val currentRegions = remember(currentMode) {
        regions.filter { it.isFront == (currentMode == BodyViewMode.ANTERIOR) }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurface)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(20.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (showControls) {
            // Selector de Vista Frontal / Dorsal
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = { currentMode = BodyViewMode.ANTERIOR },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentMode == BodyViewMode.ANTERIOR) NeonMint else Color.Transparent,
                        contentColor = if (currentMode == BodyViewMode.ANTERIOR) Color.Black else TextMuted
                    ),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Frontal", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = { currentMode = BodyViewMode.POSTERIOR },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentMode == BodyViewMode.POSTERIOR) ElectricCyan else Color.Transparent,
                        contentColor = if (currentMode == BodyViewMode.POSTERIOR) Color.Black else TextMuted
                    ),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Dorsal", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Canvas de Dibujo Anatómico SVG
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(currentRegions) {
                        detectTapGestures { offset ->
                            val normX = offset.x / size.width
                            val normY = offset.y / size.height

                            val clicked = currentRegions.find { r ->
                                normX in r.bounds.left..r.bounds.right &&
                                        normY in r.bounds.top..r.bounds.bottom
                            }
                            if (clicked != null) {
                                onMuscleSelected?.invoke(clicked.id)
                            }
                        }
                    }
            ) {
                val scaleX = size.width / 200f
                val scaleY = size.height / 400f

                // Dibujar silueta base del cuerpo humano (cabeza y cuello)
                drawHumanSkeletonBase(size.width, size.height, currentMode)

                // Dibujar cada región muscular SVG con su color de calor
                currentRegions.forEach { region ->
                    val sets = muscleVolumes[region.id] ?: 0
                    val isSelected = selectedMuscleId == region.id

                    val muscleColor = when {
                        sets <= 0 -> Color(0xFF1E2638)
                        sets < 6 -> Color(0xFF28364F)
                        sets <= 9 -> NeonMint
                        sets <= 18 -> ElectricCyan
                        else -> AlertRed
                    }

                    val path = PathParser().parsePathString(region.pathData).toPath()

                    // Transformar escala para encajar exactamente en el viewport
                    val scaledPath = Path().apply {
                        addPath(path)
                    }

                    // Relleno muscular
                    drawPath(
                        path = scaledPath,
                        color = if (isSelected) muscleColor else muscleColor.copy(alpha = 0.88f),
                        style = Fill
                    )

                    // Contorno refinado
                    drawPath(
                        path = scaledPath,
                        color = if (isSelected) Color.White else muscleColor.copy(alpha = 0.5f),
                        style = Stroke(
                            width = if (isSelected) 2.5f else 1.2f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }

        // Leyenda compacta de intensidades
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeatmapLegendItem(color = Color(0xFF28364F), label = "<6s")
            HeatmapLegendItem(color = NeonMint, label = "MEV (6-9)")
            HeatmapLegendItem(color = ElectricCyan, label = "MAV (10-18)")
            HeatmapLegendItem(color = AlertRed, label = "MRV (>18)")
        }
    }
}

@Composable
private fun HeatmapLegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(text = label, color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

private fun DrawScope.drawHumanSkeletonBase(w: Float, h: Float, mode: BodyViewMode) {
    val cx = w / 2f

    // Cabeza
    drawCircle(
        color = DarkSurfaceElevated,
        radius = h * 0.05f,
        center = Offset(cx, h * 0.065f)
    )
    drawCircle(
        color = DarkCardBorder,
        radius = h * 0.05f,
        center = Offset(cx, h * 0.065f),
        style = Stroke(width = 1.5f)
    )

    // Cuello
    drawRect(
        color = DarkSurfaceElevated,
        topLeft = Offset(cx - h * 0.018f, h * 0.11f),
        size = androidx.compose.ui.geometry.Size(h * 0.036f, h * 0.03f)
    )
}

/**
 * Catálogo completo de rutas vectoriales SVG anatómicas para el cuerpo humano frontal y dorsal.
 * Coordenadas normalizadas sobre una caja de 200x400.
 */
object SvgAnatomyData {
    fun getAllRegions(): List<SvgMuscleRegion> {
        return listOf(
            // ================= ANTERIOR (FRONTAL) =================
            // Pectorales (Pecho)
            SvgMuscleRegion(
                id = "chest",
                name = "Pectoral Izquierdo",
                group = "Pecho",
                pathData = "M 96,65 L 68,65 C 60,68 56,76 56,86 C 56,98 72,106 96,106 L 96,65 Z",
                isFront = true,
                bounds = Rect(0.28f, 0.16f, 0.49f, 0.27f)
            ),
            SvgMuscleRegion(
                id = "chest",
                name = "Pectoral Derecho",
                group = "Pecho",
                pathData = "M 104,65 L 132,65 C 140,68 144,76 144,86 C 144,98 128,106 104,106 L 104,65 Z",
                isFront = true,
                bounds = Rect(0.51f, 0.16f, 0.72f, 0.27f)
            ),

            // Hombros / Deltoides Frontales
            SvgMuscleRegion(
                id = "shoulders",
                name = "Deltoides Izquierdo",
                group = "Hombros",
                pathData = "M 54,64 C 44,66 38,76 38,88 C 38,98 44,106 50,110 L 54,92 Z",
                isFront = true,
                bounds = Rect(0.18f, 0.15f, 0.28f, 0.28f)
            ),
            SvgMuscleRegion(
                id = "shoulders",
                name = "Deltoides Derecho",
                group = "Hombros",
                pathData = "M 146,64 C 156,66 162,76 162,88 C 162,98 156,106 150,110 L 146,92 Z",
                isFront = true,
                bounds = Rect(0.72f, 0.15f, 0.82f, 0.28f)
            ),

            // Bíceps
            SvgMuscleRegion(
                id = "biceps",
                name = "Bíceps Izquierdo",
                group = "Brazos",
                pathData = "M 36,92 C 30,102 30,122 36,134 C 42,134 46,126 48,114 C 48,104 44,94 36,92 Z",
                isFront = true,
                bounds = Rect(0.14f, 0.23f, 0.24f, 0.34f)
            ),
            SvgMuscleRegion(
                id = "biceps",
                name = "Bíceps Derecho",
                group = "Brazos",
                pathData = "M 164,92 C 170,102 170,122 164,134 C 158,134 154,126 152,114 C 152,104 156,94 164,92 Z",
                isFront = true,
                bounds = Rect(0.76f, 0.23f, 0.86f, 0.34f)
            ),

            // Antebrazos
            SvgMuscleRegion(
                id = "forearms",
                name = "Antebrazo Izquierdo",
                group = "Antebrazos",
                pathData = "M 34,138 C 28,150 26,170 32,184 C 36,184 40,172 42,158 C 42,148 40,140 34,138 Z",
                isFront = true,
                bounds = Rect(0.12f, 0.34f, 0.22f, 0.47f)
            ),
            SvgMuscleRegion(
                id = "forearms",
                name = "Antebrazo Derecho",
                group = "Antebrazos",
                pathData = "M 166,138 C 172,150 174,170 168,184 C 164,184 160,172 158,158 C 158,148 160,140 166,138 Z",
                isFront = true,
                bounds = Rect(0.78f, 0.34f, 0.88f, 0.47f)
            ),

            // Core / Abdomen
            SvgMuscleRegion(
                id = "core",
                name = "Abdominales Superiores",
                group = "Core",
                pathData = "M 76,110 L 96,110 L 96,128 L 76,128 Z M 104,110 L 124,110 L 124,128 L 104,128 Z",
                isFront = true,
                bounds = Rect(0.38f, 0.27f, 0.62f, 0.33f)
            ),
            SvgMuscleRegion(
                id = "core",
                name = "Abdominales Medios",
                group = "Core",
                pathData = "M 78,132 L 96,132 L 96,150 L 78,150 Z M 104,132 L 122,132 L 122,150 L 104,150 Z",
                isFront = true,
                bounds = Rect(0.38f, 0.33f, 0.62f, 0.38f)
            ),
            SvgMuscleRegion(
                id = "core",
                name = "Abdominales Inferiores",
                group = "Core",
                pathData = "M 80,154 L 96,154 L 96,174 L 84,174 Z M 104,154 L 120,154 L 116,174 L 104,174 Z",
                isFront = true,
                bounds = Rect(0.40f, 0.38f, 0.60f, 0.44f)
            ),

            // Cuádriceps (Piernas Frontales)
            SvgMuscleRegion(
                id = "quads",
                name = "Cuádriceps Izquierdo",
                group = "Piernas",
                pathData = "M 66,184 C 58,210 56,248 64,276 C 74,282 86,276 92,260 C 94,236 94,204 90,184 Z",
                isFront = true,
                bounds = Rect(0.28f, 0.45f, 0.47f, 0.70f)
            ),
            SvgMuscleRegion(
                id = "quads",
                name = "Cuádriceps Derecho",
                group = "Piernas",
                pathData = "M 134,184 C 142,210 144,248 136,276 C 126,282 114,276 108,260 C 106,236 106,204 110,184 Z",
                isFront = true,
                bounds = Rect(0.53f, 0.45f, 0.72f, 0.70f)
            ),

            // Gemelos / Pantorrillas Frontales
            SvgMuscleRegion(
                id = "calves",
                name = "Gemelo Izquierdo",
                group = "Piernas",
                pathData = "M 66,290 C 58,310 60,342 68,366 C 74,366 82,354 84,332 C 86,312 80,296 66,290 Z",
                isFront = true,
                bounds = Rect(0.28f, 0.71f, 0.44f, 0.92f)
            ),
            SvgMuscleRegion(
                id = "calves",
                name = "Gemelo Derecho",
                group = "Piernas",
                pathData = "M 134,290 C 142,310 140,342 132,366 C 126,366 118,354 116,332 C 114,312 120,296 134,290 Z",
                isFront = true,
                bounds = Rect(0.56f, 0.71f, 0.72f, 0.92f)
            ),

            // ================= POSTERIOR (DORSAL) =================
            // Trapecios / Espalda Alta
            SvgMuscleRegion(
                id = "back",
                name = "Trapecios",
                group = "Espalda",
                pathData = "M 100,56 L 76,64 L 70,82 L 100,104 L 130,82 L 124,64 Z",
                isFront = false,
                bounds = Rect(0.35f, 0.14f, 0.65f, 0.26f)
            ),

            // Dorsales (Lats)
            SvgMuscleRegion(
                id = "back",
                name = "Dorsal Izquierdo",
                group = "Espalda",
                pathData = "M 68,86 C 54,98 52,126 62,148 C 76,148 84,136 88,118 L 88,96 Z",
                isFront = false,
                bounds = Rect(0.26f, 0.21f, 0.44f, 0.38f)
            ),
            SvgMuscleRegion(
                id = "back",
                name = "Dorsal Derecho",
                group = "Espalda",
                pathData = "M 132,86 C 146,98 148,126 138,148 C 124,148 116,136 112,118 L 112,96 Z",
                isFront = false,
                bounds = Rect(0.56f, 0.21f, 0.74f, 0.38f)
            ),

            // Deltoides Posteriores
            SvgMuscleRegion(
                id = "shoulders",
                name = "Deltoides Posterior Izquierdo",
                group = "Hombros",
                pathData = "M 52,64 C 42,66 38,76 38,88 C 38,98 44,106 50,110 L 52,86 Z",
                isFront = false,
                bounds = Rect(0.18f, 0.15f, 0.28f, 0.28f)
            ),
            SvgMuscleRegion(
                id = "shoulders",
                name = "Deltoides Posterior Derecho",
                group = "Hombros",
                pathData = "M 148,64 C 158,66 162,76 162,88 C 162,98 156,106 150,110 L 148,86 Z",
                isFront = false,
                bounds = Rect(0.72f, 0.15f, 0.82f, 0.28f)
            ),

            // Tríceps
            SvgMuscleRegion(
                id = "triceps",
                name = "Tríceps Izquierdo",
                group = "Brazos",
                pathData = "M 36,92 C 30,102 30,124 38,136 C 44,134 46,124 48,112 C 48,102 44,94 36,92 Z",
                isFront = false,
                bounds = Rect(0.14f, 0.23f, 0.24f, 0.34f)
            ),
            SvgMuscleRegion(
                id = "triceps",
                name = "Tríceps Derecho",
                group = "Brazos",
                pathData = "M 164,92 C 170,102 170,124 162,136 C 156,134 154,124 152,112 C 152,102 156,94 164,92 Z",
                isFront = false,
                bounds = Rect(0.76f, 0.23f, 0.86f, 0.34f)
            ),

            // Glúteos
            SvgMuscleRegion(
                id = "glutes",
                name = "Glúteo Izquierdo",
                group = "Glúteos",
                pathData = "M 98,174 C 74,174 64,188 64,208 C 64,224 82,228 98,218 Z",
                isFront = false,
                bounds = Rect(0.31f, 0.42f, 0.49f, 0.56f)
            ),
            SvgMuscleRegion(
                id = "glutes",
                name = "Glúteo Derecho",
                group = "Glúteos",
                pathData = "M 102,174 C 126,174 136,188 136,208 C 136,224 118,228 102,218 Z",
                isFront = false,
                bounds = Rect(0.51f, 0.42f, 0.69f, 0.56f)
            ),

            // Isquiosurales / Femorales
            SvgMuscleRegion(
                id = "hamstrings",
                name = "Isquiosural Izquierdo",
                group = "Piernas",
                pathData = "M 66,224 C 60,244 60,268 68,284 C 78,284 88,272 90,250 C 92,234 86,224 66,224 Z",
                isFront = false,
                bounds = Rect(0.29f, 0.56f, 0.46f, 0.71f)
            ),
            SvgMuscleRegion(
                id = "hamstrings",
                name = "Isquiosural Derecho",
                group = "Piernas",
                pathData = "M 134,224 C 140,244 140,268 132,284 C 122,284 112,272 110,250 C 108,234 114,224 134,224 Z",
                isFront = false,
                bounds = Rect(0.54f, 0.56f, 0.71f, 0.71f)
            ),

            // Gemelos Posteriores (Gastrocnemio)
            SvgMuscleRegion(
                id = "calves",
                name = "Pantorrilla Izquierda",
                group = "Piernas",
                pathData = "M 66,294 C 56,314 58,344 68,368 C 76,368 84,354 84,330 C 86,310 80,298 66,294 Z",
                isFront = false,
                bounds = Rect(0.28f, 0.72f, 0.44f, 0.93f)
            ),
            SvgMuscleRegion(
                id = "calves",
                name = "Pantorrilla Derecha",
                group = "Piernas",
                pathData = "M 134,294 C 144,314 142,344 132,368 C 124,368 116,354 116,330 C 114,310 120,298 134,294 Z",
                isFront = false,
                bounds = Rect(0.56f, 0.72f, 0.72f, 0.93f)
            )
        )
    }
}
