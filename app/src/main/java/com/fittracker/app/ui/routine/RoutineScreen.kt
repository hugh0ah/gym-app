package com.fittracker.app.ui.routine

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.fittracker.app.data.local.dao.RoutineExerciseWithDetails
import com.fittracker.app.data.local.entities.Exercise
import com.fittracker.app.ui.components.*
import com.fittracker.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineScreen(
    viewModel: RoutineViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val days = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")

    // Estado del diálogo de temporizador de descanso
    var activeRestExercise by remember { mutableStateOf<RoutineExerciseWithDetails?>(null) }
    var active1RMWeight by remember { mutableStateOf(80.0) }
    var active1RMReps by remember { mutableStateOf(8) }
    var activePlateWeight by remember { mutableStateOf(80.0) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CreamBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Selector de rutina y botón de creación
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                var expanded by remember { mutableStateOf(false) }
                val currentRoutineName = state.routines.find { it.id == state.selectedRoutineId }?.name ?: "Selecciona Rutina"

                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(CreamSurface)
                            .border(1.dp, CreamBorder, RoundedCornerShape(12.dp))
                            .clickable { expanded = true }
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = currentRoutineName,
                            color = TextDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = AccentCoral)
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(CreamSurface)
                    ) {
                        state.routines.forEach { routine ->
                            DropdownMenuItem(
                                text = { Text(routine.name, color = TextDark, fontWeight = FontWeight.Medium) },
                                onClick = {
                                    viewModel.selectRoutine(routine.id)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { viewModel.openPresetRoutines() },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ElectricBlueContainer)
                            .border(1.dp, ElectricBlue.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = "Rutinas OpenGym", tint = ElectricBlue, modifier = Modifier.size(20.dp))
                    }

                    IconButton(
                        onClick = { viewModel.openCreateRoutineDialog() },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AccentCoral)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Nueva Rutina", tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Barra de Herramientas OpenGym (Rutinas, 1RM, Discos, Biblioteca 1.350+)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OpenGymToolChip(
                    title = "Biblioteca (1.350+)",
                    icon = Icons.Default.Search,
                    color = AccentCoral,
                    onClick = { viewModel.openExerciseExplorer() }
                )
                OpenGymToolChip(
                    title = "Calculadora 1RM",
                    icon = Icons.Default.Calculate,
                    color = ElectricBlue,
                    onClick = {
                        active1RMWeight = 80.0
                        active1RMReps = 8
                        viewModel.openOneRepMaxDialog()
                    }
                )
                OpenGymToolChip(
                    title = "Cálculo Discos",
                    icon = Icons.Default.Layers,
                    color = WarmFlame,
                    onClick = {
                        activePlateWeight = 80.0
                        viewModel.openPlateCalculator()
                    }
                )
                OpenGymToolChip(
                    title = "Rutinas PPL / Torso",
                    icon = Icons.Default.FitnessCenter,
                    color = ElectricBlue,
                    onClick = { viewModel.openPresetRoutines() }
                )
                OpenGymToolChip(
                    title = "Sobrecarga Progresiva",
                    icon = Icons.Default.TrendingUp,
                    color = AccentCoral,
                    onClick = { viewModel.openProgressionDialog() }
                )
                OpenGymToolChip(
                    title = "Volumen Muscular",
                    icon = Icons.Default.PieChart,
                    color = ElectricBlue,
                    onClick = { viewModel.openMuscleVolumeDialog() }
                )
                OpenGymToolChip(
                    title = "Consistencia Anual",
                    icon = Icons.Default.CalendarToday,
                    color = WarmFlame,
                    onClick = { viewModel.openWorkoutHeatmapDialog() }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Selector de días de la semana
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                days.forEachIndexed { index, dayName ->
                    val dayNumber = index + 1
                    val isSelected = state.selectedDayOfWeek == dayNumber
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) AccentCoral else CreamSurface)
                            .border(
                                1.dp,
                                if (isSelected) AccentCoral else CreamBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.selectDay(dayNumber) }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = dayName,
                            color = if (isSelected) Color.White else TextDarkMuted,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Cabecera de ejercicios del día
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ejercicios (${state.exercisesForDay.size})",
                    color = TextDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(
                        onClick = { viewModel.openExerciseExplorer() },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = ElectricBlueContainer,
                            contentColor = ElectricBlue
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Explorar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { viewModel.openAddExerciseDialog() },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCoral),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Añadir", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Lista de ejercicios del día
            if (state.exercisesForDay.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(CreamSurface)
                            .border(1.dp, CreamBorder, RoundedCornerShape(18.dp))
                            .padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = AccentCoral.copy(alpha = 0.7f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No hay ejercicios para este día",
                            color = TextDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Añade ejercicios a tu propio ritmo para ir registrando tus entrenamientos poco a poco.",
                            color = TextDarkMuted,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(
                                onClick = { viewModel.openAddExerciseDialog() },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = AccentCoralContainer,
                                    contentColor = AccentCoralDark
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Añadir ejercicio", fontWeight = FontWeight.Bold)
                            }
                            FilledTonalButton(
                                onClick = { viewModel.openPresetRoutines() },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = ElectricBlueContainer,
                                    contentColor = ElectricBlueDark
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Cargar PPL", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(state.exercisesForDay, key = { it.id }) { item ->
                        RoutineExerciseCard(
                            item = item,
                            onLogClick = { viewModel.openWorkoutLogger(item) },
                            onRestClick = { activeRestExercise = item },
                            on1RMClick = {
                                active1RMWeight = if (item.weight > 0) item.weight else 80.0
                                active1RMReps = if (item.reps > 0) item.reps else 8
                                viewModel.openOneRepMaxDialog()
                            },
                            onPlateClick = {
                                activePlateWeight = if (item.weight > 0) item.weight else 80.0
                                viewModel.openPlateCalculator()
                            },
                            onDelete = { viewModel.deleteRoutineExercise(item.id) }
                        )
                    }
                }
            }
        }

        // Diálogo para añadir ejercicio a la rutina
        if (state.isAddExerciseDialogOpen) {
            AddExerciseToRoutineDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.closeAddExerciseDialog() }
            )
        }

        // Diálogo de Explorador OpenGym (1.350+ ejercicios)
        if (state.isExerciseExplorerDialogOpen) {
            ExerciseExplorerDialog(
                allExercises = state.allExercises,
                onDismiss = { viewModel.closeExerciseExplorer() },
                onSelectExerciseToAdd = { ex ->
                    viewModel.addExerciseToCurrentDay(ex.id, 4, 10, 0.0)
                },
                onOpen1RMCalculator = { ex ->
                    active1RMWeight = 80.0
                    active1RMReps = 8
                    viewModel.openOneRepMaxDialog(ex)
                }
            )
        }

        // Diálogo de Calculadora 1RM (OpenGym)
        if (state.isOneRepMaxDialogOpen) {
            OneRepMaxDialog(
                initialWeight = active1RMWeight,
                initialReps = active1RMReps,
                onDismiss = { viewModel.closeOneRepMaxDialog() }
            )
        }

        // Diálogo de Calculadora de Discos de Barra (OpenGym)
        if (state.isPlateCalculatorDialogOpen) {
            PlateCalculatorDialog(
                initialTargetWeight = activePlateWeight,
                onDismiss = { viewModel.closePlateCalculator() }
            )
        }

        // Diálogo de Rutinas Predefinidas (OpenGym)
        if (state.isPresetRoutinesDialogOpen) {
            PresetRoutinesDialog(
                onDismiss = { viewModel.closePresetRoutines() },
                onImportRoutine = { preset ->
                    viewModel.importPresetRoutine(preset)
                }
            )
        }

        // Diálogo de Sobrecarga Progresiva (OpenGym)
        if (state.isProgressionDialogOpen) {
            ProgressionOverloadDialog(
                initialWeight = active1RMWeight,
                initialReps = active1RMReps,
                onDismiss = { viewModel.closeProgressionDialog() }
            )
        }

        // Diálogo de Volumen Muscular Semanal (OpenGym)
        if (state.isMuscleVolumeDialogOpen) {
            MuscleVolumeDialog(
                routineExercises = state.exercisesForDay,
                onDismiss = { viewModel.closeMuscleVolumeDialog() }
            )
        }

        // Diálogo de Mapa de Calor de Entrenamientos (OpenGym)
        if (state.isWorkoutHeatmapDialogOpen) {
            WorkoutHeatmapDialog(
                workoutDates = state.workoutDates,
                onDismiss = { viewModel.closeWorkoutHeatmapDialog() }
            )
        }

        // Diálogo para registrar sesión real de entrenamiento
        if (state.isLogWorkoutDialogOpen && state.selectedExerciseForLog != null) {
            val exToLog = state.selectedExerciseForLog!!
            WorkoutSessionLogDialog(
                exercise = exToLog,
                onDismiss = { viewModel.closeWorkoutLogger() },
                onConfirm = { sets, reps, weight, notes ->
                    viewModel.confirmWorkoutLog(sets, reps, weight, notes)
                    // Abrir temporizador de descanso tras registrar la serie
                    activeRestExercise = exToLog
                }
            )
        }

        // Temporizador de descanso activo
        activeRestExercise?.let { ex ->
            RestTimerDialog(
                exerciseName = ex.exerciseName,
                muscleGroup = ex.muscleGroup,
                reps = ex.reps,
                weight = ex.weight,
                onDismiss = { activeRestExercise = null }
            )
        }

        // Diálogo de crear nueva rutina
        if (state.isCreateRoutineDialogOpen) {
            var newRoutineName by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { viewModel.closeCreateRoutineDialog() },
                containerColor = CreamSurface,
                title = { Text("Nueva Rutina", color = TextDark, fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = newRoutineName,
                        onValueChange = { newRoutineName = it },
                        label = { Text("Nombre (ej. Push Pull Legs)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.createRoutine(newRoutineName) },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCoral),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Crear", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.closeCreateRoutineDialog() }) {
                        Text("Cancelar", color = TextDarkMuted)
                    }
                }
            )
        }
    }
}

@Composable
private fun OpenGymToolChip(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(CreamSurface)
            .border(1.dp, CreamBorder, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 7.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(15.dp))
            Text(text = title, color = TextDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun RoutineExerciseCard(
    item: RoutineExerciseWithDetails,
    onLogClick: () -> Unit,
    onRestClick: () -> Unit,
    on1RMClick: () -> Unit,
    onPlateClick: () -> Unit,
    onDelete: () -> Unit
) {
    val (recSecs, _) = remember(item.exerciseName, item.reps, item.weight) {
        RestRecommendationHelper.getRecommendation(item.exerciseName, item.muscleGroup, item.reps, item.weight)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CreamSurface)
            .border(1.dp, CreamBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.imageUrl != null) {
                        AsyncImage(
                            model = item.imageUrl,
                            contentDescription = item.exerciseName,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CreamSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = AccentCoral)
                        }
                    }

                    Column {
                        Text(
                            text = item.exerciseName,
                            color = TextDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = item.muscleGroup, color = ElectricBlue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "•", color = TextDarkSubtle, fontSize = 11.sp)
                            Text(text = item.equipment, color = TextDarkMuted, fontSize = 11.sp)
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = on1RMClick, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Calculate, contentDescription = "1RM", tint = ElectricBlue, modifier = Modifier.size(17.dp))
                    }
                    IconButton(onClick = onPlateClick, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Layers, contentDescription = "Discos", tint = WarmFlame, modifier = Modifier.size(17.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", tint = TextDarkSubtle, modifier = Modifier.size(17.dp))
                    }
                }
            }

            // Badge con descanso recomendado inteligente
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ElectricBlueContainer)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(14.dp))
                    Text(
                        text = "Descanso sugerido: ${recSecs / 60}:${String.format("%02d", recSecs % 60)} min",
                        color = TextDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = "Iniciar descanso ⏱️",
                    color = ElectricBlueDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onRestClick() }
                )
            }

            HorizontalDivider(color = CreamBorder)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    TargetStatItem(label = "Series", value = "${item.sets}")
                    TargetStatItem(label = "Reps", value = "${item.reps}")
                    TargetStatItem(label = "Peso", value = "${item.weight} kg")
                }

                FilledTonalButton(
                    onClick = onLogClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = AccentCoralContainer,
                        contentColor = AccentCoralDark
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Registrar Serie", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TargetStatItem(label: String, value: String) {
    Column {
        Text(text = label, color = TextDarkMuted, fontSize = 11.sp)
        Text(text = value, color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExerciseToRoutineDialog(
    viewModel: RoutineViewModel,
    onDismiss: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }
    var sets by remember { mutableStateOf("4") }
    var reps by remember { mutableStateOf("10") }
    var weight by remember { mutableStateOf("50") }

    val muscles = listOf("Todos", "Chest", "Back", "Legs", "Shoulders", "Arms", "Core", "Cardio")

    val filteredExercises = state.allExercises.filter { ex ->
        val matchesMuscle = state.selectedMuscleFilter == null ||
                state.selectedMuscleFilter == "Todos" ||
                ex.muscleGroup.equals(state.selectedMuscleFilter, ignoreCase = true)
        val matchesQuery = state.exerciseSearchQuery.isBlank() ||
                ex.name.contains(state.exerciseSearchQuery, ignoreCase = true) ||
                ex.muscleGroup.contains(state.exerciseSearchQuery, ignoreCase = true)
        matchesMuscle && matchesQuery
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(22.dp)),
            color = CreamSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Añadir Ejercicio", color = TextDark, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Catálogo OpenGym (${state.allExercises.size} ejercicios)", color = TextDarkMuted, fontSize = 12.sp)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextDarkMuted)
                    }
                }

                // Buscador
                OutlinedTextField(
                    value = state.exerciseSearchQuery,
                    onValueChange = { viewModel.updateExerciseSearch(it) },
                    placeholder = { Text("Buscar por nombre o músculo...", color = TextDarkSubtle) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AccentCoral) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Chips de grupos musculares
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    muscles.forEach { muscle ->
                        val isSelected = (state.selectedMuscleFilter ?: "Todos") == muscle
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectMuscleFilter(if (muscle == "Todos") null else muscle) },
                            label = { Text(muscle, fontSize = 11.sp, color = if (isSelected) Color.White else TextDarkMuted) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentCoral,
                                selectedLabelColor = Color.White,
                                containerColor = CreamSurfaceVariant
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) AccentCoral else CreamBorder
                            )
                        )
                    }
                }

                // Si se selecciona un ejercicio, se muestran los campos de series/reps/peso objetivo
                if (selectedExercise != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CreamSurfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CreamBorder, RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Configuración para: ${selectedExercise!!.name}",
                                color = AccentCoralDark,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = sets,
                                    onValueChange = { sets = it },
                                    label = { Text("Series") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = reps,
                                    onValueChange = { reps = it },
                                    label = { Text("Reps") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = weight,
                                    onValueChange = { weight = it },
                                    label = { Text("Peso (kg)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1.2f),
                                    singleLine = true
                                )
                            }
                            Button(
                                onClick = {
                                    val setsInt = sets.toIntOrNull() ?: 3
                                    val repsInt = reps.toIntOrNull() ?: 10
                                    val weightDouble = weight.toDoubleOrNull() ?: 0.0
                                    viewModel.addExerciseToCurrentDay(selectedExercise!!.id, setsInt, repsInt, weightDouble)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentCoral),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Confirmar y Añadir a la Rutina", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Lista de ejercicios encontrados
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredExercises) { ex ->
                        val isSelected = selectedExercise?.id == ex.id
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) AccentCoralContainer else CreamSurface)
                                .border(
                                    1.dp,
                                    if (isSelected) AccentCoral else CreamBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedExercise = ex }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = ex.name, color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    Text(text = "${ex.muscleGroup} • ${ex.equipment}", color = TextDarkMuted, fontSize = 11.sp)
                                }
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.AddCircleOutline,
                                    contentDescription = null,
                                    tint = if (isSelected) AccentCoral else TextDarkSubtle
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
private fun WorkoutSessionLogDialog(
    exercise: RoutineExerciseWithDetails,
    onDismiss: () -> Unit,
    onConfirm: (sets: Int, reps: Int, weight: Double, notes: String?) -> Unit
) {
    var actualSets by remember { mutableStateOf(exercise.sets.toString()) }
    var actualReps by remember { mutableStateOf(exercise.reps.toString()) }
    var actualWeight by remember { mutableStateOf(exercise.weight.toString()) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CreamSurface,
        title = {
            Column {
                Text("Registrar Entrenamiento", color = TextDark, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(exercise.exerciseName, color = AccentCoralDark, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Introduce lo que levantaste realmente:", color = TextDarkMuted, fontSize = 12.sp)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = actualSets,
                        onValueChange = { actualSets = it },
                        label = { Text("Series") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = actualReps,
                        onValueChange = { actualReps = it },
                        label = { Text("Reps") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = actualWeight,
                        onValueChange = { actualWeight = it },
                        label = { Text("Peso (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1.2f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Sensaciones / RIR / Notas") },
                    placeholder = { Text("Ej. RIR 2, buena técnica, subir 2.5kg") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val s = actualSets.toIntOrNull() ?: exercise.sets
                    val r = actualReps.toIntOrNull() ?: exercise.reps
                    val w = actualWeight.toDoubleOrNull() ?: exercise.weight
                    onConfirm(s, r, w, notes.ifBlank { null })
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentCoral),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Guardar Serie", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextDarkMuted)
            }
        }
    )
}
