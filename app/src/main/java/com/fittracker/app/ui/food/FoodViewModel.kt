package com.fittracker.app.ui.food

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fittracker.app.data.local.dao.DailyMacroTotals
import com.fittracker.app.data.local.entities.FoodCache
import com.fittracker.app.data.local.entities.FoodLog
import com.fittracker.app.data.local.entities.UserTargets
import com.fittracker.app.data.repository.AiAssistantRepository
import com.fittracker.app.data.repository.FoodAnalysisResult
import com.fittracker.app.data.repository.FoodRepository
import com.fittracker.app.data.repository.WaterRepository
import com.fittracker.app.util.ImageUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class FoodUiState(
    val selectedDate: String = LocalDate.now().toString(),
    val logs: List<FoodLog> = emptyList(),
    val macroTotals: DailyMacroTotals = DailyMacroTotals(),
    val userTargets: UserTargets = UserTargets(),
    val waterConsumedMl: Int = 0,
    val waterGoalMl: Int = 2500,
    val isSearchDialogOpen: Boolean = false,
    val isTargetsDialogOpen: Boolean = false,
    val isPhotoAnalyzerDialogOpen: Boolean = false,
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val searchResults: List<FoodCache> = emptyList(),
    val selectedFoodForAdd: FoodCache? = null,
    val quantityInputG: String = "100",
    val selectedMealType: String = "Comida",
    val filterMealType: String = "Todos",
    val selectedPhotoUri: Uri? = null,
    val isAnalyzingFoodPhoto: Boolean = false,
    val analyzedFoodResult: FoodAnalysisResult? = null,
    val foodPhotoError: String? = null
)

class FoodViewModel(
    private val foodRepository: FoodRepository,
    private val waterRepository: WaterRepository,
    private val aiAssistantRepository: AiAssistantRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FoodUiState())
    val uiState: StateFlow<FoodUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var dataCollectionJob: Job? = null

    init {
        observeDateData(_uiState.value.selectedDate)
    }

    private fun observeDateData(date: String) {
        dataCollectionJob?.cancel()
        dataCollectionJob = viewModelScope.launch {
            combine(
                foodRepository.getLogsForDate(date),
                foodRepository.getMacroTotalsForDate(date),
                foodRepository.userTargets,
                waterRepository.getWaterForDate(date)
            ) { logs, totals, targets, water ->
                val userTarget = targets ?: UserTargets()
                _uiState.update {
                    it.copy(
                        logs = logs,
                        macroTotals = totals,
                        userTargets = userTarget,
                        waterConsumedMl = water
                    )
                }
            }.collect()
        }
    }

    fun changeDate(offsetDays: Long) {
        val current = LocalDate.parse(_uiState.value.selectedDate)
        val newDate = current.plusDays(offsetDays).toString()
        _uiState.update { it.copy(selectedDate = newDate) }
        observeDateData(newDate)
    }

    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            waterRepository.addWater(_uiState.value.selectedDate, amountMl)
        }
    }

    fun undoWater() {
        viewModelScope.launch {
            waterRepository.undoLastWater(_uiState.value.selectedDate)
        }
    }

    fun resetWater() {
        viewModelScope.launch {
            waterRepository.resetWaterForDate(_uiState.value.selectedDate)
        }
    }

    fun setFilterMeal(filter: String) {
        _uiState.update { it.copy(filterMealType = filter) }
    }

    fun setSelectedMealType(type: String) {
        _uiState.update { it.copy(selectedMealType = type) }
    }

    fun openSearchDialog(initialMealType: String? = null) {
        _uiState.update {
            it.copy(
                isSearchDialogOpen = true,
                searchQuery = "",
                searchResults = emptyList(),
                selectedFoodForAdd = null,
                quantityInputG = "100",
                selectedMealType = initialMealType ?: it.selectedMealType
            )
        }
    }

    fun closeSearchDialog() {
        _uiState.update { it.copy(isSearchDialogOpen = false) }
    }

    fun openTargetsDialog() {
        _uiState.update { it.copy(isTargetsDialogOpen = true) }
    }

    fun closeTargetsDialog() {
        _uiState.update { it.copy(isTargetsDialogOpen = false) }
    }

    fun openPhotoAnalyzer(defaultMealType: String = "Comida") {
        _uiState.update {
            it.copy(
                isPhotoAnalyzerDialogOpen = true,
                analyzedFoodResult = null,
                foodPhotoError = null,
                isAnalyzingFoodPhoto = false,
                selectedMealType = defaultMealType
            )
        }
    }

    fun closePhotoAnalyzer() {
        _uiState.update {
            it.copy(
                isPhotoAnalyzerDialogOpen = false,
                analyzedFoodResult = null,
                foodPhotoError = null,
                isAnalyzingFoodPhoto = false
            )
        }
    }

    fun onPhotoSelected(context: Context, uri: Uri) {
        _uiState.update {
            it.copy(
                isPhotoAnalyzerDialogOpen = true,
                selectedPhotoUri = uri,
                isAnalyzingFoodPhoto = true,
                foodPhotoError = null,
                analyzedFoodResult = null
            )
        }

        viewModelScope.launch {
            val base64 = ImageUtils.uriToBase64(context, uri)
            if (base64 == null) {
                _uiState.update {
                    it.copy(
                        isAnalyzingFoodPhoto = false,
                        foodPhotoError = "No se pudo procesar la imagen seleccionada."
                    )
                }
                return@launch
            }

            val result = aiAssistantRepository.analyzeFoodImage(base64)
            result.onSuccess { analyzed ->
                _uiState.update {
                    it.copy(
                        isAnalyzingFoodPhoto = false,
                        analyzedFoodResult = analyzed,
                        foodPhotoError = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isAnalyzingFoodPhoto = false,
                        foodPhotoError = error.message ?: "Error al analizar la imagen del plato."
                    )
                }
            }
        }
    }

    fun analyzeFoodPhoto(base64Image: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isAnalyzingFoodPhoto = true,
                    foodPhotoError = null,
                    analyzedFoodResult = null
                )
            }
            val result = aiAssistantRepository.analyzeFoodImage(base64Image)
            result.onSuccess { analyzed ->
                _uiState.update {
                    it.copy(
                        isAnalyzingFoodPhoto = false,
                        analyzedFoodResult = analyzed,
                        foodPhotoError = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isAnalyzingFoodPhoto = false,
                        foodPhotoError = error.message ?: "Error al analizar la imagen del plato."
                    )
                }
            }
        }
    }

    fun confirmAddAnalyzedFood(
        foodName: String,
        calories: Double,
        protein: Double,
        carbs: Double,
        fat: Double,
        quantityG: Double,
        mealType: String
    ) {
        viewModelScope.launch {
            foodRepository.addFoodLog(
                date = _uiState.value.selectedDate,
                foodName = foodName,
                calories = calories,
                protein = protein,
                carbs = carbs,
                fat = fat,
                quantityG = quantityG,
                mealType = mealType
            )
            closePhotoAnalyzer()
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _uiState.update { it.copy(searchQuery = newQuery) }
        searchJob?.cancel()
        if (newQuery.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(250)
            _uiState.update { it.copy(isSearching = true) }
            val results = foodRepository.searchFood(newQuery)
            _uiState.update { it.copy(searchResults = results, isSearching = false) }
        }
    }

    fun selectFoodToAdd(food: FoodCache) {
        _uiState.update { it.copy(selectedFoodForAdd = food) }
    }

    fun updateQuantityInput(grams: String) {
        _uiState.update { it.copy(quantityInputG = grams) }
    }

    fun confirmAddFood() {
        val currentState = _uiState.value
        val food = currentState.selectedFoodForAdd ?: return
        val grams = currentState.quantityInputG.toDoubleOrNull() ?: 100.0
        val factor = grams / 100.0

        viewModelScope.launch {
            foodRepository.addFoodLog(
                date = currentState.selectedDate,
                foodName = food.name + if (!food.brand.isNullOrBlank()) " (${food.brand})" else "",
                calories = food.caloriesPer100g * factor,
                protein = food.proteinPer100g * factor,
                carbs = food.carbsPer100g * factor,
                fat = food.fatPer100g * factor,
                quantityG = grams,
                mealType = currentState.selectedMealType
            )
            _uiState.update {
                it.copy(
                    selectedFoodForAdd = null,
                    isSearchDialogOpen = false
                )
            }
        }
    }

    fun deleteFoodLog(id: Long) {
        viewModelScope.launch {
            foodRepository.deleteFoodLog(id)
        }
    }

    fun saveTargets(calories: Double, protein: Double, carbs: Double, fat: Double) {
        viewModelScope.launch {
            foodRepository.updateUserTargets(
                UserTargets(
                    id = 1,
                    calorieTarget = calories,
                    proteinTarget = protein,
                    carbTarget = carbs,
                    fatTarget = fat
                )
            )
            _uiState.update { it.copy(isTargetsDialogOpen = false) }
        }
    }
}
