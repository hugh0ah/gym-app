package com.fittracker.app.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fittracker.app.data.local.entities.BodyCompositionLog
import com.fittracker.app.ui.components.ApiKeySettingsDialog
import com.fittracker.app.ui.components.BodyCompositionDialog
import com.fittracker.app.ui.components.MacroProgressBar
import com.fittracker.app.ui.components.StatCard
import com.fittracker.app.ui.profile.UserProfileDialog
import com.fittracker.app.ui.scale.SmartScaleImportDialog
import com.fittracker.app.ui.scale.SmartScaleViewModel
import com.fittracker.app.ui.theme.*
import com.fittracker.app.ui.wearable.WearableImportDialog
import com.fittracker.app.ui.wearable.WearableViewModel
import com.fittracker.app.ui.weight.WeightTrackerDialog
import com.fittracker.app.ui.weight.WeightViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    wearableViewModel: WearableViewModel,
    weightViewModel: WeightViewModel,
    smartScaleViewModel: SmartScaleViewModel,
    onNavigateToFood: () -> Unit,
    onNavigateToRoutine: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CreamBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // ==========================================
            // CABECERA MODERNA CON PERFIL Y ESTADO DE IA
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AccentCoral)
                        )
                        Text(
                            text = "WELLNESS TRACKER",
                            color = AccentCoral,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp
                        )
                    }
                    Text(
                        text = "Fitness Dashboard",
                        color = TextDark,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    val dateHeaderSubtitle = if (state.isViewingToday) "Hoy, ${state.date}" else state.date
                    Text(
                        text = "$dateHeaderSubtitle • ${if (state.userProfile.gender.equals("FEMALE", ignoreCase = true)) "Mujer" else "Hombre"}, ${state.userProfile.weightKg} kg",
                        color = TextDarkMuted,
                        fontSize = 12.sp
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón Mi Perfil
                    Surface(
                        onClick = { viewModel.openUserProfileDialog() },
                        shape = RoundedCornerShape(14.dp),
                        color = CreamSurface,
                        border = BorderStroke(1.dp, CreamBorder),
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Perfil",
                                tint = AccentCoral,
                                modifier = Modifier.size(17.dp)
                            )
                            val genderSymbol = if (state.userProfile.gender.equals("FEMALE", ignoreCase = true)) "♀" else "♂"
                            Text(
                                "Perfil $genderSymbol",
                                color = TextDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Botón API Key (Indicador de IA)
                    Surface(
                        onClick = { viewModel.openApiKeyDialog() },
                        shape = RoundedCornerShape(14.dp),
                        color = CreamSurface,
                        border = BorderStroke(
                            1.dp,
                            if (state.isApiKeyConfigured) SuccessGreen.copy(alpha = 0.5f) else WarningAmber.copy(alpha = 0.5f)
                        ),
                        shadowElevation = 2.dp
                    ) {
                        Box(
                            modifier = Modifier.padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Configurar API Key",
                                tint = if (state.isApiKeyConfigured) SuccessGreen else WarningAmber,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // ==========================================
            // SELECTOR DE FECHA (DÍA ANTERIOR / SIGUIENTE / HOY)
            // ==========================================
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CreamSurface,
                border = BorderStroke(1.dp, CreamBorder),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.changeDate(-1) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Día anterior", tint = TextDark)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = AccentCoral,
                            modifier = Modifier.size(16.dp)
                        )
                        val todayStr = java.time.LocalDate.now().toString()
                        val yesterdayStr = java.time.LocalDate.now().minusDays(1).toString()
                        val tomorrowStr = java.time.LocalDate.now().plusDays(1).toString()
                        val dateLabel = when (state.date) {
                            todayStr -> "Hoy, ${state.date}"
                            yesterdayStr -> "Ayer, ${state.date}"
                            tomorrowStr -> "Mañana, ${state.date}"
                            else -> state.date
                        }
                        Text(
                            text = dateLabel,
                            color = TextDark,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (!state.isViewingToday) {
                            Surface(
                                onClick = { viewModel.selectToday() },
                                shape = RoundedCornerShape(8.dp),
                                color = AccentCoralContainer
                            ) {
                                Text(
                                    text = "Ir a Hoy",
                                    color = AccentCoralDark,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = { viewModel.changeDate(1) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Día siguiente", tint = TextDark)
                    }
                }
            }

            // ==========================================
            // 1. WEEKLY ACTIVITY SUMMARY AT TOP (COMPACT)
            // ==========================================
            WeeklyActivitySummaryCard(
                weeklyActivity = state.weeklyActivity,
                completedCount = state.weeklyCompletedCount,
                targetDays = state.weeklyTargetDays,
                totalMinutes = state.weeklyTotalMinutes,
                todaySteps = state.steps,
                selectedDate = state.date,
                onSelectDay = { viewModel.selectDate(it) }
            )

            // ==========================================
            // 2. CALORIES BURNED TRACKER WITH CIRCULAR RING (COMPACT CARD)
            // ==========================================
            CaloriesBurnedRingCard(
                burned = state.activeCaloriesBurned,
                target = state.calorieTarget,
                consumed = state.caloriesConsumed,
                net = state.netCalories
            )

            // ==========================================
            // 3. WORKOUT STREAK COUNTER BELOW CALORIES CARD
            // ==========================================
            WorkoutStreakCard(
                streakDays = state.currentStreakDays,
                bestStreakDays = state.bestStreakDays
            )

            // ==========================================
            // BOTONES DE ACCIÓN RÁPIDA (ALLOW ALL - TOTALMENTE OPERATIVOS)
            // ==========================================
            Text(
                text = "ACCIONES DE SALUD Y REGISTRO",
                color = TextDarkMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                WellnessActionPill(
                    title = "Anotar Peso",
                    subtitle = if (state.latestWeight != null) "${state.latestWeight?.weightKg} kg" else "Registrar",
                    icon = Icons.Default.MonitorWeight,
                    accentColor = AccentCoral,
                    onClick = { viewModel.openWeightDialog() }
                )

                WellnessActionPill(
                    title = "Escanear Báscula",
                    subtitle = "Gemini Vision",
                    icon = Icons.Default.PhotoCamera,
                    accentColor = ElectricBlue,
                    onClick = { viewModel.openSmartScaleDialog() }
                )

                WellnessActionPill(
                    title = "Composición",
                    subtitle = if (state.latestBodyComposition != null) "${state.latestBodyComposition?.bodyFatPercentage}% grasa" else "Ver gráfica",
                    icon = Icons.Default.ShowChart,
                    accentColor = WarmFlame,
                    onClick = { viewModel.openBodyCompositionDialog() }
                )

                WellnessActionPill(
                    title = "Huawei Fit 3",
                    subtitle = "${state.steps} pasos",
                    icon = Icons.Default.Watch,
                    accentColor = StepsTeal,
                    onClick = { viewModel.openWearableDialog() }
                )

                WellnessActionPill(
                    title = "Mi Perfil",
                    subtitle = "Metabolismo",
                    icon = Icons.Default.Tune,
                    accentColor = PrimaryIndigo,
                    onClick = { viewModel.openUserProfileDialog() }
                )
            }

            // ==========================================
            // RESUMEN DE BÁSCULA & BIOIMPEDANCIA
            // ==========================================
            SmartScaleWellnessCard(
                latest = state.latestBodyComposition,
                onScanScale = { viewModel.openSmartScaleDialog() },
                onViewChart = { viewModel.openBodyCompositionDialog() }
            )

            // ==========================================
            // RESUMEN DE ACTIVIDAD Y GIMNASIO (CUADRÍCULA COMPACTA)
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Pasos Huawei",
                    value = "${state.steps}",
                    subtitle = if (state.steps >= 10000) "¡Meta superada! 🎉" else "Objetivo: 10.000",
                    icon = Icons.Default.DirectionsWalk,
                    iconColor = StepsTeal,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.openWearableDialog() }
                )

                StatCard(
                    title = "Tiempo Ejercicio",
                    value = "${state.totalExerciseMinutes} min",
                    subtitle = if (state.cardioType.isNotBlank()) "Cardio: ${state.cardioType}" else "Fuerza + Cardio",
                    icon = Icons.Default.Timer,
                    iconColor = ElectricBlue,
                    modifier = Modifier.weight(1f)
                )
            }

            // ==========================================
            // HIDRATACIÓN / WATER TRACKER COMPACTO
            // ==========================================
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CreamSurface,
                border = BorderStroke(1.dp, CreamBorder),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ElectricBlue.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            val percent = ((state.waterConsumedMl.toDouble() / state.waterGoalMl.coerceAtLeast(1)) * 100).toInt()
                            Text(
                                text = "Agua: ${state.waterConsumedMl} / ${state.waterGoalMl} ml",
                                color = TextDark,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$percent% de la meta diaria",
                                color = TextDarkMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { viewModel.addWater(250) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = ElectricBlue.copy(alpha = 0.12f),
                                contentColor = ElectricBlue
                            ),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text("+250 ml", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        FilledTonalButton(
                            onClick = { viewModel.addWater(500) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = ElectricBlue.copy(alpha = 0.12f),
                                contentColor = ElectricBlue
                            ),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text("+500 ml", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ==========================================
            // DESGLOSE DE MACROS (SEGÚN SEXO Y PESO)
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(CreamSurface)
                    .border(1.dp, CreamBorder, RoundedCornerShape(22.dp))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Nutrición y Macronutrientes",
                                color = TextDark,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Ajustados a tu peso (${state.userProfile.weightKg} kg, ${if (state.userProfile.gender == "FEMALE") "Mujer" else "Hombre"})",
                                color = TextDarkMuted,
                                fontSize = 11.sp
                            )
                        }
                        TextButton(onClick = { viewModel.openUserProfileDialog() }) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp), tint = AccentCoral)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ajustar", color = AccentCoral, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    MacroProgressBar(
                        label = "Proteínas",
                        currentG = state.proteinConsumed,
                        targetG = state.proteinTarget,
                        color = ElectricBlue
                    )

                    MacroProgressBar(
                        label = "Carbohidratos",
                        currentG = state.carbsConsumed,
                        targetG = state.carbTarget,
                        color = WarmFlame
                    )

                    MacroProgressBar(
                        label = "Grasas",
                        currentG = state.fatConsumed,
                        targetG = state.fatTarget,
                        color = AccentCoral
                    )
                }
            }

            // ==========================================
            // ACCIONES DIRECTAS DE NAVEGACIÓN
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onNavigateToFood,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCoral),
                    contentPadding = PaddingValues(vertical = 14.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Añadir Comida", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onNavigateToRoutine,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    contentPadding = PaddingValues(vertical = 14.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Mi Rutina", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            // ==========================================
            // 4. WEEKLY WORKOUT BAR CHART AT BOTTOM (ELECTRIC BLUE)
            // ==========================================
            WeeklyWorkoutBarChartCard(
                weeklyActivity = state.weeklyActivity,
                weeklyTotalMinutes = state.weeklyTotalMinutes
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // ==========================================
        // DIÁLOGOS REACTIVOS TOTALMENTE OPERATIVOS
        // ==========================================

        // Diálogo de Báscula Inteligente (Subir captura / Gemini Vision)
        if (state.isSmartScaleDialogVisible) {
            SmartScaleImportDialog(
                viewModel = smartScaleViewModel,
                onDismiss = {
                    viewModel.closeSmartScaleDialog()
                    smartScaleViewModel.reset()
                },
                onSuccessSaved = {
                    viewModel.closeSmartScaleDialog()
                    smartScaleViewModel.reset()
                }
            )
        }

        // Diálogo de Gráfica de Composición Corporal
        if (state.isBodyCompositionDialogVisible) {
            BodyCompositionDialog(
                logs = state.bodyCompositionLogs,
                onDismiss = { viewModel.closeBodyCompositionDialog() },
                onDeleteLog = { id -> viewModel.deleteBodyCompositionLog(id) },
                onScanNewScale = {
                    viewModel.closeBodyCompositionDialog()
                    viewModel.openSmartScaleDialog()
                }
            )
        }

        // Diálogo de Perfil y Cálculos según Sexo/Peso (Mifflin-St Jeor)
        if (state.isUserProfileDialogVisible) {
            UserProfileDialog(
                initialProfile = state.userProfile,
                onDismiss = { viewModel.closeUserProfileDialog() },
                onSaveProfile = { newProfile ->
                    viewModel.saveUserProfile(newProfile)
                    viewModel.closeUserProfileDialog()
                }
            )
        }

        // Diálogo de importación de wearable (Huawei Watch Fit 3)
        if (state.isWearableDialogVisible) {
            LaunchedEffect(state.date) {
                wearableViewModel.prepareForDate(state.date)
            }
            WearableImportDialog(
                viewModel = wearableViewModel,
                onDismiss = {
                    viewModel.closeWearableDialog()
                    wearableViewModel.reset()
                },
                onSuccessSaved = {
                    viewModel.closeWearableDialog()
                    wearableViewModel.reset()
                }
            )
        }

        // Diálogo de Control de Peso Corporal
        if (state.isWeightDialogVisible) {
            WeightTrackerDialog(
                viewModel = weightViewModel,
                onOpenScaleScanner = {
                    viewModel.closeWeightDialog()
                    viewModel.openSmartScaleDialog()
                },
                onDismiss = { viewModel.closeWeightDialog() }
            )
        }

        // Diálogo de configuración de API Key
        if (state.isApiKeyDialogVisible) {
            ApiKeySettingsDialog(
                currentKey = viewModel.currentApiKey,
                onSaveKey = { newKey ->
                    viewModel.saveApiKey(newKey)
                },
                onDismiss = { viewModel.closeApiKeyDialog() }
            )
        }
    }
}

/**
 * 1. Resumen de Actividad Semanal en la parte superior (compacto)
 */
@Composable
private fun WeeklyActivitySummaryCard(
    weeklyActivity: List<DayActivitySummary>,
    completedCount: Int,
    targetDays: Int,
    totalMinutes: Int,
    todaySteps: Int,
    selectedDate: String = "",
    onSelectDay: (String) -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = CreamSurface,
        border = BorderStroke(1.dp, CreamBorder),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = AccentCoral,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Actividad Semanal",
                        color = TextDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = AccentCoralContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "$completedCount/$targetDays días activos",
                        color = AccentCoralDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Fila de 7 días compacta e interactiva
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                weeklyActivity.forEach { day ->
                    val isCurrentSelection = day.date == selectedDate
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isCurrentSelection) AccentCoralContainer.copy(alpha = 0.5f) else Color.Transparent)
                            .clickable { onSelectDay(day.date) }
                            .padding(horizontal = 5.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = day.dayKey,
                            fontSize = 11.sp,
                            fontWeight = if (day.isToday || isCurrentSelection) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (day.isToday) AccentCoral else if (isCurrentSelection) TextDark else TextDarkMuted
                        )

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        day.isCompleted -> AccentCoral
                                        day.isToday -> AccentCoralContainer
                                        else -> CreamSurfaceVariant
                                    }
                                )
                                .then(
                                    when {
                                        isCurrentSelection -> Modifier.border(2.dp, AccentCoralDark, CircleShape)
                                        day.isToday -> Modifier.border(1.5.dp, AccentCoral, CircleShape)
                                        else -> Modifier
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (day.isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Completado",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else if (day.isToday) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(AccentCoral)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(TextDarkSubtle)
                                )
                            }
                        }
                    }
                }
            }

            // Mini stats en barra inferior de la tarjeta
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CreamBgWarm)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(15.dp))
                    Text(
                        text = "$totalMinutes min activos",
                        color = TextDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.DirectionsWalk, contentDescription = null, tint = StepsTeal, modifier = Modifier.size(15.dp))
                    Text(
                        text = "$todaySteps pasos hoy",
                        color = TextDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * 2. Calories burned tracker with circular ring progress (compact card)
 */
@Composable
private fun CaloriesBurnedRingCard(
    burned: Double,
    target: Double,
    consumed: Double,
    net: Double
) {
    val progress = if (target > 0) (burned / target).toFloat().coerceIn(0f, 1f) else 0f
    val percentage = (progress * 100).toInt()

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = CreamSurface,
        border = BorderStroke(1.dp, CreamBorder),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Anillo circular de progreso en Coral Canvas
            Box(
                modifier = Modifier.size(110.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 10.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = Offset(size.width / 2, size.height / 2)

                    // Track circular de fondo
                    drawCircle(
                        color = CreamSurfaceVariant,
                        radius = radius,
                        center = center,
                        style = Stroke(width = strokeWidth)
                    )

                    // Arco de progreso activo en Coral
                    val sweepAngle = progress * 360f
                    if (sweepAngle > 0f) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(AccentCoralLight, AccentCoral, AccentCoralDark)
                            ),
                            startAngle = -90f,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                // Centro del anillo
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = AccentCoral,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "${burned.toInt()}",
                        color = TextDark,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "kcal",
                        color = TextDarkMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Desglose compacto lateral
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Calorías Quemadas",
                        color = TextDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = AccentCoralContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "$percentage%",
                            color = AccentCoralDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "Meta diaria: ${target.toInt()} kcal",
                    color = TextDarkMuted,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Métricas clave
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Consumidas", color = TextDarkMuted, fontSize = 10.sp)
                        Text("${consumed.toInt()} kcal", color = TextDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        val remaining = (target - net).toInt()
                        Text("Restantes", color = TextDarkMuted, fontSize = 10.sp)
                        Text(
                            text = "${remaining.coerceAtLeast(0)} kcal",
                            color = if (remaining >= 0) SuccessGreen else WarningAmber,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * 3. Workout streak counter below the calories card
 */
@Composable
private fun WorkoutStreakCard(
    streakDays: Int,
    bestStreakDays: Int
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = CreamSurface,
        border = BorderStroke(1.dp, CreamBorder),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(WarmFlameContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Racha",
                    tint = WarmFlame,
                    modifier = Modifier.size(32.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$streakDays DÍAS DE RACHA",
                        color = TextDark,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )

                    Surface(
                        color = WarmFlameContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (bestStreakDays > 0) "🏆 Récord: $bestStreakDays d" else "Nuevo comienzo",
                            color = WarmFlame,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    text = if (streakDays > 0) "¡Estás imparable! La constancia diaria es tu mayor ventaja. ¡Sigue así!"
                           else "Comienza tu racha hoy con tu primer entrenamiento o registro de actividad.",
                    color = TextDarkMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

/**
 * Píldoras de acción rápida en estilo wellness moderno
 */
@Composable
private fun WellnessActionPill(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = CreamSurface,
        border = BorderStroke(1.dp, CreamBorder),
        shadowElevation = 2.dp,
        modifier = Modifier.width(135.dp)
    ) {
        Column(
            modifier = Modifier.padding(13.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    color = TextDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    color = TextDarkMuted,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Tarjeta de Báscula y Bioimpedancia en estilo wellness claro
 */
@Composable
private fun SmartScaleWellnessCard(
    latest: BodyCompositionLog?,
    onScanScale: () -> Unit,
    onViewChart: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = CreamSurface,
        border = BorderStroke(1.dp, CreamBorder),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
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
                            .clip(RoundedCornerShape(12.dp))
                            .background(ElectricBlueContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Scale, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(22.dp))
                    }
                    Column {
                        Text(
                            text = "Báscula y Bioimpedancia",
                            color = TextDark,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (latest != null) "Última medición: ${latest.date}" else "Sube una foto de tu báscula",
                            color = TextDarkMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Button(
                    onClick = onScanScale,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Escanear", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (latest != null) {
                // Mini cuadrícula de bioimpedancia
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WellnessScaleTile("Peso", "${latest.weightKg} kg", "IMC ${latest.bmi}", ElectricBlue, Modifier.weight(1f))
                    WellnessScaleTile("% Grasa", "${latest.bodyFatPercentage}%", "Visceral: ${latest.visceralFat}", AccentCoral, Modifier.weight(1f))
                    WellnessScaleTile("Músculo", "${latest.muscleMassKg} kg", "Agua: ${latest.bodyWaterPercentage}%", SuccessGreen, Modifier.weight(1f))
                }

                // Barra segmentada de composición
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                ) {
                    Box(modifier = Modifier.weight(latest.bodyFatPercentage.toFloat().coerceAtLeast(1f)).fillMaxHeight().background(AccentCoral))
                    Box(modifier = Modifier.weight(if (latest.weightKg > 0) (latest.muscleMassKg.toFloat() / latest.weightKg.toFloat() * 100f).coerceAtLeast(1f) else 50f).fillMaxHeight().background(SuccessGreen))
                    Box(modifier = Modifier.weight(if (latest.weightKg > 0) ((latest.boneMassKg.toFloat() / latest.weightKg.toFloat()) * 100f).coerceAtLeast(3f) else 4f).fillMaxHeight().background(ElectricBlue))
                }

                // Botón para ver gráfica completa
                OutlinedButton(
                    onClick = onViewChart,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricBlue),
                    border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.ShowChart, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ver Gráfica y Evolución Completa", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun WellnessScaleTile(
    label: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CreamBgWarm)
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(label, color = TextDarkMuted, fontSize = 10.sp)
            Text(value, color = color, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = TextDarkSubtle, fontSize = 9.sp)
        }
    }
}

/**
 * 4. Weekly workout bar chart at bottom (en Azul Eléctrico y estilo wellness)
 */
@Composable
private fun WeeklyWorkoutBarChartCard(
    weeklyActivity: List<DayActivitySummary>,
    weeklyTotalMinutes: Int
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = CreamSurface,
        border = BorderStroke(1.dp, CreamBorder),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Entrenamientos Semanales",
                            color = TextDark,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Minutos de actividad física por día",
                            color = TextDarkMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(
                    color = ElectricBlueContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "$weeklyTotalMinutes min total",
                        color = ElectricBlueDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Gráfica de barras en Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .padding(vertical = 4.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val barWidth = 24.dp.toPx()
                    val cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    val maxMinutes = 75f
                    val chartHeight = height - 26.dp.toPx()

                    // Línea discontinua de meta (45 min)
                    val targetY = chartHeight * (1f - (45f / maxMinutes))
                    drawLine(
                        color = ElectricBlue.copy(alpha = 0.25f),
                        start = Offset(0f, targetY),
                        end = Offset(width, targetY),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )

                    val count = weeklyActivity.size
                    val spacing = width / count

                    weeklyActivity.forEachIndexed { index, day ->
                        val centerX = (index * spacing) + (spacing / 2)
                        val barHeight = ((day.workoutMinutes.toFloat() / maxMinutes).coerceIn(0f, 1f)) * chartHeight
                        val topY = chartHeight - barHeight

                        // Dibujar barra de fondo gris suave (capacidad máxima)
                        drawRoundRect(
                            color = CreamSurfaceVariant,
                            topLeft = Offset(centerX - (barWidth / 2), 0f),
                            size = Size(barWidth, chartHeight),
                            cornerRadius = cornerRadius
                        )

                        // Dibujar barra activa en Azul Eléctrico
                        if (day.workoutMinutes > 0) {
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    listOf(ElectricBlueLight, ElectricBlue)
                                ),
                                topLeft = Offset(centerX - (barWidth / 2), topY),
                                size = Size(barWidth, barHeight),
                                cornerRadius = cornerRadius
                            )
                        }

                        // Si es hoy, resaltar con un punto en la base
                        if (day.isToday) {
                            drawCircle(
                                color = AccentCoral,
                                radius = 3.dp.toPx(),
                                center = Offset(centerX, chartHeight + 20.dp.toPx())
                            )
                        }
                    }
                }

                // Etiquetas de minutos arriba y días abajo
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    weeklyActivity.forEach { day ->
                        Column(
                            modifier = Modifier.width(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (day.workoutMinutes > 0) "${day.workoutMinutes}'" else "-",
                                color = if (day.isToday) ElectricBlue else TextDarkMuted,
                                fontSize = 9.sp,
                                fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.height(105.dp))
                            Text(
                                text = day.dayKey,
                                color = if (day.isToday) AccentCoral else TextDark,
                                fontSize = 11.sp,
                                fontWeight = if (day.isToday) FontWeight.ExtraBold else FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Pie de gráfica motivacional
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CreamBgWarm)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = ElectricBlue,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Línea punteada: Meta diaria 45 min • ¡Gran ritmo esta semana!",
                    color = TextDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
