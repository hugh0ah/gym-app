package com.fittracker.app.ui.wearable

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import coil.compose.AsyncImage
import com.fittracker.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WearableImportDialog(
    viewModel: WearableViewModel,
    onDismiss: () -> Unit,
    onSuccessSaved: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.onImageSelected(context, uri)
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.onImageSelected(context, uri)
        }
    }

    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.data
        if (uri != null) {
            viewModel.onImageSelected(context, uri)
        }
    }

    fun openNativeGallery() {
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

    fun openGooglePhotosPicker() {
        try {
            photoPickerLauncher.launch(
                androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } catch (_: Exception) {
            openNativeGallery()
        }
    }

    fun openGalleryPicker() {
        openNativeGallery()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(24.dp)),
            color = CreamSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
            tonalElevation = 6.dp
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
                            Icon(
                                imageVector = Icons.Default.Watch,
                                contentDescription = null,
                                tint = AccentCoral,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Huawei Watch Fit 3",
                                color = TextDark,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Importar captura con Gemini Visión",
                                color = TextDarkMuted,
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = TextDarkMuted
                        )
                    }
                }

                HorizontalDivider(color = CreamBorder)

                // Botón selector de imagen
                if (state.selectedImageUri == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(CreamBg)
                            .border(
                                1.dp,
                                CreamBorder,
                                RoundedCornerShape(16.dp)
                            )
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = AccentCoral,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Sube la captura de Huawei Health",
                                color = TextDark,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Gemini detectará pasos, calorías activas, minutos de cardio y tipo de actividad.",
                                color = TextDarkMuted,
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = { openNativeGallery() },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentCoral),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1.3f),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp)
                                ) {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Galería / Álbum", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { openGooglePhotosPicker() },
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricBlue),
                                    modifier = Modifier.weight(0.9f),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp)
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Fotos", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }

                                OutlinedButton(
                                    onClick = { filePickerLauncher.launch("image/*") },
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextDarkMuted),
                                    modifier = Modifier.weight(0.9f),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp)
                                ) {
                                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Archivos", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                } else {
                    // Previsualización y estado de análisis
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CreamBg)
                            .border(1.dp, CreamBorder, RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AsyncImage(
                            model = state.selectedImageUri,
                            contentDescription = "Captura",
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Captura cargada",
                                color = TextDark,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (state.isAnalyzing) "Gemini está interpretando los widgets..." else "Análisis completado",
                                color = if (state.isAnalyzing) CalorieOrange else SuccessGreen,
                                fontSize = 12.sp
                            )
                        }
                        TextButton(onClick = { openGalleryPicker() }) {
                            Text("Cambiar", color = AccentCoral, fontSize = 12.sp)
                        }
                    }
                }

                // Indicador de carga de Gemini
                if (state.isAnalyzing) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(color = AccentCoral)
                            Text(
                                text = "Interpretando datos de Huawei Health...",
                                color = TextDark,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Error
                if (state.errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ErrorRed.copy(alpha = 0.12f))
                            .border(1.dp, ErrorRed.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = state.errorMessage ?: "",
                            color = ErrorRed,
                            fontSize = 12.sp
                        )
                    }
                }

                // Formulario Editable de Confirmación (Requisito clave de la especificación)
                if (state.selectedImageUri != null && !state.isAnalyzing) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CreamBg)
                            .border(1.dp, CreamBorder, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Revisa y corrige los valores detectados antes de guardar en Room:",
                                color = TextDarkMuted,
                                fontSize = 12.sp
                            )
                        }
                    }

                    OutlinedTextField(
                        value = state.inputDate,
                        onValueChange = { viewModel.updateDate(it) },
                        label = { Text("Fecha (AAAA-MM-DD)", color = TextDarkMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = outlinedFieldColors(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = state.inputSteps,
                        onValueChange = { viewModel.updateSteps(it) },
                        label = { Text("Pasos totales", color = TextDarkMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = outlinedFieldColors(),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.DirectionsWalk, contentDescription = null, tint = ElectricBlue)
                        }
                    )

                    OutlinedTextField(
                        value = state.inputActiveCalories,
                        onValueChange = { viewModel.updateActiveCalories(it) },
                        label = { Text("Calorías activas quemadas (kcal)", color = TextDarkMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = outlinedFieldColors(),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = AccentCoral)
                        }
                    )

                    OutlinedTextField(
                        value = state.inputExerciseMinutes,
                        onValueChange = { viewModel.updateExerciseMinutes(it) },
                        label = { Text("Tiempo de ejercicio (minutos)", color = TextDarkMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = outlinedFieldColors(),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = CalorieOrange)
                        }
                    )

                    OutlinedTextField(
                        value = state.inputExerciseType,
                        onValueChange = { viewModel.updateExerciseType(it) },
                        label = { Text("Tipo de actividad principal", color = TextDarkMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = outlinedFieldColors(),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = ElectricBlue)
                        }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextDarkMuted)
                        ) {
                            Text("Cancelar")
                        }

                        Button(
                            onClick = {
                                viewModel.confirmAndSave {
                                    onSuccessSaved()
                                }
                            },
                            modifier = Modifier.weight(1.4f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCoral)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Confirmar y Guardar", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextDark,
    unfocusedTextColor = TextDark,
    focusedBorderColor = AccentCoral,
    unfocusedBorderColor = CreamBorder,
    focusedLabelColor = AccentCoral,
    unfocusedLabelColor = TextDarkMuted,
    focusedContainerColor = CreamSurface,
    unfocusedContainerColor = CreamSurface
)
