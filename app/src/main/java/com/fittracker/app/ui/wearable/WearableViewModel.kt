package com.fittracker.app.ui.wearable

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fittracker.app.data.local.entities.DailyActivitySummary
import com.fittracker.app.data.remote.gemini.ActivityExtractionResult
import com.fittracker.app.data.repository.ActivityRepository
import com.fittracker.app.data.repository.AiAssistantRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.time.LocalDate

data class WearableUiState(
    val isAnalyzing: Boolean = false,
    val selectedImageUri: Uri? = null,
    val extractedResult: ActivityExtractionResult? = null,
    val errorMessage: String? = null,
    val isSavedSuccess: Boolean = false,
    // Campos del formulario editable para corrección del usuario
    val inputDate: String = LocalDate.now().toString(),
    val inputSteps: String = "0",
    val inputActiveCalories: String = "0",
    val inputExerciseMinutes: String = "0",
    val inputExerciseType: String = "Entrenamiento"
)

class WearableViewModel(
    private val aiAssistantRepository: AiAssistantRepository,
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WearableUiState())
    val uiState: StateFlow<WearableUiState> = _uiState.asStateFlow()

    fun onImageSelected(context: Context, uri: Uri) {
        _uiState.update {
            it.copy(
                selectedImageUri = uri,
                isAnalyzing = true,
                errorMessage = null,
                isSavedSuccess = false
            )
        }

        viewModelScope.launch {
            val base64String = com.fittracker.app.util.ImageUtils.uriToBase64(context, uri)
            if (base64String == null) {
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        errorMessage = "No se pudo leer la imagen seleccionada."
                    )
                }
                return@launch
            }

            val result = aiAssistantRepository.parseActivityScreenshot(base64String)
            result.onSuccess { extraction ->
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        extractedResult = extraction,
                        inputDate = extraction.date.ifBlank { LocalDate.now().toString() },
                        inputSteps = extraction.steps.toString(),
                        inputActiveCalories = extraction.activeCalories.toInt().toString(),
                        inputExerciseMinutes = extraction.exerciseMinutes.toString(),
                        inputExerciseType = extraction.exerciseType.ifBlank { "Entrenamiento" }
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        errorMessage = "Error al analizar captura con Gemini: ${err.localizedMessage ?: "Error desconocido"}"
                    )
                }
            }
        }
    }

    fun updateDate(value: String) = _uiState.update { it.copy(inputDate = value) }
    fun updateSteps(value: String) = _uiState.update { it.copy(inputSteps = value) }
    fun updateActiveCalories(value: String) = _uiState.update { it.copy(inputActiveCalories = value) }
    fun updateExerciseMinutes(value: String) = _uiState.update { it.copy(inputExerciseMinutes = value) }
    fun updateExerciseType(value: String) = _uiState.update { it.copy(inputExerciseType = value) }

    fun confirmAndSave(onSuccess: () -> Unit) {
        val state = _uiState.value
        val steps = state.inputSteps.toIntOrNull() ?: 0
        val activeCalories = state.inputActiveCalories.toDoubleOrNull() ?: 0.0
        val exerciseMinutes = state.inputExerciseMinutes.toIntOrNull() ?: 0
        val date = state.inputDate.ifBlank { LocalDate.now().toString() }
        val exerciseType = state.inputExerciseType.ifBlank { "General" }

        viewModelScope.launch {
            activityRepository.saveActivitySummary(
                DailyActivitySummary(
                    date = date,
                    steps = steps,
                    activeCalories = activeCalories,
                    exerciseMinutes = exerciseMinutes,
                    exerciseType = exerciseType
                )
            )
            _uiState.update { it.copy(isSavedSuccess = true) }
            onSuccess()
        }
    }

    fun reset() {
        _uiState.value = WearableUiState()
    }
}
