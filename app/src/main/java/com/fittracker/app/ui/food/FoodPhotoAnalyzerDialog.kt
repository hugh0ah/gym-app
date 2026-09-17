package com.fittracker.app.ui.food

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.fittracker.app.ui.theme.*

@Composable
fun FoodPhotoAnalyzerDialog(
    viewModel: FoodViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    var selectedImageUri by remember { mutableStateOf<Uri?>(state.selectedPhotoUri) }
    var foodNameInput by remember { mutableStateOf("") }
    var portionGramsInput by remember { mutableStateOf("250") }
    var caloriesInput by remember { mutableStateOf("350") }
    var proteinInput by remember { mutableStateOf("25") }
    var carbsInput by remember { mutableStateOf("30") }
    var fatInput by remember { mutableStateOf("10") }
    var selectedMeal by remember { mutableStateOf(state.selectedMealType) }

    LaunchedEffect(state.analyzedFoodResult) {
        val res = state.analyzedFoodResult
        if (res != null) {
            foodNameInput = res.foodName
            portionGramsInput = res.portionGrams.toInt().toString()
            caloriesInput = res.calories.toInt().toString()
            proteinInput = res.protein.toInt().toString()
            carbsInput = res.carbs.toInt().toString()
            fatInput = res.fat.toInt().toString()
            selectedMeal = res.mealType
        }
    }

    LaunchedEffect(state.selectedPhotoUri) {
        if (state.selectedPhotoUri != null) {
            selectedImageUri = state.selectedPhotoUri
        }
    }

    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.data
        if (uri != null) {
            selectedImageUri = uri
            viewModel.onPhotoSelected(context, uri)
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            viewModel.onPhotoSelected(context, uri)
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            viewModel.onPhotoSelected(context, uri)
        }
    }

    fun openPicker() {
        try {
            val intent = android.content.Intent(
                android.content.Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            galleryPickerLauncher.launch(intent)
        } catch (_: Exception) {
            try {
                photoPickerLauncher.launch(
                    androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            } catch (_: Exception) {
                filePickerLauncher.launch("image/*")
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(26.dp)),
            color = CreamSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
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
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AccentCoralContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = AccentCoral,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Analizar Plato con IA",
                                color = TextDark,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Reconocimiento y cálculo nutricional",
                                color = TextDarkMuted,
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CreamSurfaceVariant)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextDark, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Selector / Visor de imagen
                    if (selectedImageUri == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(CreamSurfaceVariant)
                                .border(1.5.dp, AccentCoral.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                                .clickable { openPicker() },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(AccentCoral.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = null,
                                        tint = AccentCoral,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                                Text(
                                    text = "Toca aquí para seleccionar una foto de tu comida",
                                    color = TextDark,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Text(
                                    text = "Sube un plato, tupper o preparación casera",
                                    color = TextDarkMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    } else {
                        // Vista previa de la foto seleccionada
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .border(1.dp, CreamBorder, RoundedCornerShape(18.dp))
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(selectedImageUri),
                                contentDescription = "Foto de comida",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Botón para cambiar foto
                            FilledTonalButton(
                                onClick = { openPicker() },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(10.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = CreamSurface.copy(alpha = 0.9f),
                                    contentColor = TextDark
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cambiar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Estado de carga / análisis IA
                    if (state.isAnalyzingFoodPhoto) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(CreamSurfaceVariant)
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(color = AccentCoral, strokeWidth = 3.dp)
                                Text(
                                    text = "Analizando plato con Gemini Vision...",
                                    color = TextDark,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Identificando ingredientes y calculando calorías y macros",
                                    color = TextDarkMuted,
                                    fontSize = 12.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    // Error si ocurre
                    if (state.foodPhotoError != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFFDE8E8))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = state.foodPhotoError ?: "Error al procesar la foto.",
                                color = Color(0xFFC81E1E),
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Resultado y formulario interactivo
                    if (state.analyzedFoodResult != null && !state.isAnalyzingFoodPhoto) {
                        val result = state.analyzedFoodResult!!

                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = CreamSurfaceVariant),
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
                                    Text(
                                        text = "Resultado Detectado ✨",
                                        color = AccentCoral,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (result.confidenceNotes.isNotBlank()) {
                                        Text(
                                            text = result.confidenceNotes,
                                            color = TextDarkMuted,
                                            fontSize = 11.sp,
                                            maxLines = 1
                                        )
                                    }
                                }

                                // Campo Nombre del plato
                                OutlinedTextField(
                                    value = foodNameInput,
                                    onValueChange = { foodNameInput = it },
                                    label = { Text("Nombre del Plato") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AccentCoral,
                                        unfocusedBorderColor = CreamBorder,
                                        focusedContainerColor = CreamSurface,
                                        unfocusedContainerColor = CreamSurface
                                    )
                                )

                                // Selección de momento del día
                                Text(
                                    text = "Momento del día:",
                                    color = TextDark,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("Desayuno", "Comida", "Cena", "Snack").forEach { meal ->
                                        val isSelected = selectedMeal == meal
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { selectedMeal = meal },
                                            label = { Text(meal, fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = AccentCoral,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }

                                // Badges de Macros Detectados
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    MacroBadge(label = "Calorías", value = "${caloriesInput} kcal", color = AccentCoral)
                                    MacroBadge(label = "Proteína", value = "${proteinInput} g", color = ProteinBlue)
                                    MacroBadge(label = "Carbos", value = "${carbsInput} g", color = CarbsYellow)
                                    MacroBadge(label = "Grasas", value = "${fatInput} g", color = FatPurple)
                                }

                                // Campos editables numéricos (Ración g, Calorías, P, C, G)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = portionGramsInput,
                                        onValueChange = { portionGramsInput = it },
                                        label = { Text("Porción (g)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AccentCoral,
                                            unfocusedBorderColor = CreamBorder,
                                            focusedContainerColor = CreamSurface,
                                            unfocusedContainerColor = CreamSurface
                                        )
                                    )
                                    OutlinedTextField(
                                        value = caloriesInput,
                                        onValueChange = { caloriesInput = it },
                                        label = { Text("Kcal") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AccentCoral,
                                            unfocusedBorderColor = CreamBorder,
                                            focusedContainerColor = CreamSurface,
                                            unfocusedContainerColor = CreamSurface
                                        )
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = proteinInput,
                                        onValueChange = { proteinInput = it },
                                        label = { Text("Proteína (g)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = ProteinBlue,
                                            unfocusedBorderColor = CreamBorder,
                                            focusedContainerColor = CreamSurface,
                                            unfocusedContainerColor = CreamSurface
                                        )
                                    )
                                    OutlinedTextField(
                                        value = carbsInput,
                                        onValueChange = { carbsInput = it },
                                        label = { Text("Carbos (g)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CarbsYellow,
                                            unfocusedBorderColor = CreamBorder,
                                            focusedContainerColor = CreamSurface,
                                            unfocusedContainerColor = CreamSurface
                                        )
                                    )
                                    OutlinedTextField(
                                        value = fatInput,
                                        onValueChange = { fatInput = it },
                                        label = { Text("Grasas (g)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = FatPurple,
                                            unfocusedBorderColor = CreamBorder,
                                            focusedContainerColor = CreamSurface,
                                            unfocusedContainerColor = CreamSurface
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Footer Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextDark)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            val cal = caloriesInput.toDoubleOrNull() ?: 350.0
                            val p = proteinInput.toDoubleOrNull() ?: 25.0
                            val c = carbsInput.toDoubleOrNull() ?: 30.0
                            val f = fatInput.toDoubleOrNull() ?: 10.0
                            val grams = portionGramsInput.toDoubleOrNull() ?: 250.0
                            val name = foodNameInput.ifBlank { "Plato analizado" }

                            viewModel.confirmAddAnalyzedFood(
                                foodName = name,
                                calories = cal,
                                protein = p,
                                carbs = c,
                                fat = f,
                                quantityG = grams,
                                mealType = selectedMeal
                            )
                        },
                        enabled = state.analyzedFoodResult != null && !state.isAnalyzingFoodPhoto,
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCoral)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Añadir al Diario", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroBadge(
    label: String,
    value: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, color = TextDarkMuted, fontSize = 10.sp)
            Text(text = value, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}
