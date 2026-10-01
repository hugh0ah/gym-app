package com.fittracker.app.ui.workout

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fittracker.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveWorkoutSheet(
    viewModel: LiveWorkoutViewModel,
    onDismiss: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val session = state.activeSession ?: return
    val haptic = LocalHapticFeedback.current
    var adjustExerciseTarget by remember { mutableStateOf<LiveWorkoutExercise?>(null) }

    val minutes = session.elapsedSeconds / 60
    val seconds = session.elapsedSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = DarkBg,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = DarkCardBorder)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Cabecera de la sesión activa
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = session.routineName,
                            color = TextWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AthleticOrange.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("EN VIVO", color = AthleticOrange, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        text = "⏱ $timeFormatted  •  Volumen: ${session.totalVolumeKg.toInt()} kg",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { viewModel.cancelWorkout() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertRed),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AlertRed.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Cancelar", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { viewModel.finishWorkout() },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonMint),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Terminar", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Banner flotante de temporizador de descanso
            if (session.restTimerRemainingSeconds != null) {
                val restMin = session.restTimerRemainingSeconds / 60
                val restSec = session.restTimerRemainingSeconds % 60
                val restFormatted = String.format("%02d:%02d", restMin, restSec)

                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(20.dp))
                            Column {
                                Text("DESCANSO ACTIVO", color = ElectricCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(restFormatted, color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilledTonalButton(
                                onClick = { viewModel.adjustRestTimer(-15) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = DarkSurfaceVariant)
                            ) {
                                Text("-15s", fontSize = 11.sp, color = TextWhite)
                            }
                            FilledTonalButton(
                                onClick = { viewModel.adjustRestTimer(+30) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = DarkSurfaceVariant)
                            ) {
                                Text("+30s", fontSize = 11.sp, color = TextWhite)
                            }
                            IconButton(
                                onClick = { viewModel.skipRestTimer() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Omitir", tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Lista de ejercicios y series interactivas
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                if (session.exercises.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Añade ejercicios a tu sesión libre", color = TextMuted, fontSize = 14.sp)
                            }
                        }
                    }
                }

                items(session.exercises, key = { it.exerciseId }) { exercise ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(18.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Cabecera ejercicio
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = exercise.exerciseName,
                                        color = TextWhite,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = exercise.muscleGroup,
                                        color = ElectricCyan,
                                        fontSize = 11.sp
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = { adjustExerciseTarget = exercise },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Tune, contentDescription = "Ajustar peso y reps para todas las series", tint = ElectricCyan, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = { viewModel.removeExercise(exercise.exerciseId) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar ejercicio", tint = TextSubtle, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            // Cabecera de la tabla de series
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("SERIE", color = TextSubtle, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(42.dp))
                                Text("ANTERIOR", color = TextSubtle, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.1f))
                                Text("PESO (KG)", color = TextSubtle, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(1.1f))
                                Text("REPS", color = TextSubtle, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                                Text("1RM", color = TextSubtle, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.width(42.dp))
                                Spacer(modifier = Modifier.width(42.dp)) // Espacio para botón de check
                            }

                            HorizontalDivider(color = DarkCardBorderSubtle)

                            // Filas de series
                            exercise.sets.forEach { set ->
                                LiveSetRow(
                                    set = set,
                                    onToggleComplete = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.toggleSetCompletion(exercise.exerciseId, set.id)
                                    },
                                    onWeightChange = { viewModel.updateSetWeight(exercise.exerciseId, set.id, it) },
                                    onRepsChange = { viewModel.updateSetReps(exercise.exerciseId, set.id, it) },
                                    onIncrementWeight = { delta -> viewModel.incrementSetWeight(exercise.exerciseId, set.id, delta) },
                                    onIncrementReps = { delta -> viewModel.incrementSetReps(exercise.exerciseId, set.id, delta) },
                                    onCycleType = { viewModel.cycleSetType(exercise.exerciseId, set.id) }
                                )
                            }

                            // Botón para añadir serie al ejercicio
                            OutlinedButton(
                                onClick = { viewModel.addSet(exercise.exerciseId) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextLight),
                                contentPadding = PaddingValues(vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Añadir Serie", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        // Diálogo para ajustar pesos y repeticiones de todas las series de un ejercicio
        adjustExerciseTarget?.let { exerciseToAdjust ->
            QuickAdjustLiveExerciseDialog(
                exercise = exerciseToAdjust,
                onDismiss = { adjustExerciseTarget = null },
                onApply = { weight, reps ->
                    viewModel.updateExerciseAllSetsWeightAndReps(exerciseToAdjust.exerciseId, weight, reps)
                    adjustExerciseTarget = null
                }
            )
        }
    }
}

@Composable
private fun LiveSetRow(
    set: LiveWorkoutSet,
    onToggleComplete: () -> Unit,
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onIncrementWeight: (Double) -> Unit,
    onIncrementReps: (Int) -> Unit,
    onCycleType: () -> Unit
) {
    val backgroundColor = if (set.isCompleted) NeonMint.copy(alpha = 0.12f) else Color.Transparent
    val borderColor = if (set.isCompleted) NeonMint.copy(alpha = 0.35f) else Color.Transparent
    var showSteppers by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .padding(vertical = 4.dp, horizontal = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de Serie y Selector de Tipo (N, W, F, D)
            Box(
                modifier = Modifier
                    .width(42.dp)
                    .clickable { onCycleType() }
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        when (set.type) {
                            SetType.NORMAL -> DarkSurfaceElevated
                            SetType.WARMUP -> WarningAmber.copy(alpha = 0.2f)
                            SetType.FAILURE -> AlertRed.copy(alpha = 0.25f)
                            SetType.DROP_SET -> CyberViolet.copy(alpha = 0.25f)
                        }
                    )
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${set.setNumber}",
                        color = TextWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (set.type != SetType.NORMAL) {
                        Text(
                            text = set.type.shortLabel,
                            color = when (set.type) {
                                SetType.WARMUP -> WarningAmber
                                SetType.FAILURE -> AlertRed
                                SetType.DROP_SET -> CyberViolet
                                else -> TextSubtle
                            },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Ghost text de sesión previa
            Text(
                text = set.previousGhost ?: "-",
                color = TextMuted,
                fontSize = 11.sp,
                maxLines = 1,
                modifier = Modifier.weight(1.1f)
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Campo de entrada de Peso (kg) con soporte para punto/coma
            NumericInputCell(
                value = set.weightKg,
                placeholder = if (set.targetWeightKg > 0) {
                    if (set.targetWeightKg % 1.0 == 0.0) "${set.targetWeightKg.toInt()}" else "${set.targetWeightKg}"
                } else "0",
                onValueChange = onWeightChange,
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.weight(1.1f)
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Campo de entrada de Repeticiones
            NumericInputCell(
                value = set.reps,
                placeholder = "${set.targetReps}",
                onValueChange = onRepsChange,
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(4.dp))

            // 1RM Calculado en vivo (al tocarlo despliega ajustes rápidos de peso/reps)
            Box(
                modifier = Modifier
                    .width(42.dp)
                    .clickable { showSteppers = !showSteppers },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (set.estimated1RM > 0) "${set.estimated1RM.toInt()}" else "-",
                    color = ElectricCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Botón Checkmark con animación y feedback háptico
            IconButton(
                onClick = onToggleComplete,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (set.isCompleted) NeonMint else DarkSurfaceElevated)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Marcar completada",
                    tint = if (set.isCompleted) Color.Black else TextSubtle,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Fila expansible de ajustes rápidos (+2.5 kg, -2.5 kg, +1 rep, -1 rep)
        AnimatedVisibility(visible = showSteppers) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, start = 48.dp, end = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Rápido:", color = TextMuted, fontSize = 10.sp)
                QuickDeltaChip(label = "-2.5kg", onClick = { onIncrementWeight(-2.5) })
                QuickDeltaChip(label = "+2.5kg", onClick = { onIncrementWeight(2.5) })
                QuickDeltaChip(label = "-1 rep", onClick = { onIncrementReps(-1) })
                QuickDeltaChip(label = "+1 rep", onClick = { onIncrementReps(1) })
            }
        }
    }
}

@Composable
private fun QuickDeltaChip(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        color = DarkSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
    ) {
        Text(
            text = label,
            color = TextLight,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun NumericInputCell(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Decimal,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = value,
        onValueChange = { input ->
            val sanitized = input.replace(',', '.')
            if (sanitized.count { it == '.' } <= 1 && sanitized.all { it.isDigit() || it == '.' }) {
                onValueChange(sanitized)
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        textStyle = LocalTextStyle.current.copy(
            color = TextWhite,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        ),
        cursorBrush = SolidColor(NeonMint),
        modifier = modifier,
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceElevated)
                    .border(
                        width = 1.dp,
                        color = if (value.isNotBlank()) NeonMint.copy(alpha = 0.5f) else DarkCardBorder,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = TextGhost,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun QuickAdjustLiveExerciseDialog(
    exercise: LiveWorkoutExercise,
    onDismiss: () -> Unit,
    onApply: (weight: Double, reps: Int) -> Unit
) {
    val initialWeight = exercise.sets.firstOrNull()?.let {
        it.weightKg.toDoubleOrNull() ?: it.targetWeightKg
    } ?: 60.0
    val initialReps = exercise.sets.firstOrNull()?.let {
        it.reps.toIntOrNull() ?: it.targetReps
    } ?: 10

    var currentWeight by remember { mutableStateOf(initialWeight) }
    var currentReps by remember { mutableStateOf(initialReps) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceElevated,
        shape = RoundedCornerShape(20.dp),
        title = {
            Column {
                Text("Ajustar Ejercicio", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                Text(exercise.exerciseName, color = ElectricCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Aplica este peso y repeticiones a todas las series de este ejercicio:",
                    color = TextMuted,
                    fontSize = 12.sp
                )

                // Ajuste de peso
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Peso:", color = TextLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        val wStr = if (currentWeight % 1.0 == 0.0) "${currentWeight.toInt()} kg" else String.format(java.util.Locale.US, "%.1f kg", currentWeight)
                        Text(wStr, color = NeonMint, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = { currentWeight = (currentWeight - 5.0).coerceAtLeast(0.0) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) { Text("-5", fontSize = 12.sp, color = TextLight) }

                        OutlinedButton(
                            onClick = { currentWeight = (currentWeight - 2.5).coerceAtLeast(0.0) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) { Text("-2.5", fontSize = 12.sp, color = TextLight) }

                        Button(
                            onClick = { currentWeight += 2.5 },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonMint),
                            contentPadding = PaddingValues(0.dp)
                        ) { Text("+2.5", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold) }

                        Button(
                            onClick = { currentWeight += 5.0 },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonMint),
                            contentPadding = PaddingValues(0.dp)
                        ) { Text("+5", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold) }
                    }
                }

                // Ajuste de repeticiones
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Repeticiones:", color = TextLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("$currentReps", color = ElectricCyan, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { currentReps = (currentReps - 1).coerceAtLeast(1) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) { Text("-1 Rep", fontSize = 12.sp, color = TextLight) }

                        Button(
                            onClick = { currentReps += 1 },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                        ) { Text("+1 Rep", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onApply(currentWeight, currentReps) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonMint),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Aplicar a Todas las Series", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextMuted)
            }
        }
    )
}
