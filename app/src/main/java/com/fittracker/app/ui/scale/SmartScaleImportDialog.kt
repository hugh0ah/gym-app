package com.fittracker.app.ui.scale

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.vector.ImageVector
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

@Composable
fun SmartScaleImportDialog(
    viewModel: SmartScaleViewModel,
    onDismiss: () -> Unit,
    onSuccessSaved: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
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

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
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
                            Icon(Icons.Default.Scale, contentDescription = null, tint = AccentCoral, modifier = Modifier.size(24.dp))
                        }
                        Column {
                            Text(
                                text = "Báscula Inteligente",
                                color = TextDark,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Escaneo con Gemini Vision IA",
                                color = TextDarkMuted,
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextDarkMuted)
                    }
                }

                // Banner de error si existe
                if (state.errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(20.dp))
                            Text(state.errorMessage ?: "", color = ErrorRed, fontSize = 13.sp)
                        }
                    }
                }

                // Zona de selección de imagen
                if (state.selectedImageUri == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(CreamBg)
                            .border(1.dp, CreamBorder, RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(AccentCoral.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = AccentCoral,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Text(
                                text = "Sube una captura de tu báscula",
                                color = TextDark,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Compatible con Huawei Scale, Xiaomi, Renpho, Garmin, Withings, etc.",
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
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1.3f),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp)
                                ) {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Galería / Álbum", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }

                                OutlinedButton(
                                    onClick = { openGooglePhotosPicker() },
                                    shape = RoundedCornerShape(12.dp),
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
                                    shape = RoundedCornerShape(12.dp),
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
                    // Vista previa y botón para cambiar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(CreamBg)
                            .border(1.dp, CreamBorder, RoundedCornerShape(14.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AsyncImage(
                            model = state.selectedImageUri,
                            contentDescription = "Captura seleccionada",
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Captura de Báscula", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                if (state.isAnalyzing) "Gemini está analizando los valores..." else "Valores extraídos con éxito",
                                color = if (state.isAnalyzing) CalorieOrange else SuccessGreen,
                                fontSize = 12.sp
                            )
                        }
                        OutlinedButton(
                            onClick = { openGalleryPicker() },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCoral),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CreamBorder),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("Cambiar", fontSize = 12.sp)
                        }
                    }
                }

                // Si está analizando
                if (state.isAnalyzing) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            CircularProgressIndicator(color = AccentCoral)
                            Text(
                                text = "Extrayendo grasa, masa muscular, agua, TMB y bioimpedancia...",
                                color = TextDarkMuted,
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                // Formulario de los 9 parámetros detectados (editable por el usuario)
                if (!state.isAnalyzing && (state.extractedResult != null || state.inputWeightKg.isNotBlank())) {
                    Text(
                        text = "Parámetros de Composición Corporal",
                        color = TextDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Fila 1: Peso y IMC
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricInputField(
                            label = "Peso (kg)",
                            value = state.inputWeightKg,
                            onValueChange = viewModel::updateWeight,
                            icon = Icons.Default.MonitorWeight,
                            color = AccentCoral,
                            modifier = Modifier.weight(1f)
                        )
                        MetricInputField(
                            label = "IMC",
                            value = state.inputBmi,
                            onValueChange = viewModel::updateBmi,
                            icon = Icons.Default.Straighten,
                            color = ElectricBlue,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Fila 2: % Grasa corporal y Masa muscular en kg
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricInputField(
                            label = "% Grasa Corporal",
                            value = state.inputBodyFatPercentage,
                            onValueChange = viewModel::updateBodyFat,
                            icon = Icons.Default.PieChart,
                            color = CalorieOrange,
                            modifier = Modifier.weight(1f)
                        )
                        MetricInputField(
                            label = "Masa Muscular (kg)",
                            value = state.inputMuscleMassKg,
                            onValueChange = viewModel::updateMuscleMass,
                            icon = Icons.Default.FitnessCenter,
                            color = ElectricBlue,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Fila 3: Grasa visceral y TMB (kcal)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricInputField(
                            label = "Grasa Visceral (1-20)",
                            value = state.inputVisceralFat,
                            onValueChange = viewModel::updateVisceralFat,
                            icon = Icons.Default.WarningAmber,
                            color = WarningAmber,
                            modifier = Modifier.weight(1f)
                        )
                        MetricInputField(
                            label = "TMB Báscula (kcal)",
                            value = state.inputBmrKcal,
                            onValueChange = viewModel::updateBmr,
                            icon = Icons.Default.LocalFireDepartment,
                            color = AccentCoral,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Fila 4: % Agua corporal, Masa ósea y % Proteínas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricInputField(
                            label = "% Agua",
                            value = state.inputBodyWaterPercentage,
                            onValueChange = viewModel::updateBodyWater,
                            icon = Icons.Default.WaterDrop,
                            color = ElectricBlue,
                            modifier = Modifier.weight(1f)
                        )
                        MetricInputField(
                            label = "Masa Ósea (kg)",
                            value = state.inputBoneMassKg,
                            onValueChange = viewModel::updateBoneMass,
                            icon = Icons.Default.Shield,
                            color = FatPurple,
                            modifier = Modifier.weight(1f)
                        )
                        MetricInputField(
                            label = "% Proteína",
                            value = state.inputProteinPercentage,
                            onValueChange = viewModel::updateProtein,
                            icon = Icons.Default.Egg,
                            color = ProteinBlue,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Fecha y notas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = state.inputDate,
                            onValueChange = viewModel::updateDate,
                            label = { Text("Fecha (YYYY-MM-DD)", fontSize = 11.sp) },
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
                            value = state.inputNotes,
                            onValueChange = viewModel::updateNotes,
                            label = { Text("Notas", fontSize = 11.sp) },
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

                    Spacer(modifier = Modifier.height(6.dp))

                    // Botón guardar
                    Button(
                        onClick = {
                            viewModel.confirmAndSave {
                                onSuccessSaved()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCoral)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Guardar Medición de Báscula",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 11.sp, color = color) },
        leadingIcon = {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = modifier,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextDark,
            unfocusedTextColor = TextDark,
            focusedBorderColor = color,
            unfocusedBorderColor = CreamBorder
        )
    )
}
