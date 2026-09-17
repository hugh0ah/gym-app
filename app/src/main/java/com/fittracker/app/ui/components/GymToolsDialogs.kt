package com.fittracker.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.fittracker.app.ui.theme.*
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Calculadora de 1RM (One Rep Max) basada en las fórmulas de OpenGym:
 * - Epley: w * (1 + r / 30)
 * - Brzycki: w * 36 / (37 - r)
 * - Lombardi: w * (r ^ 0.10)
 */
@Composable
fun OneRepMaxDialog(
    initialWeight: Double = 80.0,
    initialReps: Int = 8,
    onDismiss: () -> Unit
) {
    var weightText by remember { mutableStateOf(if (initialWeight > 0) initialWeight.toString() else "80") }
    var repsText by remember { mutableStateOf(initialReps.toString()) }
    var formula by remember { mutableStateOf("epley") } // "epley", "brzycki", "lombardi"

    val weight = weightText.toDoubleOrNull() ?: 0.0
    val reps = (repsText.toIntOrNull() ?: 1).coerceIn(1, 15)

    val oneRepMax = remember(weight, reps, formula) {
        if (weight <= 0.0) 0.0
        else when (formula) {
            "brzycki" -> if (reps >= 37) weight else weight * 36.0 / (37.0 - reps)
            "lombardi" -> weight * (reps.toDouble().pow(0.10))
            else -> weight * (1.0 + reps / 30.0) // Epley por defecto
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
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
                            Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = AccentCoral, modifier = Modifier.size(24.dp))
                        }
                        Column {
                            Text(
                                text = "Calculadora 1RM (OpenGym)",
                                color = TextDark,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Fuerza máxima estimada y porcentajes",
                                color = TextDarkMuted,
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextDarkMuted)
                    }
                }

                // Inputs de Peso y Reps
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text("Peso levantado (kg)", color = TextDarkMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark,
                            focusedBorderColor = AccentCoral,
                            unfocusedBorderColor = CreamBorder
                        )
                    )

                    OutlinedTextField(
                        value = repsText,
                        onValueChange = { repsText = it },
                        label = { Text("Repeticiones (1-15)", color = TextDarkMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark,
                            focusedBorderColor = AccentCoral,
                            unfocusedBorderColor = CreamBorder
                        )
                    )
                }

                // Selector de Fórmula
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FormulaChip(
                        name = "Epley (Estándar)",
                        code = "epley",
                        current = formula,
                        modifier = Modifier.weight(1f)
                    ) { formula = it }
                    FormulaChip(
                        name = "Brzycki",
                        code = "brzycki",
                        current = formula,
                        modifier = Modifier.weight(1f)
                    ) { formula = it }
                    FormulaChip(
                        name = "Lombardi",
                        code = "lombardi",
                        current = formula,
                        modifier = Modifier.weight(1f)
                    ) { formula = it }
                }

                // Tarjeta 1RM Resultado Hero
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(CreamBg)
                        .border(1.dp, CreamBorder, RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "1RM ESTIMADO (PESO MÁXIMO A 1 REP)",
                            color = TextDarkMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${String.format("%.1f", oneRepMax)} kg",
                            color = AccentCoral,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Calculado a partir de $weight kg x $reps reps",
                            color = ElectricBlue,
                            fontSize = 12.sp
                        )
                    }
                }

                // Tabla de Cargas de Entrenamiento (% 1RM)
                Text(
                    text = "Porcentajes de Entrenamiento",
                    color = TextDark,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                val percentages = listOf(
                    Triple(95, "Fuerza Máxima", "1-2 reps"),
                    Triple(90, "Fuerza Pura", "3-4 reps"),
                    Triple(85, "Fuerza-Hipertrofia", "5-6 reps"),
                    Triple(80, "Hipertrofia Clásica", "7-8 reps"),
                    Triple(75, "Rango Medio", "9-10 reps"),
                    Triple(70, "Hipertrofia-Resistencia", "11-12 reps"),
                    Triple(65, "Resistencia Muscular", "15 reps"),
                    Triple(60, "Velocidad / Potencia", "20 reps")
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    percentages.forEach { (pct, goalDesc, repsDesc) ->
                        val targetKg = (oneRepMax * pct / 100.0)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(CreamSurface)
                                .border(1.dp, CreamBorder, RoundedCornerShape(10.dp))
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Badge(containerColor = AccentCoral.copy(alpha = 0.15f)) {
                                    Text("$pct%", color = AccentCoral, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                                Column {
                                    Text(goalDesc, color = TextDark, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Text(repsDesc, color = TextDarkMuted, fontSize = 10.sp)
                                }
                            }
                            Text(
                                text = "${String.format("%.1f", targetKg)} kg",
                                color = if (pct >= 85) AccentCoral else ElectricBlue,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormulaChip(
    name: String,
    code: String,
    current: String,
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit
) {
    val selected = current == code
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) AccentCoral.copy(alpha = 0.15f) else CreamSurface)
            .border(
                1.dp,
                if (selected) AccentCoral else CreamBorder,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick(code) }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            color = if (selected) AccentCoral else TextDarkMuted,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/**
 * Calculadora de Discos de Barra (Barbell Plate Calculator)
 * Calcula la combinación óptima de discos de 25kg, 20kg, 15kg, 10kg, 5kg, 2.5kg y 1.25kg
 * para cargar cada lado de la barra de forma instantánea.
 */
@Composable
fun PlateCalculatorDialog(
    initialTargetWeight: Double = 100.0,
    onDismiss: () -> Unit
) {
    var targetText by remember { mutableStateOf(initialTargetWeight.toString()) }
    var barWeight by remember { mutableStateOf(20.0) } // 20kg olímpica, 15kg técnica, 10kg barra corta

    val targetWeight = targetText.toDoubleOrNull() ?: 20.0
    val weightPerSide = ((targetWeight - barWeight) / 2.0).coerceAtLeast(0.0)

    // Desglose de discos por lado
    val availablePlates = listOf(
        Pair(25.0, Color(0xFFE53935)), // Rojo
        Pair(20.0, Color(0xFF1E88E5)), // Azul
        Pair(15.0, Color(0xFFFDD835)), // Amarillo
        Pair(10.0, Color(0xFF43A047)), // Verde
        Pair(5.0, Color(0xFFFAFAFA)),  // Blanco
        Pair(2.5, Color(0xFF212121)),  // Negro
        Pair(1.25, Color(0xFF9E9E9E))  // Gris / Cromo
    )

    val platesBreakdown = remember(weightPerSide) {
        var remainder = weightPerSide
        val breakdown = mutableListOf<Pair<Double, Int>>()
        for ((plate, _) in availablePlates) {
            val count = (remainder / plate).toInt()
            if (count > 0) {
                breakdown.add(Pair(plate, count))
                remainder -= count * plate
            }
        }
        breakdown
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
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
                                .background(ElectricBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Layers, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(24.dp))
                        }
                        Column {
                            Text(
                                text = "Cálculo de Discos de Barra",
                                color = TextDark,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Distribución exacta por cada lado",
                                color = TextDarkMuted,
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextDarkMuted)
                    }
                }

                // Input de Peso Objetivo
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it },
                    label = { Text("Peso total objetivo (barra + discos en kg)", color = TextDarkMuted) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark,
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = CreamBorder
                    )
                )

                // Selector de peso de la barra
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Tipo de barra:", color = TextDarkMuted, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BarWeightButton("Olímpica (20 kg)", 20.0, barWeight, Modifier.weight(1f)) { barWeight = it }
                        BarWeightButton("Técnica (15 kg)", 15.0, barWeight, Modifier.weight(1f)) { barWeight = it }
                        BarWeightButton("Corta (10 kg)", 10.0, barWeight, Modifier.weight(1f)) { barWeight = it }
                    }
                }

                // Representación Visual de la Barra con Discos
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(CreamBg)
                        .border(1.dp, CreamBorder, RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "CARGAR EN CADA LADO: ${String.format("%.2f", weightPerSide)} kg",
                            color = ElectricBlue,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        // Dibujo visual del manguito de la barra con los discos
                        BarbellVisualizer(plates = platesBreakdown, modifier = Modifier.fillMaxWidth().height(80.dp))

                        if (platesBreakdown.isEmpty()) {
                            Text("Solo la barra (sin discos adicionales)", color = TextDarkMuted, fontSize = 12.sp)
                        }
                    }
                }

                // Lista detallada de discos
                Text("Desglose por lado:", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                if (platesBreakdown.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        platesBreakdown.forEach { (plate, count) ->
                            val color = availablePlates.find { it.first == plate }?.second ?: ElectricBlue
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CreamSurface)
                                    .border(1.dp, CreamBorder, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                    Text(
                                        text = "Disco de ${plate} kg",
                                        color = TextDark,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = "× $count ${if (count == 1) "disco" else "discos"}",
                                    color = ElectricBlue,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BarWeightButton(
    label: String,
    weight: Double,
    current: Double,
    modifier: Modifier = Modifier,
    onClick: (Double) -> Unit
) {
    val selected = current == weight
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) ElectricBlue.copy(alpha = 0.15f) else CreamSurface)
            .border(
                1.dp,
                if (selected) ElectricBlue else CreamBorder,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick(weight) }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) ElectricBlue else TextDarkMuted,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun BarbellVisualizer(
    plates: List<Pair<Double, Int>>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val centerY = size.height / 2f
        val sleeveHeight = 16.dp.toPx()
        val collarWidth = 14.dp.toPx()
        val collarHeight = 36.dp.toPx()

        // Dibujar collarín de la barra
        drawRoundRect(
            color = Color.LightGray,
            topLeft = Offset(20f, centerY - collarHeight / 2f),
            size = Size(collarWidth, collarHeight),
            cornerRadius = CornerRadius(4f, 4f)
        )

        // Barra manga (sleeve)
        drawRoundRect(
            color = Color.Gray,
            topLeft = Offset(20f + collarWidth, centerY - sleeveHeight / 2f),
            size = Size(size.width - 40f, sleeveHeight),
            cornerRadius = CornerRadius(2f, 2f)
        )

        // Dibujar discos insertados uno tras otro
        var currentX = 20f + collarWidth + 4f
        for ((plate, count) in plates) {
            val plateHeight = when {
                plate >= 20.0 -> 64.dp.toPx()
                plate >= 15.0 -> 56.dp.toPx()
                plate >= 10.0 -> 48.dp.toPx()
                plate >= 5.0 -> 38.dp.toPx()
                else -> 28.dp.toPx()
            }
            val plateWidth = 12.dp.toPx()
            val color = when (plate) {
                25.0 -> Color(0xFFE53935)
                20.0 -> Color(0xFF1E88E5)
                15.0 -> Color(0xFFFDD835)
                10.0 -> Color(0xFF43A047)
                5.0 -> Color(0xFFFAFAFA)
                2.5 -> Color(0xFF212121)
                else -> Color(0xFF9E9E9E)
            }

            for (i in 0 until count) {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(currentX, centerY - plateHeight / 2f),
                    size = Size(plateWidth, plateHeight),
                    cornerRadius = CornerRadius(3f, 3f)
                )
                currentX += plateWidth + 3f
            }
        }
    }
}
