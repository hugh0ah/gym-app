package com.fittracker.app.ui.assistant

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fittracker.app.data.local.ApiKeyManager
import com.fittracker.app.data.remote.gemini.Content
import com.fittracker.app.data.remote.gemini.InlineData
import com.fittracker.app.data.remote.gemini.Part
import com.fittracker.app.data.remote.gemini.RoutineChangeProposal
import com.fittracker.app.data.repository.AiAssistantRepository
import com.fittracker.app.data.repository.RoutineRepository
import com.fittracker.app.util.ImageUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: String, // "user", "assistant"
    val text: String,
    val imageUri: Uri? = null,
    val toolExecutions: List<String> = emptyList(),
    val proposal: RoutineChangeProposal? = null,
    val isProposalHandled: Boolean = false
)

data class AssistantUiState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage(
            role = "assistant",
            text = "¡Hola! Soy tu Asistente y Entrenador IA. Puedes escribirme, pedirme rutinas, o adjuntarme capturas de tu báscula inteligente, comidas o entrenamientos con el icono 📷 para registrarlos directamente.\n\n¿En qué te ayudo hoy?"
        )
    ),
    val inputText: String = "",
    val selectedImageUri: Uri? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isApiKeyDialogOpen: Boolean = false
)

class AssistantViewModel(
    private val aiAssistantRepository: AiAssistantRepository,
    private val routineRepository: RoutineRepository,
    private val apiKeyManager: ApiKeyManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    private val conversationHistory = mutableListOf<Content>()

    val currentApiKey: String
        get() = apiKeyManager.getApiKey()

    fun onInputTextChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun onImageSelected(uri: Uri) {
        _uiState.update { it.copy(selectedImageUri = uri) }
    }

    fun clearSelectedImage() {
        _uiState.update { it.copy(selectedImageUri = null) }
    }

    fun openApiKeyDialog() {
        _uiState.update { it.copy(isApiKeyDialogOpen = true) }
    }

    fun closeApiKeyDialog() {
        _uiState.update { it.copy(isApiKeyDialogOpen = false) }
    }

    fun saveApiKey(key: String) {
        apiKeyManager.setApiKey(key)
        closeApiKeyDialog()
    }

    fun sendMessage(context: Context) {
        val text = _uiState.value.inputText.trim()
        val imageUri = _uiState.value.selectedImageUri

        if (text.isBlank() && imageUri == null) return
        if (_uiState.value.isLoading) return

        val userMessage = ChatMessage(
            role = "user",
            text = if (text.isNotBlank()) text else "📷 Captura adjuntada para análisis",
            imageUri = imageUri
        )

        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                inputText = "",
                selectedImageUri = null,
                isLoading = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            if (imageUri != null) {
                val base64 = ImageUtils.uriToBase64(context, imageUri)
                if (base64 == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "No se pudo leer la imagen seleccionada."
                        )
                    }
                    return@launch
                }

                val result = aiAssistantRepository.sendChatMessageWithImage(
                    conversationHistory = conversationHistory,
                    userMessage = text,
                    base64Image = base64
                )

                handleChatResult(result, text, base64)
            } else {
                val result = aiAssistantRepository.sendChatMessage(
                    conversationHistory = conversationHistory,
                    userMessage = text
                )

                handleChatResult(result, text, null)
            }
        }
    }

    private fun handleChatResult(
        result: Result<com.fittracker.app.data.repository.AssistantChatResult>,
        userText: String,
        base64: String?
    ) {
        result.onSuccess { chatResult ->
            val userParts = mutableListOf<Part>()
            if (userText.isNotBlank()) {
                userParts.add(Part(text = userText))
            }
            if (base64 != null) {
                userParts.add(Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64)))
            }
            if (userParts.isNotEmpty()) {
                conversationHistory.add(Content(role = "user", parts = userParts))
            }
            conversationHistory.add(
                Content(role = "model", parts = listOf(Part(text = chatResult.replyText)))
            )

            val assistantMessage = ChatMessage(
                role = "assistant",
                text = chatResult.replyText,
                toolExecutions = chatResult.toolExecutions,
                proposal = chatResult.routineProposal
            )

            _uiState.update {
                it.copy(
                    messages = it.messages + assistantMessage,
                    isLoading = false
                )
            }
        }.onFailure { error ->
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Error: ${error.localizedMessage ?: "Verifica tu clave de Gemini API pulsando en la llave 🔑"}"
                )
            }
        }
    }

    fun acceptProposal(messageId: String, proposal: RoutineChangeProposal) {
        viewModelScope.launch {
            routineRepository.applyProposalChange(proposal)

            _uiState.update { state ->
                val updatedMessages = state.messages.map { msg ->
                    if (msg.id == messageId) {
                        msg.copy(
                            isProposalHandled = true,
                            text = msg.text + "\n\n✅ *¡Cambio aceptado y aplicado en tu rutina de gimnasio!*"
                        )
                    } else msg
                }
                state.copy(messages = updatedMessages)
            }
        }
    }

    fun dismissProposal(messageId: String) {
        _uiState.update { state ->
            val updatedMessages = state.messages.map { msg ->
                if (msg.id == messageId) {
                    msg.copy(
                        isProposalHandled = true,
                        text = msg.text + "\n\n❌ *Propuesta descartada.*"
                    )
                } else msg
            }
            state.copy(messages = updatedMessages)
        }
    }
}
