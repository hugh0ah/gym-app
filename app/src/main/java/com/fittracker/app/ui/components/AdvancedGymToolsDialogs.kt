package com.fittracker.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import com.fittracker.app.data.local.dao.RoutineExerciseWithDetails
import com.fittracker.app.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

/**
 * Diálogo de Sobrecarga Progresiva (basado en la lógica progression.js de OpenGym)
 * Permite calcular de forma científica el peso y repeticiones para la siguiente sesión:
 * - Progresión Lineal (+2.5 kg superior / +5 kg inferior si cumples repeticiones)
 * - Doble Progresión (rango 8-12 reps: sube peso solo al alcanzar el tope)
 * - Greyskull LP (última serie AMRAP al fallo con salto doble o descarga del 10%)
 */
@Composable
fun ProgressionOverloadDialog(
    initialExerciseName: String = "Press de Banca con Barra",
    initialWeight: Double = 70.0,
    initialReps: Int = 8,
    initialSets: Int = 4,
    onDismiss: () -> Unit
) {
    var weightText by remember { mutableStateOf(if (initialWeight > 0) initialWeight.toString() else "70") }
    var repsText by remember { mutableStateOf(initialReps.toString()) }
    var setsText by remember { mutableStateOf(initialSets.toString()) }
    var selectedPolicy by remember { mutableStateOf("linear") } // "linear", "double", "greyskull"
    var isLowerBody by remember { mutableStateOf(false) }
    var hitsTargetReps by remember { mutableStateOf(true) }
    var amrapRepsText by remember { mutableStateOf("12") }

    val currentWeight = weightText.toDoubleOrNull() ?: 0.0
    val targetReps = repsText.toIntOrNull() ?: 8
    val totalSets = setsText.toIntOrNull() ?: 4
    val amrapReps = amrapRepsText.toIntOrNull() ?: 10

    // Cálculo del siguiente peso e incremento recomendado
    val standardIncrement = if (isLowerBody) 5.0 else 2.5

    val (nextWeight, nextRepsSuggestion, adviceText, badgeColor) = remember(
        currentWeight, targetReps, totalSets, selectedPolicy, isLowerBody, hitsTargetReps, amrapReps
    ) {
        when (selectedPolicy) {
            "double" -> {
                // Doble Progresión: Rango típico 8-12 reps
                if (targetReps >= 12 && hitsTargetReps) {
                    val nextW = currentWeight + 2.5
                    Tuple4(
                        nextW,
                        8,
                        "¡Has alcanzado el tope del rango (12 reps)! Sube +2.5 kg y vuelve a empezar en 8 repeticiones.",
                        SecondaryEmerald
                    )
                } else if (hitsTargetReps) {
                    Tuple4(
                        currentWeight,
                        targetReps + 1,
                        "Objetivo cumplido. Mantén ${currentWeight} kg e intenta sacar ${targetReps + 1} reps en todas las series.",
                        AccentCyan
                    )
                } else {
                    Tuple4(
                        currentWeight,
                        targetReps,
                        "No completaste las repeticiones. Mantén el mismo peso y consolida la técnica antes de subir.",
                        WarningAmber
                    )
                }
            }
            "greyskull" -> {
                // Greyskull LP: 2 series base + 1 AMRAP
                if (amrapReps >= targetReps * 2) {
                    val doubleInc = standardIncrement * 2
                    Tuple4(
                        currentWeight + doubleInc,
                        targetReps,
                        "¡Rendimiento excepcional en AMRAP ($amrapReps reps)! Salto doble de carga (+${doubleInc} kg).",
                        WeightViolet
                    )
                } else if (amrapReps >= targetReps) {
                    Tuple4(
                        currentWeight + standardIncrement,
                        targetReps,
                        "AMRAP superado ($amrapReps reps >= $targetReps). Sube +${standardIncrement} kg en la próxima sesión.",
                        SecondaryEmerald
                    )
                } else {
                    val deload = (currentWeight * 0.9 / 2.5).roundToInt() * 2.5
                    Tuple4(
                        deload,
                        targetReps,
                        "Fallo en AMRAP (< $targetReps reps). Descarga sugerida del 10% (reinicia en ${deload} kg) para construir nuevo impulso.",
                        ErrorRed
                    )
                }
            }
            else -> {
                // Progresión Lineal
                if (hitsTargetReps) {
                    val nextW = currentWeight + standardIncrement
                    Tuple4(
                        nextW,
                        targetReps,
                        "¡Todas las series completadas! Sube +${standardIncrement} kg para mantener sobrecarga progresiva.",
                        SecondaryEmerald
                    )
                } else {
                    Tuple4(
                        currentWeight,
                        targetReps,
                        "No se completaron todas las series. Mantén ${currentWeight} kg. Si fallas 3 sesiones consecutivas, aplica descarga del 10%.",
                        WarningAmber
                    )
                }
            }
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
                // Encabezado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AccentCoral.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = AccentCoral)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Sobrecarga Progresiva", color = TextDark, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("Motor inteligente de progresión OpenGym", color = TextDarkMuted, fontSize = 12.sp)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextDarkMuted)
                    }
                }

                // Selector de Estrategia de Progresión
                Text("Estrategia de Progresión:", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedPolicy == "linear",
                        onClick = { selectedPolicy = "linear" },
                        label = { Text("Lineal (+2.5/+5 kg)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentCoral,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = selectedPolicy == "double",
                        onClick = { selectedPolicy = "double" },
                        label = { Text("Doble (8-12 reps)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = selectedPolicy == "greyskull",
                        onClick = { selectedPolicy = "greyskull" },
                        label = { Text("Greyskull LP (AMRAP)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FatPurple,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                // Tipo de levantamiento (Tren superior vs inferior)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("¿Es ejercicio de tren inferior (piernas/sentadilla)?", color = TextDarkMuted, fontSize = 13.sp)
                    Switch(
                        checked = isLowerBody,
                        onCheckedChange = { isLowerBody = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AccentCoral,
                            checkedTrackColor = AccentCoral.copy(alpha = 0.5f)
                        )
                    )
                }

                // Formulario de Carga Actual
                Card(
                    colors = CardDefaults.cardColors(containerColor = CreamBg),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Carga de la Última Sesión", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = weightText,
                                onValueChange = { weightText = it },
                                label = { Text("Peso (kg)", color = TextDarkMuted) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
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
                                label = { Text("Reps objetivo", color = TextDarkMuted) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextDark,
                                    unfocusedTextColor = TextDark,
                                    focusedBorderColor = AccentCoral,
                                    unfocusedBorderColor = CreamBorder
                                )
                            )
                            OutlinedTextField(
                                value = setsText,
                                onValueChange = { setsText = it },
                                label = { Text("Series", color = TextDarkMuted) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextDark,
                                    unfocusedTextColor = TextDark,
                                    focusedBorderColor = AccentCoral,
                                    unfocusedBorderColor = CreamBorder
                                )
                            )
                        }

                        if (selectedPolicy == "greyskull") {
                            OutlinedTextField(
                                value = amrapRepsText,
                                onValueChange = { amrapRepsText = it },
                                label = { Text("Reps logradas en serie final AMRAP al fallo", color = TextDarkMuted) },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextDark,
                                    unfocusedTextColor = TextDark,
                                    focusedBorderColor = AccentCoral,
                                    unfocusedBorderColor = CreamBorder
                                )
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("¿Completaste todas las series con éxito?", color = TextDarkMuted, fontSize = 13.sp)
                                Switch(
                                    checked = hitsTargetReps,
                                    onCheckedChange = { hitsTargetReps = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = AccentCoral,
                                        checkedTrackColor = AccentCoral.copy(alpha = 0.5f)
                                    )
                                )
                            }
                        }
                    }
                }

                // Tarjeta de Prescripción Siguiente Sesión
                Card(
                    colors = CardDefaults.cardColors(containerColor = CreamBg),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("OBJETIVO PRÓXIMA SESIÓN", color = AccentCoral, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                            Surface(
                                color = AccentCoral.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (hitsTargetReps || (selectedPolicy == "greyskull" && amrapReps >= targetReps)) "AVANZAR CARGA" else "CONSOLIDAR / DESCARGA",
                                    color = AccentCoral,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Peso Prescrito", color = TextDarkMuted, fontSize = 12.sp)
                                Text("$nextWeight kg", color = TextDark, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            }
                            HorizontalDivider(
                                modifier = Modifier
                                    .height(40.dp)
                                    .width(1.dp),
                                color = CreamBorder
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Repeticiones", color = TextDarkMuted, fontSize = 12.sp)
                                Text("$nextRepsSuggestion reps", color = ElectricBlue, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Text(
                            text = adviceText,
                            color = TextDark,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCoral),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Aplicar a mi Entrenamiento", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

/**
 * Diálogo de Volumen Muscular Semanal (basado en muscles.js de OpenGym)
 * Muestra el conteo de series efectivas por grupo muscular comparado con las directrices
 * científicas de hipertrofia muscular (10-20 series/semana para volumen óptimo).
 */
@Composable
fun MuscleVolumeDialog(
    routineExercises: List<RoutineExerciseWithDetails>,
    onDismiss: () -> Unit
) {
    // Agrupar y calcular series semanales estimadas por grupo muscular
    val volumeByGroup = remember(routineExercises) {
        val map = mutableMapOf(
            "Pecho" to 0,
            "Espalda" to 0,
            "Piernas" to 0,
            "Hombros" to 0,
            "Brazos" to 0,
            "Core" to 0
        )
        routineExercises.forEach { ex ->
            val mg = ex.muscleGroup.lowercase()
            val sets = ex.sets
            when {
                mg.contains("pecho") || mg.contains("chest") -> map["Pecho"] = (map["Pecho"] ?: 0) + sets
                mg.contains("espalda") || mg.contains("back") || mg.contains("lats") -> map["Espalda"] = (map["Espalda"] ?: 0) + sets
                mg.contains("pierna") || mg.contains("leg") || mg.contains("glute") || mg.contains("quad") -> map["Piernas"] = (map["Piernas"] ?: 0) + sets
                mg.contains("hombro") || mg.contains("shoulder") || mg.contains("delt") -> map["Hombros"] = (map["Hombros"] ?: 0) + sets
                mg.contains("brazo") || mg.contains("arm") || mg.contains("bicep") || mg.contains("tricep") -> map["Brazos"] = (map["Brazos"] ?: 0) + sets
                mg.contains("core") || mg.contains("abs") || mg.contains("abdomen") -> map["Core"] = (map["Core"] ?: 0) + sets
                else -> map["Pecho"] = (map["Pecho"] ?: 0) + sets
            }
        }
        map
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
                // Encabezado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AccentCoral.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PieChart, contentDescription = null, tint = AccentCoral)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Volumen por Músculo", color = TextDark, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("Series semanales efectivas (OpenGym)", color = TextDarkMuted, fontSize = 12.sp)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextDarkMuted)
                    }
                }

                // Leyenda de referencia científica
                Card(
                    colors = CardDefaults.cardColors(containerColor = CreamBg),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        VolumeLegendItem(color = WarningAmber, label = "<10", desc = "Mantenimiento")
                        VolumeLegendItem(color = SecondaryEmerald, label = "10-20", desc = "Óptimo Hipertrofia")
                        VolumeLegendItem(color = AccentCoral, label = ">20", desc = "Alto Volumen")
                    }
                }

                // Barras de progreso por grupo muscular
                volumeByGroup.forEach { (group, sets) ->
                    val progress = (sets / 24f).coerceIn(0f, 1f)
                    val (statusColor, statusText) = when {
                        sets < 10 -> Pair(WarningAmber, "Volumen Bajo / Mantenimiento")
                        sets <= 20 -> Pair(SecondaryEmerald, "Rango Óptimo de Crecimiento")
                        else -> Pair(AccentCoral, "Volumen Máximo Recuperable")
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = CreamBg),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(group, color = TextDark, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("$sets ", color = statusColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Text("series/sem", color = TextDarkMuted, fontSize = 12.sp)
                                }
                            }

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = statusColor,
                                trackColor = CreamBorder
                            )

                            Text(
                                text = statusText,
                                color = statusColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCoral),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Cerrar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun VolumeLegendItem(color: Color, label: String, desc: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Column {
            Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(desc, color = TextDarkMuted, fontSize = 10.sp)
        }
    }
}

/**
 * Diálogo de Mapa de Calor de Entrenamientos (Activity Heatmap basado en Heatmap.jsx de OpenGym)
 * Muestra una cuadrícula estilo GitHub de las últimas semanas de consistencia en el gimnasio.
 */
@Composable
fun WorkoutHeatmapDialog(
    workoutDates: Set<String> = emptySet(),
    onDismiss: () -> Unit
) {
    // Generar las últimas 12 semanas (84 días)
    val today = remember { LocalDate.now() }
    val daysGrid = remember(today, workoutDates) {
        val list = mutableListOf<HeatmapDay>()
        // Retroceder 12 semanas desde el lunes de esta semana
        val startDay = today.minusWeeks(11).minusDays((today.dayOfWeek.value - 1).toLong())
        var cur = startDay
        while (!cur.isAfter(today)) {
            val dateStr = cur.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val hasTrained = workoutDates.contains(dateStr)
            list.add(HeatmapDay(date = cur, dateStr = dateStr, hasTrained = hasTrained))
            cur = cur.plusDays(1)
        }
        list
    }

    val totalTrainedDays = workoutDates.size
    val currentStreak = remember(today, workoutDates) {
        var streak = 0
        var checkDate = today
        // Si hoy no entrenó, mirar si ayer sí
        val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
        if (!workoutDates.contains(todayStr)) {
            checkDate = today.minusDays(1)
        }
        while (workoutDates.contains(checkDate.format(DateTimeFormatter.ISO_LOCAL_DATE))) {
            streak++
            checkDate = checkDate.minusDays(1)
        }
        streak
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(26.dp)),
            color = CreamSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Encabezado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AccentCoral.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = AccentCoral)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Consistencia Anual", color = TextDark, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("Mapa de actividad estilo GitHub (OpenGym)", color = TextDarkMuted, fontSize = 12.sp)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextDarkMuted)
                    }
                }

                // Tarjetas de estadísticas de racha
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = CreamBg),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Días Activos", color = TextDarkMuted, fontSize = 11.sp)
                            Text("$totalTrainedDays días", color = AccentCoral, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = CreamBg),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Racha Actual", color = TextDarkMuted, fontSize = 11.sp)
                            Text("$currentStreak días", color = ElectricBlue, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Cuadrícula de mapa de calor
                Text("Últimas 12 semanas:", color = TextDark, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CreamBg)
                        .border(1.dp, CreamBorder, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    // Dibujar cuadrícula de días agrupados en columnas de 7 días
                    val columns = daysGrid.chunked(7)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        columns.forEach { week ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                week.forEach { day ->
                                    val cellColor = when {
                                        day.hasTrained -> AccentCoral
                                        day.date == today -> ElectricBlue.copy(alpha = 0.4f)
                                        else -> CreamBorder
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(cellColor)
                                            .border(
                                                width = if (day.date == today) 1.dp else 0.dp,
                                                color = if (day.date == today) ElectricBlue else Color.Transparent,
                                                shape = RoundedCornerShape(3.dp)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }

                // Leyenda del mapa
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Menos", color = TextDarkMuted, fontSize = 10.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(CreamBorder))
                    Spacer(modifier = Modifier.width(3.dp))
                    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(AccentCoral.copy(alpha = 0.4f)))
                    Spacer(modifier = Modifier.width(3.dp))
                    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(AccentCoral))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Más entrenamientos", color = TextDarkMuted, fontSize = 10.sp)
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCoral),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Cerrar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private data class HeatmapDay(
    val date: LocalDate,
    val dateStr: String,
    val hasTrained: Boolean
)
