package com.fittracker.app.ui.assistant

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fittracker.app.ui.components.ApiKeySettingsDialog
import com.fittracker.app.ui.components.ConfirmProposalCard
import com.fittracker.app.ui.theme.*

@Composable
fun AssistantScreen(
    viewModel: AssistantViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.onImageSelected(uri)
        }
    }

    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.data
        if (uri != null) {
            viewModel.onImageSelected(uri)
        }
    }

    fun openGallery() {
        try {
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            galleryPickerLauncher.launch(intent)
        } catch (_: Exception) {
            try {
                photoPickerLauncher.launch(
                    androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(state.messages.size, state.isLoading) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CreamBg)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Cabecera moderna en CreamSurface
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CreamSurface)
                    .border(
                        width = 1.dp,
                        color = CreamBorder,
                        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(AccentCoralContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = AccentCoral,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Entrenador y Asistente IA",
                            color = TextDark,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Acciones en base de datos + Wearable",
                            color = ElectricBlueDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                IconButton(
                    onClick = { viewModel.openApiKeyDialog() },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CreamSurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = "Configurar API Key",
                        tint = TextDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Lista de mensajes
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(state.messages, key = { it.id }) { message ->
                    ChatMessageItem(
                        message = message,
                        onAcceptProposal = { proposal ->
                            viewModel.acceptProposal(message.id, proposal)
                        },
                        onDismissProposal = {
                            viewModel.dismissProposal(message.id)
                        }
                    )
                }

                if (state.isLoading) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = AccentCoral,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "El asistente está analizando y ejecutando acciones...",
                                color = TextDarkMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                state.errorMessage?.let { err ->
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AccentCoralContainer)
                                .border(1.dp, AccentCoral.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = err,
                                    color = AccentCoralDark,
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = { viewModel.openApiKeyDialog() }) {
                                    Text("Ajustar Clave", color = ElectricBlueDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Barra de entrada y previsualización de imagen adjunta
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CreamSurface)
                    .border(1.dp, CreamBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                // Vista previa de captura seleccionada
                if (state.selectedImageUri != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AsyncImage(
                            model = state.selectedImageUri,
                            contentDescription = "Captura adjunta",
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Captura lista para analizar",
                                color = TextDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Gemini Vision extraerá los datos automáticamente al enviar",
                                color = TextDarkMuted,
                                fontSize = 11.sp
                            )
                        }
                        IconButton(
                            onClick = { viewModel.clearSelectedImage() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Quitar", tint = TextDarkMuted, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { openGallery() },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(AccentCoral.copy(alpha = 0.12f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Adjuntar captura",
                            tint = AccentCoral,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    OutlinedTextField(
                        value = state.inputText,
                        onValueChange = { viewModel.onInputTextChanged(it) },
                        placeholder = { Text("Mensaje o consulta sobre tu captura...", color = TextDarkSubtle, fontSize = 13.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        maxLines = 3
                    )

                    IconButton(
                        onClick = { viewModel.sendMessage(context) },
                        enabled = (state.inputText.isNotBlank() || state.selectedImageUri != null) && !state.isLoading,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if ((state.inputText.isNotBlank() || state.selectedImageUri != null) && !state.isLoading) AccentCoral else CreamSurfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Enviar",
                            tint = if ((state.inputText.isNotBlank() || state.selectedImageUri != null) && !state.isLoading) Color.White else TextDarkSubtle,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        if (state.isApiKeyDialogOpen) {
            ApiKeySettingsDialog(
                currentKey = viewModel.currentApiKey,
                onSaveKey = { newKey -> viewModel.saveApiKey(newKey) },
                onDismiss = { viewModel.closeApiKeyDialog() }
            )
        }
    }
}

@Composable
private fun ChatMessageItem(
    message: ChatMessage,
    onAcceptProposal: (com.fittracker.app.data.remote.gemini.RoutineChangeProposal) -> Unit,
    onDismissProposal: () -> Unit
) {
    val isUser = message.role == "user"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Indicadores de herramientas ejecutadas
        if (message.toolExecutions.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .fillMaxWidth(0.92f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                message.toolExecutions.forEach { toolLog ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElectricBlueContainer)
                            .border(1.dp, ElectricBlue.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = toolLog,
                                color = ElectricBlueDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Burbuja de mensaje
        Box(
            modifier = Modifier
                .widthIn(max = 330.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isUser) 18.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 18.dp
                    )
                )
                .background(if (isUser) AccentCoral else CreamSurface)
                .border(
                    1.dp,
                    if (isUser) AccentCoral else CreamBorder,
                    RoundedCornerShape(18.dp)
                )
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (message.imageUri != null) {
                    AsyncImage(
                        model = message.imageUri,
                        contentDescription = "Captura",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                Text(
                    text = message.text,
                    color = if (isUser) Color.White else TextDark,
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )
            }
        }

        // Tarjeta interactiva de propuesta de modificación de rutina
        if (message.proposal != null && !message.isProposalHandled) {
            Spacer(modifier = Modifier.height(8.dp))
            ConfirmProposalCard(
                proposal = message.proposal,
                onAccept = onAcceptProposal,
                onDismiss = onDismissProposal,
                modifier = Modifier.fillMaxWidth(0.95f)
            )
        }
    }
}
