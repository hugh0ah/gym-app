package com.fittracker.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fittracker.app.ui.theme.*

@Composable
fun InteractiveMacroRing(
    caloriesConsumed: Double,
    caloriesTarget: Double,
    caloriesBurned: Double = 0.0,
    proteinConsumed: Double,
    proteinTarget: Double,
    carbsConsumed: Double,
    carbsTarget: Double,
    fatConsumed: Double,
    fatTarget: Double,
    modifier: Modifier = Modifier
) {
    val remainingCalories = (caloriesTarget + caloriesBurned - caloriesConsumed).coerceAtLeast(0.0)
    val calProgress by animateFloatAsState(
        targetValue = if (caloriesTarget > 0) (caloriesConsumed / (caloriesTarget + caloriesBurned)).toFloat().coerceIn(0f, 1f) else 0f,
        animationSpec = tween(800),
        label = "calProgress"
    )
    val proteinProgress by animateFloatAsState(
        targetValue = if (proteinTarget > 0) (proteinConsumed / proteinTarget).toFloat().coerceIn(0f, 1f) else 0f,
        animationSpec = tween(800),
        label = "proteinProgress"
    )
    val carbsProgress by animateFloatAsState(
        targetValue = if (carbsTarget > 0) (carbsConsumed / carbsTarget).toFloat().coerceIn(0f, 1f) else 0f,
        animationSpec = tween(800),
        label = "carbsProgress"
    )
    val fatProgress by animateFloatAsState(
        targetValue = if (fatTarget > 0) (fatConsumed / fatTarget).toFloat().coerceIn(0f, 1f) else 0f,
        animationSpec = tween(800),
        label = "fatProgress"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(22.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Anillo Circular
            Box(
                modifier = Modifier.size(190.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 10.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val arcSize = Size(diameter, diameter)
                    val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)

                    // Track de fondo
                    drawArc(
                        color = DarkSurfaceElevated,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Arco de calorías consumidas
                    drawArc(
                        color = AthleticOrange,
                        startAngle = -90f,
                        sweepAngle = calProgress * 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Anillo interior sutil de proteína
                    val innerStroke = 6.dp.toPx()
                    val innerDiameter = diameter - 26.dp.toPx()
                    val innerSize = Size(innerDiameter, innerDiameter)
                    val innerTopLeft = Offset((size.width - innerDiameter) / 2, (size.height - innerDiameter) / 2)

                    drawArc(
                        color = DarkSurfaceVariant,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = innerTopLeft,
                        size = innerSize,
                        style = Stroke(width = innerStroke, cap = StrokeCap.Round)
                    )

                    drawArc(
                        color = ProteinBlue,
                        startAngle = -90f,
                        sweepAngle = proteinProgress * 360f,
                        useCenter = false,
                        topLeft = innerTopLeft,
                        size = innerSize,
                        style = Stroke(width = innerStroke, cap = StrokeCap.Round)
                    )
                }

                // Centro del anillo
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${remainingCalories.toInt()}",
                        color = TextWhite,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "kcal restantes",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (caloriesBurned > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AthleticOrange.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = AthleticOrange, modifier = Modifier.size(11.dp))
                            Text("+${caloriesBurned.toInt()} quemadas", color = AthleticOrange, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Desglose de Macronutrientes (Proteínas, Carbos, Grasas)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MacroPillCard(
                    name = "Proteína",
                    currentG = proteinConsumed,
                    targetG = proteinTarget,
                    color = ProteinBlue,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                MacroPillCard(
                    name = "Carbos",
                    currentG = carbsConsumed,
                    targetG = carbsTarget,
                    color = CarbsYellow,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                MacroPillCard(
                    name = "Grasas",
                    currentG = fatConsumed,
                    targetG = fatTarget,
                    color = FatPurple,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MacroPillCard(
    name: String,
    currentG: Double,
    targetG: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    val progress = if (targetG > 0) (currentG / targetG).toFloat().coerceIn(0f, 1f) else 0f

    Surface(
        color = DarkSurfaceElevated,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(name, color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }

            Text(
                text = "${currentG.toInt()} / ${targetG.toInt()}g",
                color = TextWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = DarkSurface
            )
        }
    }
}
