package com.fittracker.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.fittracker.app.data.local.entities.Exercise
import com.fittracker.app.ui.theme.*

@Composable
fun ExerciseExplorerDialog(
    allExercises: List<Exercise>,
    onDismiss: () -> Unit,
    onSelectExerciseToAdd: (Exercise) -> Unit,
    onOpen1RMCalculator: (Exercise) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedMuscle by remember { mutableStateOf("TODOS") }
    var selectedEquipment by remember { mutableStateOf("TODOS") }
    var selectedExerciseDetails by remember { mutableStateOf<Exercise?>(null) }

    val muscles = listOf("TODOS", "Chest", "Back", "Legs", "Shoulders", "Arms", "Core", "Cardio")
    val muscleLabels = mapOf(
        "TODOS" to "Todos",
        "Chest" to "Pecho",
        "Back" to "Espalda",
        "Legs" to "Piernas",
        "Shoulders" to "Hombros",
        "Arms" to "Brazos",
        "Core" to "Abdomen",
        "Cardio" to "Cardio"
    )

    val equipments = listOf("TODOS", "Barbell", "Dumbbell", "Cable", "Bodyweight", "Machine", "Kettlebell")
    val equipmentLabels = mapOf(
        "TODOS" to "Todo equipo",
        "Barbell" to "Barra",
        "Dumbbell" to "Mancuernas",
        "Cable" to "Polea",
        "Bodyweight" to "Peso Corporal",
        "Machine" to "Máquina",
        "Kettlebell" to "Pesa Rusa"
    )

    val filteredExercises = remember(searchQuery, selectedMuscle, selectedEquipment, allExercises) {
        allExercises.filter { ex ->
            val matchQuery = searchQuery.isBlank() || ex.name.contains(searchQuery, ignoreCase = true) || ex.instructions.contains(searchQuery, ignoreCase = true)
            val matchMuscle = selectedMuscle == "TODOS" || ex.muscleGroup.equals(selectedMuscle, ignoreCase = true)
            val matchEq = selectedEquipment == "TODOS" || ex.equipment.equals(selectedEquipment, ignoreCase = true)
            matchQuery && matchMuscle && matchEq
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(26.dp)),
            color = CreamSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                .background(AccentCoralContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = AccentCoral, modifier = Modifier.size(24.dp))
                        }
                        Column {
                            Text(
                                text = "Biblioteca OpenGym",
                                color = TextDark,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${allExercises.size} ejercicios disponibles",
                                color = TextDarkMuted,
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextDarkMuted)
                    }
                }

                // Buscador por texto
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar por nombre (press, curl, dominada...)", fontSize = 13.sp, color = TextDarkMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextDarkMuted) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar", tint = TextDarkMuted)
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark,
                        focusedBorderColor = AccentCoral,
                        unfocusedBorderColor = CreamBorder,
                        focusedContainerColor = CreamBg,
                        unfocusedContainerColor = CreamBg
                    )
                )

                // Filtros de Grupo Muscular
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    muscles.forEach { code ->
                        val label = muscleLabels[code] ?: code
                        val isSelected = selectedMuscle == code
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedMuscle = code },
                            label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentCoral,
                                selectedLabelColor = Color.White,
                                containerColor = CreamSurfaceVariant,
                                labelColor = TextDarkMuted
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = CreamBorder,
                                selectedBorderColor = AccentCoral
                            )
                        )
                    }
                }

                // Filtros de Equipamiento
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    equipments.forEach { code ->
                        val label = equipmentLabels[code] ?: code
                        val isSelected = selectedEquipment == code
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedEquipment = code },
                            label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricBlue,
                                selectedLabelColor = Color.White,
                                containerColor = CreamSurfaceVariant,
                                labelColor = TextDarkMuted
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = CreamBorder,
                                selectedBorderColor = ElectricBlue
                            )
                        )
                    }
                }

                Text(
                    text = "Mostrando ${filteredExercises.size} ejercicios",
                    color = TextDarkMuted,
                    fontSize = 11.sp
                )

                // Lista de ejercicios
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredExercises) { ex ->
                        ExerciseListItemCard(
                            exercise = ex,
                            onClick = { selectedExerciseDetails = ex },
                            onAdd = { onSelectExerciseToAdd(ex) },
                            on1RM = { onOpen1RMCalculator(ex) }
                        )
                    }
                }
            }
        }
    }

    // Modal con detalles completos del ejercicio
    selectedExerciseDetails?.let { ex ->
        ExerciseDetailModal(
            exercise = ex,
            onDismiss = { selectedExerciseDetails = null },
            onAdd = {
                onSelectExerciseToAdd(ex)
                selectedExerciseDetails = null
            },
            on1RM = {
                onOpen1RMCalculator(ex)
                selectedExerciseDetails = null
            }
        )
    }
}

@Composable
private fun ExerciseListItemCard(
    exercise: Exercise,
    onClick: () -> Unit,
    onAdd: () -> Unit,
    on1RM: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CreamSurface)
            .border(1.dp, CreamBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = exercise.name,
                    color = TextDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Badge(containerColor = AccentCoralContainer) {
                        Text(exercise.muscleGroup, color = AccentCoralDark, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Badge(containerColor = ElectricBlueContainer) {
                        Text(exercise.equipment, color = ElectricBlueDark, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = on1RM,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = "1RM", tint = ElectricBlue, modifier = Modifier.size(18.dp))
                }

                IconButton(
                    onClick = onAdd,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AccentCoral)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun ExerciseDetailModal(
    exercise: Exercise,
    onDismiss: () -> Unit,
    onAdd: () -> Unit,
    on1RM: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(22.dp)),
            color = CreamSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = exercise.name,
                        color = TextDark,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextDarkMuted)
                    }
                }

                if (!exercise.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = exercise.imageUrl,
                        contentDescription = exercise.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Badge(containerColor = AccentCoralContainer) {
                        Text("Músculo: ${exercise.muscleGroup}", color = AccentCoralDark, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Badge(containerColor = ElectricBlueContainer) {
                        Text("Equipo: ${exercise.equipment}", color = ElectricBlueDark, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Text("Instrucciones de ejecución:", color = TextDark, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    text = exercise.instructions.ifBlank { "Sin instrucciones detalladas disponibles." },
                    color = TextDarkMuted,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = on1RM,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricBlue)
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Calcular 1RM", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onAdd,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCoral)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Añadir a Rutina", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }
    }
}
