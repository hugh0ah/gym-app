package com.fittracker.app.ui.food

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.fittracker.app.data.local.entities.FoodCache
import com.fittracker.app.data.local.entities.FoodLog
import com.fittracker.app.ui.components.MacroProgressBar
import com.fittracker.app.ui.theme.*

import com.fittracker.app.ui.components.InteractiveMacroRing

@Composable
fun FoodScreen(
    viewModel: FoodViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    val filteredLogs = remember(state.logs, state.filterMealType) {
        if (state.filterMealType == "Todos") {
            state.logs
        } else {
            state.logs.filter { it.mealType.equals(state.filterMealType, ignoreCase = true) }
        }
    }

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

            // Selector de fecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.changeDate(-1) }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Día anterior", tint = TextDark)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = AccentCoral, modifier = Modifier.size(18.dp))
                    val todayStr = java.time.LocalDate.now().toString()
                    val yesterdayStr = java.time.LocalDate.now().minusDays(1).toString()
                    val dateLabel = when (state.selectedDate) {
                        todayStr -> "Hoy, ${state.selectedDate}"
                        yesterdayStr -> "Ayer, ${state.selectedDate}"
                        else -> state.selectedDate
                    }
                    Text(
                        text = dateLabel,
                        color = TextDark,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (state.selectedDate != todayStr) {
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

                IconButton(onClick = { viewModel.changeDate(1) }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Día siguiente", tint = TextDark)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Anillo Interactivo de Calorías y Macronutrientes
            InteractiveMacroRing(
                caloriesConsumed = state.macroTotals.totalCalories,
                caloriesTarget = state.userTargets.calorieTarget,
                caloriesBurned = 0.0,
                proteinConsumed = state.macroTotals.totalProtein,
                proteinTarget = state.userTargets.proteinTarget,
                carbsConsumed = state.macroTotals.totalCarbs,
                carbsTarget = state.userTargets.carbTarget,
                fatConsumed = state.macroTotals.totalFat,
                fatTarget = state.userTargets.fatTarget
            )

            // Botón de ajuste de objetivos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { viewModel.openTargetsDialog() },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Editar Metas Nutricionales", color = ElectricCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tarjeta de Seguimiento de Agua (Water Tracker)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(CreamSurface)
                    .border(1.dp, CreamBorder, RoundedCornerShape(18.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ElectricBlue.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WaterDrop,
                                    contentDescription = null,
                                    tint = ElectricBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Hidratación Diaria",
                                    color = TextDark,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                val percent = ((state.waterConsumedMl.toDouble() / state.waterGoalMl.coerceAtLeast(1)) * 100).toInt()
                                Text(
                                    text = "${state.waterConsumedMl} / ${state.waterGoalMl} ml ($percent%)",
                                    color = TextDarkMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Botones de acción rápida de agua
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            FilledTonalButton(
                                onClick = { viewModel.addWater(250) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = ElectricBlue.copy(alpha = 0.12f),
                                    contentColor = ElectricBlue
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("+250ml", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            FilledTonalButton(
                                onClick = { viewModel.addWater(500) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = ElectricBlue.copy(alpha = 0.12f),
                                    contentColor = ElectricBlue
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("+500ml", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            if (state.waterConsumedMl > 0) {
                                IconButton(
                                    onClick = { viewModel.undoWater() },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.Undo, contentDescription = "Deshacer agua", tint = TextDarkMuted, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    val progress = (state.waterConsumedMl.toFloat() / state.waterGoalMl.coerceAtLeast(1)).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = ElectricBlue,
                        trackColor = CreamSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Cabecera con Botón de Foto IA y Añadir Alimento
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Diario (${filteredLogs.size})",
                    color = TextDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = { viewModel.openPhotoAnalyzer() },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = AccentCoralContainer,
                            contentColor = AccentCoralDark
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Foto IA", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.openSearchDialog() },
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

            Spacer(modifier = Modifier.height(6.dp))

            // Filtros de categoría de comida
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Todos", "Desayuno", "Comida", "Cena", "Snack").forEach { cat ->
                    val isSelected = state.filterMealType == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setFilterMeal(cat) },
                        label = { Text(cat, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentCoral,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Lista de alimentos
            if (filteredLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.94f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(CreamSurface)
                            .border(1.dp, CreamBorder, RoundedCornerShape(18.dp))
                            .padding(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestaurantMenu,
                            contentDescription = null,
                            tint = AccentCoral.copy(alpha = 0.7f),
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = if (state.filterMealType == "Todos") "No hay comidas registradas para hoy" else "No hay alimentos en '${state.filterMealType}'",
                            color = TextDark,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Text(
                            text = "Añade alimentos buscando en el catálogo fitness o sacando una foto a tu plato con la IA.",
                            color = TextDarkMuted,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(
                                onClick = { viewModel.openPhotoAnalyzer() },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = AccentCoralContainer,
                                    contentColor = AccentCoralDark
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Foto con IA", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { viewModel.openSearchDialog() },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentCoral),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Buscar Comida", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(filteredLogs, key = { it.id }) { log ->
                        FoodLogCard(
                            log = log,
                            onDelete = { viewModel.deleteFoodLog(log.id) }
                        )
                    }
                }
            }
        }

        // Diálogo de Búsqueda Open Food Facts y Catálogo
        if (state.isSearchDialogOpen) {
            FoodSearchDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.closeSearchDialog() }
            )
        }

        // Diálogo de Análisis de Plato por Foto IA
        if (state.isPhotoAnalyzerDialogOpen) {
            FoodPhotoAnalyzerDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.closePhotoAnalyzer() }
            )
        }

        // Diálogo de Ajuste de Objetivos
        if (state.isTargetsDialogOpen) {
            UserTargetsDialog(
                currentTargets = state.userTargets,
                onDismiss = { viewModel.closeTargetsDialog() },
                onSave = { cal, p, c, f -> viewModel.saveTargets(cal, p, c, f) }
            )
        }
    }
}

@Composable
private fun FoodLogCard(
    log: FoodLog,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CreamSurface)
            .border(1.dp, CreamBorder, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = log.foodName,
                        color = TextDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AccentCoral.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = log.mealType,
                            color = AccentCoralDark,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${log.quantityG.toInt()}g  •  ${log.calories.toInt()} kcal",
                    color = AccentCoralDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "P: ${log.protein.toInt()}g", color = ProteinBlue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = "C: ${log.carbs.toInt()}g", color = CarbsYellow, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = "G: ${log.fat.toInt()}g", color = FatPurple, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Eliminar",
                    tint = TextDarkSubtle
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FoodSearchDialog(
    viewModel: FoodViewModel,
    onDismiss: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
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
                        Text(
                            text = "Buscar Alimento",
                            color = TextDark,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Catálogo Fitness & Open Food Facts",
                            color = TextDarkMuted,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextDarkMuted)
                    }
                }

                // Campo de búsqueda
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = { Text("Ej: Pechuga, Avena, Yogur, Atún...", color = TextDarkSubtle) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AccentCoral) },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar", tint = TextDarkMuted)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (state.isSearching) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = AccentCoral,
                        trackColor = CreamSurfaceVariant
                    )
                }

                // Selector de porción y categoría cuando se elige un alimento
                state.selectedFoodForAdd?.let { food ->
                    ServingSelectorCard(
                        food = food,
                        quantityG = state.quantityInputG,
                        selectedMealType = state.selectedMealType,
                        onMealTypeChanged = { viewModel.setSelectedMealType(it) },
                        onQuantityChanged = { viewModel.updateQuantityInput(it) },
                        onConfirm = { viewModel.confirmAddFood() },
                        onCancel = { viewModel.selectFoodToAdd(food) }
                    )
                }

                // Lista de resultados
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.searchResults, key = { it.code }) { food ->
                        SearchResultItem(
                            food = food,
                            isSelected = state.selectedFoodForAdd?.code == food.code,
                            onSelect = { viewModel.selectFoodToAdd(food) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    food: FoodCache,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
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
            .clickable { onSelect() }
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = food.name,
                    color = TextDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${food.caloriesPer100g.toInt()} kcal / 100g",
                    color = AccentCoralDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (!food.brand.isNullOrBlank()) {
                Text(text = food.brand, color = TextDarkMuted, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "Prot: ${food.proteinPer100g.toInt()}g", color = ProteinBlue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text(text = "Carb: ${food.carbsPer100g.toInt()}g", color = CarbsYellow, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text(text = "Gras: ${food.fatPer100g.toInt()}g", color = FatPurple, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ServingSelectorCard(
    food: FoodCache,
    quantityG: String,
    selectedMealType: String,
    onMealTypeChanged: (String) -> Unit,
    onQuantityChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val grams = quantityG.toDoubleOrNull() ?: 100.0
    val factor = grams / 100.0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CreamSurfaceVariant)
            .border(1.dp, CreamBorder, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Añadir: ${food.name}",
                color = TextDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            // Selector de momento del día
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Desayuno", "Comida", "Cena", "Snack").forEach { meal ->
                    val isSelected = selectedMealType == meal
                    FilterChip(
                        selected = isSelected,
                        onClick = { onMealTypeChanged(meal) },
                        label = { Text(meal, fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentCoral,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = quantityG,
                    onValueChange = onQuantityChanged,
                    label = { Text("Gramos") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(105.dp),
                    singleLine = true
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Total: ${(food.caloriesPer100g * factor).toInt()} kcal",
                        color = AccentCoralDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "P: ${(food.proteinPer100g * factor).toInt()}g • C: ${(food.carbsPer100g * factor).toInt()}g • G: ${(food.fatPer100g * factor).toInt()}g",
                        color = TextDarkMuted,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCoral),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Añadir", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun UserTargetsDialog(
    currentTargets: com.fittracker.app.data.local.entities.UserTargets,
    onDismiss: () -> Unit,
    onSave: (calories: Double, protein: Double, carbs: Double, fat: Double) -> Unit
) {
    var cal by remember { mutableStateOf(currentTargets.calorieTarget.toInt().toString()) }
    var prot by remember { mutableStateOf(currentTargets.proteinTarget.toInt().toString()) }
    var carbs by remember { mutableStateOf(currentTargets.carbTarget.toInt().toString()) }
    var fat by remember { mutableStateOf(currentTargets.fatTarget.toInt().toString()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.90f)
                .clip(RoundedCornerShape(22.dp)),
            color = CreamSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Objetivos Diarios",
                        color = TextDark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextDarkMuted)
                    }
                }

                OutlinedTextField(
                    value = cal,
                    onValueChange = { cal = it },
                    label = { Text("Calorías (kcal)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = prot,
                    onValueChange = { prot = it },
                    label = { Text("Proteína (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = carbs,
                    onValueChange = { carbs = it },
                    label = { Text("Carbohidratos (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = fat,
                    onValueChange = { fat = it },
                    label = { Text("Grasas (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancelar", color = TextDark)
                    }

                    Button(
                        onClick = {
                            val c = cal.toDoubleOrNull() ?: currentTargets.calorieTarget
                            val p = prot.toDoubleOrNull() ?: currentTargets.proteinTarget
                            val cb = carbs.toDoubleOrNull() ?: currentTargets.carbTarget
                            val f = fat.toDoubleOrNull() ?: currentTargets.fatTarget
                            onSave(c, p, cb, f)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCoral),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Guardar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
