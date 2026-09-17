package com.fittracker.app.ui.scale

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fittracker.app.data.local.entities.BodyCompositionLog
import com.fittracker.app.data.remote.gemini.SmartScaleExtractionResult
import com.fittracker.app.data.repository.AiAssistantRepository
import com.fittracker.app.data.repository.BodyCompositionRepository
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

data class SmartScaleUiState(
    val isAnalyzing: Boolean = false,
    val selectedImageUri: Uri? = null,
    val extractedResult: SmartScaleExtractionResult? = null,
    val errorMessage: String? = null,
    val isSavedSuccess: Boolean = false,
    // Campos editables para revisión del usuario
    val inputDate: String = LocalDate.now().toString(),
    val inputWeightKg: String = "",
    val inputBmi: String = "",
    val inputBodyFatPercentage: String = "",
    val inputMuscleMassKg: String = "",
    val inputVisceralFat: String = "",
    val inputBmrKcal: String = "",
    val inputBodyWaterPercentage: String = "",
    val inputBoneMassKg: String = "",
    val inputProteinPercentage: String = "",
    val inputNotes: String = ""
)

class SmartScaleViewModel(
    private val aiAssistantRepository: AiAssistantRepository,
    private val bodyCompositionRepository: BodyCompositionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SmartScaleUiState())
    val uiState: StateFlow<SmartScaleUiState> = _uiState.asStateFlow()

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
                        errorMessage = "No se pudo leer la captura de la báscula."
                    )
                }
                return@launch
            }

            val result = aiAssistantRepository.parseSmartScaleScreenshot(base64String)
            result.onSuccess { extraction ->
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        extractedResult = extraction,
                        inputDate = extraction.date.ifBlank { LocalDate.now().toString() },
                        inputWeightKg = if (extraction.weightKg > 0) extraction.weightKg.toString() else "",
                        inputBmi = if (extraction.bmi > 0) extraction.bmi.toString() else "",
                        inputBodyFatPercentage = if (extraction.bodyFatPercentage > 0) extraction.bodyFatPercentage.toString() else "",
                        inputMuscleMassKg = if (extraction.muscleMassKg > 0) extraction.muscleMassKg.toString() else "",
                        inputVisceralFat = if (extraction.visceralFat > 0) extraction.visceralFat.toString() else "5",
                        inputBmrKcal = if (extraction.bmrKcal > 0) extraction.bmrKcal.toInt().toString() else "",
                        inputBodyWaterPercentage = if (extraction.bodyWaterPercentage > 0) extraction.bodyWaterPercentage.toString() else "",
                        inputBoneMassKg = if (extraction.boneMassKg > 0) extraction.boneMassKg.toString() else "",
                        inputProteinPercentage = if (extraction.proteinPercentage > 0) extraction.proteinPercentage.toString() else "",
                        inputNotes = "Captura de báscula inteligente analizada por IA"
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        errorMessage = "Error al analizar la báscula con Gemini: ${err.localizedMessage ?: "Error desconocido"}"
                    )
                }
            }
        }
    }

    fun updateDate(v: String) = _uiState.update { it.copy(inputDate = v) }
    fun updateWeight(v: String) = _uiState.update { it.copy(inputWeightKg = v) }
    fun updateBmi(v: String) = _uiState.update { it.copy(inputBmi = v) }
    fun updateBodyFat(v: String) = _uiState.update { it.copy(inputBodyFatPercentage = v) }
    fun updateMuscleMass(v: String) = _uiState.update { it.copy(inputMuscleMassKg = v) }
    fun updateVisceralFat(v: String) = _uiState.update { it.copy(inputVisceralFat = v) }
    fun updateBmr(v: String) = _uiState.update { it.copy(inputBmrKcal = v) }
    fun updateBodyWater(v: String) = _uiState.update { it.copy(inputBodyWaterPercentage = v) }
    fun updateBoneMass(v: String) = _uiState.update { it.copy(inputBoneMassKg = v) }
    fun updateProtein(v: String) = _uiState.update { it.copy(inputProteinPercentage = v) }
    fun updateNotes(v: String) = _uiState.update { it.copy(inputNotes = v) }

    fun confirmAndSave(onSuccess: () -> Unit) {
        val s = _uiState.value
        val weight = s.inputWeightKg.toDoubleOrNull() ?: 0.0
        if (weight <= 0.0) {
            _uiState.update { it.copy(errorMessage = "Introduce un peso válido en kg") }
            return
        }

        val bmi = s.inputBmi.toDoubleOrNull() ?: 0.0
        val fat = s.inputBodyFatPercentage.toDoubleOrNull() ?: 0.0
        val muscle = s.inputMuscleMassKg.toDoubleOrNull() ?: 0.0
        val visceral = s.inputVisceralFat.toIntOrNull() ?: 5
        val bmr = s.inputBmrKcal.toDoubleOrNull() ?: 0.0
        val water = s.inputBodyWaterPercentage.toDoubleOrNull() ?: 0.0
        val bone = s.inputBoneMassKg.toDoubleOrNull() ?: 0.0
        val protein = s.inputProteinPercentage.toDoubleOrNull() ?: 0.0
        val date = s.inputDate.ifBlank { LocalDate.now().toString() }

        viewModelScope.launch {
            bodyCompositionRepository.saveLog(
                BodyCompositionLog(
                    date = date,
                    weightKg = weight,
                    bmi = bmi,
                    bodyFatPercentage = fat,
                    muscleMassKg = muscle,
                    visceralFat = visceral,
                    bmrKcal = bmr,
                    bodyWaterPercentage = water,
                    boneMassKg = bone,
                    proteinPercentage = protein,
                    notes = s.inputNotes.takeIf { it.isNotBlank() }
                )
            )
            _uiState.update { it.copy(isSavedSuccess = true) }
            onSuccess()
        }
    }

    fun reset() {
        _uiState.value = SmartScaleUiState()
    }
}
