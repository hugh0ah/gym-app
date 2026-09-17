package com.fittracker.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.fittracker.app.data.local.entities.BodyCompositionLog
import com.fittracker.app.ui.theme.*

@Composable
fun BodyCompositionDialog(
    logs: List<BodyCompositionLog>,
    onDismiss: () -> Unit,
    onDeleteLog: (Long) -> Unit = {},
    onScanNewScale: () -> Unit = {}
) {
    val latest = logs.firstOrNull()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(26.dp)),
            color = CreamSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Cabecera
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AccentCoral.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Insights, contentDescription = null, tint = AccentCoral, modifier = Modifier.size(24.dp))
                        }
                        Column {
                            Text(
                                text = "Composición Corporal",
                                color = TextDark,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Bioimpedancia de Báscula Inteligente",
                                color = TextDarkMuted,
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextDarkMuted)
                    }
                }

                if (latest != null) {
                    // Métricas principales actuales (Fila 1)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CompositionMetricTile(
                            label = "Peso",
                            value = "${latest.weightKg} kg",
                            subLabel = "IMC: ${latest.bmi}",
                            color = AccentCoral,
                            modifier = Modifier.weight(1f)
                        )
                        CompositionMetricTile(
                            label = "Grasa Corporal",
                            value = "${latest.bodyFatPercentage}%",
                            subLabel = "G. Visceral: ${latest.visceralFat}",
                            color = CalorieOrange,
                            modifier = Modifier.weight(1f)
                        )
                        CompositionMetricTile(
                            label = "Masa Muscular",
                            value = "${latest.muscleMassKg} kg",
                            subLabel = "Agua: ${latest.bodyWaterPercentage}%",
                            color = ElectricBlue,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Métricas complementarias de bioimpedancia (Fila 2)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CompositionMetricTile(
                            label = "% Agua Corporal",
                            value = "${latest.bodyWaterPercentage}%",
                            subLabel = "Hidratación",
                            color = ElectricBlue,
                            modifier = Modifier.weight(1f)
                        )
                        CompositionMetricTile(
                            label = "Masa Ósea",
                            value = "${latest.boneMassKg} kg",
                            subLabel = "Densidad",
                            color = FatPurple,
                            modifier = Modifier.weight(1f)
                        )
                        CompositionMetricTile(
                            label = "% Proteínas",
                            value = "${latest.proteinPercentage}%",
                            subLabel = "Tejido magro",
                            color = ProteinBlue,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Gráfica de evolución temporal
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(CreamBg)
                            .border(1.dp, CreamBorder, RoundedCornerShape(18.dp))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Evolución Temporal",
                                    color = TextDark,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    LegendIndicator(label = "Peso", color = TextDark)
                                    LegendIndicator(label = "% Grasa", color = CalorieOrange)
                                    LegendIndicator(label = "Músculo", color = ElectricBlue)
                                }
                            }

                            BodyEvolutionCanvas(
                                logs = logs.reversed(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            )
                        }
                    }

                    // Desglose de compartimentos corporales (Barra segmentada)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(CreamBg)
                            .border(1.dp, CreamBorder, RoundedCornerShape(18.dp))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Distribución de Bioimpedancia",
                                color = TextDark,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Barra segmentada
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(18.dp)
                                    .clip(RoundedCornerShape(9.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(latest.bodyFatPercentage.toFloat().coerceAtLeast(1f))
                                        .fillMaxHeight()
                                        .background(CalorieOrange)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(
                                            if (latest.weightKg > 0)
                                                (latest.muscleMassKg.toFloat() / latest.weightKg.toFloat() * 100f).coerceAtLeast(1f)
                                            else 50f
                                        )
                                        .fillMaxHeight()
                                        .background(ElectricBlue)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(
                                            if (latest.weightKg > 0)
                                                ((latest.boneMassKg.toFloat() / latest.weightKg.toFloat()) * 100f).coerceAtLeast(3f)
                                            else 4f
                                        )
                                        .fillMaxHeight()
                                        .background(FatPurple)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                SegmentLegend(name = "Grasa", pct = "${latest.bodyFatPercentage}%", color = CalorieOrange)
                                SegmentLegend(
                                    name = "Músculo",
                                    pct = if (latest.weightKg > 0) "${String.format("%.1f", (latest.muscleMassKg / latest.weightKg) * 100)}%" else "—",
                                    color = ElectricBlue
                                )
                                SegmentLegend(name = "Masa Ósea", pct = "${latest.boneMassKg} kg", color = FatPurple)
                            }
                        }
                    }

                    // Indicadores de Salud: Grasa Visceral & TMB
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Semáforo Grasa Visceral
                        val visceralColor = when {
                            latest.visceralFat <= 9 -> SuccessGreen
                            latest.visceralFat <= 14 -> WarningAmber
                            else -> ErrorRed
                        }
                        val visceralText = when {
                            latest.visceralFat <= 9 -> "Nivel Óptimo (1-9)"
                            latest.visceralFat <= 14 -> "Nivel Alto (10-14)"
                            else -> "Peligro (>14)"
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(CreamBg)
                                .border(1.dp, visceralColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Grasa Visceral", color = TextDarkMuted, fontSize = 12.sp)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "${latest.visceralFat}",
                                        color = visceralColor,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(visceralColor)
                                    )
                                }
                                Text(text = visceralText, color = visceralColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }

                        // Tasa Metabólica Basal
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(CreamBg)
                                .border(1.dp, CreamBorder, RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("TMB (Báscula)", color = TextDarkMuted, fontSize = 12.sp)
                                Text(
                                    text = "${latest.bmrKcal.toInt()} kcal",
                                    color = TextDark,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text("Gasto mínimo en reposo", color = TextDarkMuted, fontSize = 11.sp)
                            }
                        }
                    }

                    // Historial de Registros
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Historial de Mediciones (${logs.size})",
                            color = TextDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        logs.forEach { item ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CreamBg)
                                    .border(1.dp, CreamBorder, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = item.date, color = TextDark, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            text = "${item.weightKg} kg · ${item.bodyFatPercentage}% grasa · ${item.muscleMassKg}kg músculo · TMB: ${item.bmrKcal.toInt()} kcal",
                                            color = TextDarkMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteLog(item.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = ErrorRed.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Botón para subir nueva captura
                    Button(
                        onClick = {
                            onDismiss()
                            onScanNewScale()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCoral)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Escanear Nueva Medición de Báscula", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "No hay mediciones de báscula inteligente aún.\nSube una captura para ver tus gráficas de bioimpedancia.",
                                color = TextDarkMuted,
                                fontSize = 14.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Button(
                                onClick = {
                                    onDismiss()
                                    onScanNewScale()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentCoral)
                            ) {
                                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Subir Captura de Báscula", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompositionMetricTile(
    label: String,
    value: String,
    subLabel: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CreamBg)
            .border(1.dp, CreamBorder, RoundedCornerShape(14.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(text = label, color = TextDarkMuted, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, color = color, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subLabel, color = TextDarkMuted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun LegendIndicator(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(text = label, color = TextDarkMuted, fontSize = 11.sp)
    }
}

@Composable
private fun SegmentLegend(name: String, pct: String, color: Color) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
            Text(text = name, color = TextDarkMuted, fontSize = 11.sp)
        }
        Text(text = pct, color = TextDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BodyEvolutionCanvas(
    logs: List<BodyCompositionLog>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (logs.size < 2) {
            drawLine(
                color = CreamBorder,
                start = Offset(0f, size.height / 2),
                end = Offset(size.width, size.height / 2),
                strokeWidth = 2.dp.toPx()
            )
            return@Canvas
        }

        val stepX = size.width / (logs.size - 1)

        val minWeight = logs.minOf { it.weightKg } - 2.0
        val maxWeight = logs.maxOf { it.weightKg } + 2.0
        val minFat = logs.minOf { it.bodyFatPercentage } - 2.0
        val maxFat = logs.maxOf { it.bodyFatPercentage } + 2.0
        val minMuscle = logs.minOf { it.muscleMassKg } - 2.0
        val maxMuscle = logs.maxOf { it.muscleMassKg } + 2.0

        val weightPath = Path()
        val fatPath = Path()
        val musclePath = Path()

        logs.forEachIndexed { index, item ->
            val x = index * stepX

            // Normalización de Peso
            val weightY = size.height - (((item.weightKg - minWeight) / (maxWeight - minWeight).coerceAtLeast(0.1)).toFloat() * size.height)
            if (index == 0) weightPath.moveTo(x, weightY) else weightPath.lineTo(x, weightY)

            // Normalización de % Grasa
            val fatY = size.height - (((item.bodyFatPercentage - minFat) / (maxFat - minFat).coerceAtLeast(0.1)).toFloat() * size.height)
            if (index == 0) fatPath.moveTo(x, fatY) else fatPath.lineTo(x, fatY)

            // Normalización de Músculo
            val muscleY = size.height - (((item.muscleMassKg - minMuscle) / (maxMuscle - minMuscle).coerceAtLeast(0.1)).toFloat() * size.height)
            if (index == 0) musclePath.moveTo(x, muscleY) else musclePath.lineTo(x, muscleY)

            // Puntos
            drawCircle(color = TextDark, radius = 4.dp.toPx(), center = Offset(x, weightY))
            drawCircle(color = CalorieOrange, radius = 4.dp.toPx(), center = Offset(x, fatY))
            drawCircle(color = ElectricBlue, radius = 4.dp.toPx(), center = Offset(x, muscleY))
        }

        // Trazado de líneas
        drawPath(path = weightPath, color = TextDark, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
        drawPath(path = fatPath, color = CalorieOrange, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
        drawPath(path = musclePath, color = ElectricBlue, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
    }
}
