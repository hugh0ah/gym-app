package com.fittracker.app.ui.weight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fittracker.app.data.local.entities.WeightLog
import com.fittracker.app.data.repository.WeightRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class WeightUiState(
    val logs: List<WeightLog> = emptyList(),
    val latestWeight: WeightLog? = null,
    val inputWeightKg: String = "",
    val inputNotes: String = "",
    val isDialogOpen: Boolean = false
) {
    val weightDifferenceVsPrevious: Double?
        get() {
            if (logs.size < 2) return null
            return logs[0].weightKg - logs[1].weightKg
        }
}

class WeightViewModel(
    private val weightRepository: WeightRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeightUiState())
    val uiState: StateFlow<WeightUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            weightRepository.allLogs.collect { logs ->
                _uiState.update {
                    it.copy(
                        logs = logs,
                        latestWeight = logs.firstOrNull(),
                        inputWeightKg = if (it.inputWeightKg.isBlank()) (logs.firstOrNull()?.weightKg?.toString() ?: "75.0") else it.inputWeightKg
                    )
                }
            }
        }
    }

    fun openDialog() {
        val currentLatest = _uiState.value.latestWeight?.weightKg?.toString() ?: "75.0"
        _uiState.update {
            it.copy(
                isDialogOpen = true,
                inputWeightKg = currentLatest,
                inputNotes = ""
            )
        }
    }

    fun closeDialog() {
        _uiState.update { it.copy(isDialogOpen = false) }
    }

    fun updateInputWeight(weight: String) {
        _uiState.update { it.copy(inputWeightKg = weight) }
    }

    fun updateInputNotes(notes: String) {
        _uiState.update { it.copy(inputNotes = notes) }
    }

    fun saveWeight() {
        val weight = _uiState.value.inputWeightKg.toDoubleOrNull() ?: return
        val notes = _uiState.value.inputNotes.takeIf { it.isNotBlank() }

        viewModelScope.launch {
            weightRepository.logWeight(
                weightKg = weight,
                date = LocalDate.now().toString(),
                notes = notes
            )
            closeDialog()
        }
    }

    fun deleteLog(id: Long) {
        viewModelScope.launch {
            weightRepository.deleteWeightLog(id)
        }
    }
}
