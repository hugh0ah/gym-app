package com.fittracker.app.data.repository

import com.fittracker.app.data.local.ApiKeyManager
import com.fittracker.app.data.local.entities.DailyActivitySummary
import com.fittracker.app.data.local.entities.Exercise
import com.fittracker.app.data.local.entities.UserTargets
import com.fittracker.app.data.remote.gemini.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.LocalDate

data class AssistantChatResult(
    val replyText: String,
    val toolExecutions: List<String>,
    val routineProposal: RoutineChangeProposal? = null
)

data class FoodAnalysisResult(
    val foodName: String,
    val portionGrams: Double,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val mealType: String = "Comida",
    val confidenceNotes: String = ""
)

class AiAssistantRepository(
    private val routineRepository: RoutineRepository,
    private val activityRepository: ActivityRepository,
    private val exerciseRepository: ExerciseRepository,
    private val bodyCompositionRepository: BodyCompositionRepository,
    private val foodRepository: FoodRepository,
    private val weightRepository: WeightRepository,
    private val waterRepository: WaterRepository,
    private val apiKeyManager: ApiKeyManager
) {
    private val gson = Gson()

    companion object {
        val CANDIDATE_MODELS = listOf(
            "gemini-3.6-flash",
            "gemini-3.5-flash",
            "gemini-3.8-flash",
            "gemini-3.5-flash-lite",
            "gemini-3.1-flash-lite",
            "gemini-flash-latest"
        )
    }

    private fun getCurrentApiKey(): String {
        return apiKeyManager.getApiKey()
    }

    private suspend fun executeWithModelFallback(
        apiKey: String,
        request: GeminiRequest
    ): retrofit2.Response<GeminiResponse> {
        var lastResponse: retrofit2.Response<GeminiResponse>? = null
        for (modelName in CANDIDATE_MODELS) {
            try {
                val response = GeminiClient.api.generateContent(
                    model = modelName,
                    apiKey = apiKey,
                    request = request
                )
                if (response.isSuccessful) {
                    return response
                }
                lastResponse = response
                // Si el error es 503 (servidor saturado), 404 (modelo obsoleto) o 429 (rate limit), probamos con el siguiente
                if (response.code() == 503 || response.code() == 404 || response.code() == 429) {
                    continue
                } else {
                    return response
                }
            } catch (e: Exception) {
                // Reintentar con siguiente modelo
            }
        }
        return lastResponse ?: throw IllegalStateException("No se pudo obtener respuesta de los servidores de Gemini.")
    }

    /**
     * Envía la captura de Huawei Health a Gemini Vision para extraer actividad.
     */
    suspend fun parseActivityScreenshot(base64Image: String): Result<ActivityExtractionResult> = withContext(Dispatchers.IO) {
        try {
            val key = getCurrentApiKey()
            if (key.isBlank()) {
                return@withContext Result.failure(
                    IllegalStateException("Introduce tu clave de Gemini API en la app para analizar capturas.")
                )
            }

            val promptText = """
                Analiza esta captura de pantalla de la app Huawei Health (Huawei Watch Fit 3) y extrae los datos de actividad física.
                Devuelve EXCLUSIVAMENTE un objeto JSON válido (sin formato markdown ni texto adicional) con este esquema exacto:
                {
                  "date": "${LocalDate.now()}",
                  "steps": 0,
                  "activeCalories": 0.0,
                  "exerciseMinutes": 0,
                  "exerciseType": "Entrenamiento"
                }
                Si no puedes identificar con certeza la fecha, utiliza "${LocalDate.now()}".
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        role = "user",
                        parts = listOf(
                            Part(text = promptText),
                            Part(
                                inlineData = InlineData(
                                    mimeType = "image/jpeg",
                                    data = base64Image
                                )
                            )
                        )
                    )
                ),
                generationConfig = GenerationConfig(
                    temperature = 0.1f,
                    responseMimeType = "application/json"
                )
            )

            val response = executeWithModelFallback(key, request)

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                return@withContext Result.failure(Exception("Error Gemini (${response.code()}): $errorBody"))
            }

            val textResponse = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return@withContext Result.failure(Exception("Gemini no devolvió texto."))

            val cleanJson = textResponse
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val extraction = gson.fromJson(cleanJson, ActivityExtractionResult::class.java)
            Result.success(extraction)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Envía la captura de la báscula inteligente (Huawei Health Scale, Xiaomi, Renpho, etc.)
     * a Gemini Vision para extraer todos los parámetros de bioimpedancia.
     */
    suspend fun parseSmartScaleScreenshot(base64Image: String): Result<SmartScaleExtractionResult> = withContext(Dispatchers.IO) {
        try {
            val key = getCurrentApiKey()
            if (key.isBlank()) {
                return@withContext Result.failure(
                    IllegalStateException("Introduce tu clave de Gemini API para analizar capturas de báscula.")
                )
            }

            val promptText = """
                Analiza esta captura de pantalla de la app de una báscula inteligente (composición corporal / bioimpedancia).
                Extrae todos los datos visibles y devuelve EXCLUSIVAMENTE un objeto JSON válido (sin formato markdown adicional) con este esquema exacto:
                {
                  "date": "${LocalDate.now()}",
                  "weightKg": 75.0,
                  "bmi": 23.5,
                  "bodyFatPercentage": 16.5,
                  "muscleMassKg": 61.2,
                  "visceralFat": 5,
                  "bmrKcal": 1780.0,
                  "bodyWaterPercentage": 59.5,
                  "boneMassKg": 3.2,
                  "proteinPercentage": 18.5
                }
                Si algún valor no está en la captura, estima un valor razonable a partir del peso y masa corporal o pon 0.0.
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        role = "user",
                        parts = listOf(
                            Part(text = promptText),
                            Part(
                                inlineData = InlineData(
                                    mimeType = "image/jpeg",
                                    data = base64Image
                                )
                            )
                        )
                    )
                ),
                generationConfig = GenerationConfig(
                    temperature = 0.1f,
                    responseMimeType = "application/json"
                )
            )

            val response = executeWithModelFallback(key, request)

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                return@withContext Result.failure(Exception("Error al escanear báscula (${response.code()}): $errorBody"))
            }

            val textResponse = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return@withContext Result.failure(Exception("Gemini no devolvió datos para la báscula."))

            val cleanJson = textResponse
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val extraction = gson.fromJson(cleanJson, SmartScaleExtractionResult::class.java)
            Result.success(extraction)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Bucle conversacional con Function Calling
     */
    suspend fun sendChatMessage(
        conversationHistory: List<Content>,
        userMessage: String
    ): Result<AssistantChatResult> = withContext(Dispatchers.IO) {
        try {
            val key = getCurrentApiKey()
            if (key.isBlank()) {
                return@withContext Result.failure(
                    IllegalStateException("Configura tu clave de Gemini API pulsando en el icono de llave 🔑 arriba para hablar con el Asistente.")
                )
            }

            val tools = GeminiClient.createAssistantTools()
            val systemInstruction = GeminiClient.createSystemInstruction()

            val currentContents = conversationHistory.toMutableList()
            currentContents.add(
                Content(
                    role = "user",
                    parts = listOf(Part(text = userMessage))
                )
            )

            val executedTools = mutableListOf<String>()
            var proposedChange: RoutineChangeProposal? = null

            for (step in 1..4) {
                val request = GeminiRequest(
                    contents = currentContents,
                    tools = tools,
                    systemInstruction = systemInstruction,
                    generationConfig = GenerationConfig(temperature = 0.3f)
                )

                val response = executeWithModelFallback(key, request)

                if (!response.isSuccessful) {
                    val code = response.code()
                    val errorBody = response.errorBody()?.string()
                    return@withContext Result.failure(Exception("Error de Gemini ($code): $errorBody"))
                }

                val candidate = response.body()?.candidates?.firstOrNull()
                val candidateContent = candidate?.content
                    ?: return@withContext Result.failure(Exception("Sin respuesta de Gemini."))

                currentContents.add(candidateContent)

                val functionCalls = candidateContent.parts.mapNotNull { it.functionCall }

                if (functionCalls.isNotEmpty()) {
                    val responseParts = mutableListOf<Part>()

                    for (functionCall in functionCalls) {
                        val toolName = functionCall.name
                        val toolArgs = functionCall.args ?: emptyMap()
                        val responseMap = mutableMapOf<String, Any>()

                        when (toolName) {
                            "get_routine" -> {
                                val routineId = (toolArgs["routineId"] as? Number)?.toLong() ?: 1L
                                executedTools.add("Consultando rutina #$routineId...")
                                val exercises = routineRepository.getRoutineExercisesDirect(routineId)
                                val routineInfo = routineRepository.getRoutineById(routineId)
                                responseMap["routineName"] = routineInfo?.name ?: "Rutina #$routineId"
                                responseMap["exercises"] = exercises.map {
                                    mapOf(
                                        "exerciseId" to it.exerciseId,
                                        "name" to it.exerciseName,
                                        "muscleGroup" to it.muscleGroup,
                                        "dayOfWeek" to it.dayOfWeek,
                                        "sets" to it.sets,
                                        "reps" to it.reps,
                                        "weight" to it.weight
                                    )
                                }
                            }

                            "get_workout_history" -> {
                                val exerciseId = toolArgs["exerciseId"]?.toString() ?: ""
                                val weeks = (toolArgs["weeks"] as? Number)?.toInt() ?: 4
                                val exerciseName = exerciseRepository.getExerciseById(exerciseId)?.name ?: exerciseId
                                executedTools.add("Analizando historial de fuerza de $exerciseName ($weeks semanas)...")
                                val history = routineRepository.getExerciseHistory(exerciseId, weeks)
                                responseMap["exerciseId"] = exerciseId
                                responseMap["history"] = history.map {
                                    mapOf(
                                        "date" to it.date,
                                        "sets" to it.actualSets,
                                        "reps" to it.actualReps,
                                        "weight" to it.actualWeight,
                                        "notes" to (it.notes ?: "")
                                    )
                                }
                            }

                            "get_activity_history" -> {
                                val weeks = (toolArgs["weeks"] as? Number)?.toInt() ?: 2
                                executedTools.add("Consultando actividad de Huawei Watch ($weeks semanas)...")
                                val activityHistory = activityRepository.getActivityHistory(weeks)
                                responseMap["activity"] = activityHistory.map {
                                    mapOf(
                                        "date" to it.date,
                                        "steps" to it.steps,
                                        "activeCalories" to it.activeCalories,
                                        "exerciseMinutes" to it.exerciseMinutes,
                                        "exerciseType" to it.exerciseType
                                    )
                                }
                            }

                            "get_body_composition_history" -> {
                                val weeks = (toolArgs["weeks"] as? Number)?.toInt() ?: 4
                                executedTools.add("Consultando bioimpedancia de báscula inteligente ($weeks semanas)...")
                                val compHistory = bodyCompositionRepository.getHistory(weeks)
                                responseMap["bodyComposition"] = compHistory.map {
                                    mapOf(
                                        "date" to it.date,
                                        "weightKg" to it.weightKg,
                                        "bmi" to it.bmi,
                                        "bodyFatPercentage" to it.bodyFatPercentage,
                                        "muscleMassKg" to it.muscleMassKg,
                                        "visceralFat" to it.visceralFat,
                                        "bmrKcal" to it.bmrKcal,
                                        "bodyWaterPercentage" to it.bodyWaterPercentage
                                    )
                                }
                            }

                            "propose_routine_change" -> {
                                val routineId = (toolArgs["routineId"] as? Number)?.toLong() ?: 1L
                                val exerciseId = toolArgs["exerciseId"]?.toString() ?: ""
                                val sets = (toolArgs["proposedSets"] as? Number)?.toInt() ?: 3
                                val reps = (toolArgs["proposedReps"] as? Number)?.toInt() ?: 10
                                val weight = (toolArgs["proposedWeight"] as? Number)?.toDouble() ?: 50.0
                                val dayOfWeek = (toolArgs["dayOfWeek"] as? Number)?.toInt() ?: 1
                                val justification = toolArgs["justification"]?.toString() ?: "Ajuste de sobrecarga progresiva"

                                val exercise = exerciseRepository.getExerciseById(exerciseId)
                                val exerciseName = exercise?.name ?: exerciseId

                                executedTools.add("Generando propuesta para $exerciseName...")

                                proposedChange = RoutineChangeProposal(
                                    routineId = routineId,
                                    exerciseId = exerciseId,
                                    exerciseName = exerciseName,
                                    proposedSets = sets,
                                    proposedReps = reps,
                                    proposedWeight = weight,
                                    dayOfWeek = dayOfWeek,
                                    justification = justification
                                )

                                responseMap["status"] = "Propuesta registrada para confirmación del usuario."
                            }

                            "create_routine" -> {
                                val routineName = toolArgs["name"]?.toString() ?: "Nueva Rutina IA"
                                executedTools.add("Creando rutina '$routineName'...")
                                val routineId = routineRepository.createRoutine(routineName)

                                val rawExercises = toolArgs["exercises"]
                                val exerciseList: List<*> = when (rawExercises) {
                                    is List<*> -> rawExercises
                                    is String -> {
                                        try {
                                            gson.fromJson(rawExercises, List::class.java) ?: rawExercises.lines().filter { it.isNotBlank() }
                                        } catch (e: Exception) {
                                            rawExercises.lines().filter { it.isNotBlank() }
                                        }
                                    }
                                    else -> emptyList<Any>()
                                }

                                var addedCount = 0
                                for ((index, item) in exerciseList.withIndex()) {
                                    if (item == null) continue
                                    val parsed = parseExerciseItem(item, defaultDay = ((index % 4) + 1), defaultIndex = index)

                                    val found = exerciseRepository.searchExercises(parsed.name).first().firstOrNull()
                                    val exId = found?.id ?: parsed.name.lowercase().replace(Regex("[^a-z0-9_]"), "_").take(40).ifBlank { "ejercicio_${index + 1}" }

                                    if (found == null && exerciseRepository.getExerciseById(exId) == null) {
                                        val muscleGroup = detectMuscleGroup(parsed.name)
                                        exerciseRepository.insertExercise(
                                            Exercise(
                                                id = exId,
                                                name = parsed.name,
                                                muscleGroup = muscleGroup,
                                                equipment = "Gimnasio",
                                                level = "Intermedio",
                                                instructions = "Ejercicio para rutina personalizado por Asistente IA"
                                            )
                                        )
                                    }

                                    routineRepository.addExerciseToRoutine(
                                        routineId = routineId,
                                        exerciseId = exId,
                                        sets = parsed.sets,
                                        reps = parsed.reps,
                                        weight = parsed.weightKg,
                                        order = index + 1,
                                        dayOfWeek = parsed.dayOfWeek
                                    )
                                    addedCount++
                                }

                                responseMap["status"] = "success"
                                responseMap["routineId"] = routineId
                                responseMap["message"] = "Rutina '$routineName' creada con éxito ($addedCount ejercicios añadidos)."
                                executedTools.add("Rutina '$routineName' guardada en la base de datos ($addedCount ejercicios).")
                            }

                            "log_food" -> {
                                val foodName = toolArgs["foodName"]?.toString() ?: "Alimento"
                                val calories = (toolArgs["calories"] as? Number)?.toDouble() ?: 0.0
                                val protein = (toolArgs["protein"] as? Number)?.toDouble() ?: 0.0
                                val carbs = (toolArgs["carbs"] as? Number)?.toDouble() ?: 0.0
                                val fat = (toolArgs["fat"] as? Number)?.toDouble() ?: 0.0
                                val quantityG = (toolArgs["quantityG"] as? Number)?.toDouble() ?: 100.0
                                val mealType = toolArgs["mealType"]?.toString() ?: "Comida"

                                val logId = foodRepository.addFoodLog(
                                    date = LocalDate.now().toString(),
                                    foodName = foodName,
                                    calories = calories,
                                    protein = protein,
                                    carbs = carbs,
                                    fat = fat,
                                    quantityG = quantityG,
                                    mealType = mealType
                                )
                                executedTools.add("Registrado en tu diario ($mealType): $foodName (${calories.toInt()} kcal, ${protein.toInt()}g P, ${carbs.toInt()}g C, ${fat.toInt()}g G)")
                                responseMap["status"] = "success"
                                responseMap["logId"] = logId
                                responseMap["message"] = "Alimento '$foodName' registrado en el diario de hoy ($mealType)."
                            }

                            "log_water_intake" -> {
                                val amountMl = (toolArgs["amountMl"] as? Number)?.toInt() ?: 250
                                val logId = waterRepository.addWater(
                                    date = LocalDate.now().toString(),
                                    amountMl = amountMl
                                )
                                executedTools.add("Registrado consumo de agua: +$amountMl ml")
                                responseMap["status"] = "success"
                                responseMap["logId"] = logId
                                responseMap["message"] = "$amountMl ml de agua registrados con éxito."
                            }

                            "log_body_weight" -> {
                                val weightKg = (toolArgs["weightKg"] as? Number)?.toDouble() ?: 0.0
                                val notes = toolArgs["notes"]?.toString() ?: "Registrado por Asistente IA"
                                if (weightKg > 0) {
                                    val logId = weightRepository.logWeight(
                                        weightKg = weightKg,
                                        date = LocalDate.now().toString(),
                                        notes = notes
                                    )
                                    executedTools.add("Registrado pesaje corporal: $weightKg kg")
                                    responseMap["status"] = "success"
                                    responseMap["logId"] = logId
                                    responseMap["message"] = "Peso de $weightKg kg registrado."
                                } else {
                                    responseMap["status"] = "error"
                                    responseMap["message"] = "Peso inválido."
                                }
                            }

                            "log_smart_scale_measurement" -> {
                                val weightKg = (toolArgs["weightKg"] as? Number)?.toDouble() ?: 0.0
                                val fat = (toolArgs["bodyFatPercentage"] as? Number)?.toDouble() ?: 0.0
                                val muscle = (toolArgs["muscleMassKg"] as? Number)?.toDouble() ?: 0.0
                                val bmi = (toolArgs["bmi"] as? Number)?.toDouble() ?: 0.0
                                val visceral = (toolArgs["visceralFat"] as? Number)?.toInt() ?: 5
                                val water = (toolArgs["bodyWaterPercentage"] as? Number)?.toDouble() ?: 0.0
                                val bmr = (toolArgs["bmrKcal"] as? Number)?.toDouble() ?: 0.0

                                if (weightKg > 0) {
                                    val logId = bodyCompositionRepository.saveLog(
                                        com.fittracker.app.data.local.entities.BodyCompositionLog(
                                            date = LocalDate.now().toString(),
                                            weightKg = weightKg,
                                            bmi = bmi,
                                            bodyFatPercentage = fat,
                                            muscleMassKg = muscle,
                                            visceralFat = visceral,
                                            bmrKcal = bmr,
                                            bodyWaterPercentage = water,
                                            boneMassKg = 0.0,
                                            proteinPercentage = 0.0,
                                            notes = "Báscula inteligente escaneada por Asistente IA"
                                        )
                                    )
                                    executedTools.add("Registrado pesaje y composición: $weightKg kg ($fat% grasa, $muscle kg músculo)")
                                    responseMap["status"] = "success"
                                    responseMap["logId"] = logId
                                    responseMap["message"] = "Medición de báscula de $weightKg kg guardada con éxito."
                                } else {
                                    responseMap["status"] = "error"
                                    responseMap["message"] = "Peso de báscula inválido."
                                }
                            }

                            "log_workout_set" -> {
                                val exerciseQuery = toolArgs["exerciseId"]?.toString() ?: toolArgs["exerciseName"]?.toString() ?: "Ejercicio"
                                val sets = (toolArgs["sets"] as? Number)?.toInt() ?: 3
                                val reps = (toolArgs["reps"] as? Number)?.toInt() ?: 10
                                val weightKg = (toolArgs["weightKg"] as? Number)?.toDouble() ?: 0.0
                                val notes = toolArgs["notes"]?.toString()

                                val matched = exerciseRepository.searchExercises(exerciseQuery).first().firstOrNull()
                                val realId = matched?.id ?: exerciseQuery.lowercase().replace(Regex("[^a-z0-9_]"), "_").take(40).ifBlank { "ejercicio" }

                                if (matched == null && exerciseRepository.getExerciseById(realId) == null) {
                                    val muscleGroup = detectMuscleGroup(exerciseQuery)
                                    exerciseRepository.insertExercise(
                                        Exercise(
                                            id = realId,
                                            name = exerciseQuery,
                                            muscleGroup = muscleGroup,
                                            equipment = "Gimnasio",
                                            level = "Intermedio",
                                            instructions = "Ejercicio registrado por Asistente IA"
                                        )
                                    )
                                }

                                val logId = routineRepository.logWorkout(
                                    exerciseId = realId,
                                    date = LocalDate.now().toString(),
                                    actualSets = sets,
                                    actualReps = reps,
                                    actualWeight = weightKg,
                                    notes = notes
                                )
                                val displayName = matched?.name ?: exerciseQuery
                                executedTools.add("Registrado entreno: $displayName ($sets x $reps @ ${weightKg}kg)")
                                responseMap["status"] = "success"
                                responseMap["logId"] = logId
                                responseMap["message"] = "Entrenamiento registrado con éxito."
                            }

                            "log_cardio_activity" -> {
                                val steps = (toolArgs["steps"] as? Number)?.toInt() ?: 0
                                val activeCalories = (toolArgs["activeCalories"] as? Number)?.toDouble() ?: 0.0
                                val exerciseMinutes = (toolArgs["exerciseMinutes"] as? Number)?.toInt() ?: 0
                                val exerciseType = toolArgs["exerciseType"]?.toString() ?: "Cardio"

                                activityRepository.saveActivitySummary(
                                    DailyActivitySummary(
                                        date = LocalDate.now().toString(),
                                        steps = steps,
                                        activeCalories = activeCalories,
                                        exerciseMinutes = exerciseMinutes,
                                        exerciseType = exerciseType
                                    )
                                )
                                executedTools.add("Registrado cardio: $steps pasos, ${activeCalories.toInt()} kcal, $exerciseMinutes min ($exerciseType)")
                                responseMap["status"] = "success"
                                responseMap["message"] = "Actividad registrada con éxito."
                            }

                            "update_nutrition_targets" -> {
                                val calorieTarget = (toolArgs["calorieTarget"] as? Number)?.toDouble() ?: 2000.0
                                val proteinTarget = (toolArgs["proteinTarget"] as? Number)?.toDouble() ?: 140.0
                                val carbTarget = (toolArgs["carbTarget"] as? Number)?.toDouble() ?: 220.0
                                val fatTarget = (toolArgs["fatTarget"] as? Number)?.toDouble() ?: 60.0

                                foodRepository.updateUserTargets(
                                    UserTargets(
                                        calorieTarget = calorieTarget,
                                        proteinTarget = proteinTarget,
                                        carbTarget = carbTarget,
                                        fatTarget = fatTarget
                                    )
                                )
                                executedTools.add("Nuevas metas nutricionales: ${calorieTarget.toInt()} kcal (${proteinTarget.toInt()}g P / ${carbTarget.toInt()}g C / ${fatTarget.toInt()}g G)")
                                responseMap["status"] = "success"
                                responseMap["message"] = "Objetivos nutricionales actualizados en la app."
                            }

                            "clear_all_data" -> {
                                val confirm = (toolArgs["confirm"] as? Boolean) ?: true
                                if (confirm) {
                                    routineRepository.clearAllRoutinesAndWorkouts()
                                    foodRepository.clearAllFoodLogs()
                                    weightRepository.clearAllWeightLogs()
                                    activityRepository.clearAllActivities()
                                    bodyCompositionRepository.clearAllBodyCompositionLogs()
                                    executedTools.add("Todos los registros han sido borrados a cero.")
                                    responseMap["status"] = "success"
                                    responseMap["message"] = "Todos los registros de comidas, rutinas, pesajes y actividades han sido borrados a cero."
                                } else {
                                    responseMap["status"] = "error"
                                    responseMap["message"] = "No confirmado."
                                }
                            }

                            else -> {
                                responseMap["status"] = "Ok"
                            }
                        }

                        responseParts.add(
                            Part(
                                functionResponse = FunctionResponse(
                                    name = toolName,
                                    response = responseMap,
                                    id = functionCall.id
                                )
                            )
                        )
                    }

                    // En la API Gemini v1beta, las respuestas a herramientas DEBEN enviarse con role = "user"
                    currentContents.add(
                        Content(
                            role = "user",
                            parts = responseParts
                        )
                    )
                } else {
                    val finalReply = candidateContent.parts
                        .mapNotNull { it.text }
                        .joinToString("\n")

                    return@withContext Result.success(
                        AssistantChatResult(
                            replyText = finalReply,
                            toolExecutions = executedTools,
                            routineProposal = proposedChange
                        )
                    )
                }
            }

            val replyText = currentContents.lastOrNull()?.parts?.mapNotNull { it.text }?.joinToString("\n")
                ?: "He completado el análisis y diseño de la rutina."

            Result.success(
                AssistantChatResult(
                    replyText = replyText,
                    toolExecutions = executedTools,
                    routineProposal = proposedChange
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Envía un mensaje conversacional junto con una imagen/captura (báscula, wearable, comida)
     */
    suspend fun sendChatMessageWithImage(
        conversationHistory: List<Content>,
        userMessage: String,
        base64Image: String
    ): Result<AssistantChatResult> = withContext(Dispatchers.IO) {
        try {
            val key = getCurrentApiKey()
            if (key.isBlank()) {
                return@withContext Result.failure(
                    IllegalStateException("Configura tu clave de Gemini API pulsando en el icono de llave 🔑 arriba para hablar con el Asistente.")
                )
            }

            val textPrompt = if (userMessage.isNotBlank()) {
                userMessage
            } else {
                "Analiza esta captura de pantalla (báscula inteligente, composición corporal, actividad o nutrición) y registra los datos con tus herramientas."
            }

            val userParts = listOf(
                Part(text = textPrompt),
                Part(
                    inlineData = InlineData(
                        mimeType = "image/jpeg",
                        data = base64Image
                    )
                )
            )

            val currentContents = conversationHistory.toMutableList()
            currentContents.add(Content(role = "user", parts = userParts))

            val tools = GeminiClient.createAssistantTools()
            val systemInstruction = GeminiClient.createSystemInstruction()
            val executedTools = mutableListOf<String>()
            var proposedChange: RoutineChangeProposal? = null

            for (step in 1..4) {
                val request = GeminiRequest(
                    contents = currentContents,
                    tools = tools,
                    systemInstruction = systemInstruction,
                    generationConfig = GenerationConfig(temperature = 0.2f)
                )

                val response = executeWithModelFallback(key, request)

                if (!response.isSuccessful) {
                    val code = response.code()
                    val errorBody = response.errorBody()?.string()
                    return@withContext Result.failure(Exception("Error de Gemini ($code): $errorBody"))
                }

                val candidate = response.body()?.candidates?.firstOrNull()
                val candidateContent = candidate?.content
                    ?: return@withContext Result.failure(Exception("Sin respuesta de Gemini."))

                currentContents.add(candidateContent)

                val functionCalls = candidateContent.parts.mapNotNull { it.functionCall }

                if (functionCalls.isNotEmpty()) {
                    val responseParts = mutableListOf<Part>()

                    for (functionCall in functionCalls) {
                        val toolName = functionCall.name
                        val toolArgs = functionCall.args ?: emptyMap()
                        val responseMap = mutableMapOf<String, Any>()

                        when (toolName) {
                            "log_smart_scale_measurement" -> {
                                val weightKg = (toolArgs["weightKg"] as? Number)?.toDouble() ?: 0.0
                                val fat = (toolArgs["bodyFatPercentage"] as? Number)?.toDouble() ?: 0.0
                                val muscle = (toolArgs["muscleMassKg"] as? Number)?.toDouble() ?: 0.0
                                val bmi = (toolArgs["bmi"] as? Number)?.toDouble() ?: 0.0
                                val visceral = (toolArgs["visceralFat"] as? Number)?.toInt() ?: 5
                                val water = (toolArgs["bodyWaterPercentage"] as? Number)?.toDouble() ?: 0.0
                                val bmr = (toolArgs["bmrKcal"] as? Number)?.toDouble() ?: 0.0

                                if (weightKg > 0) {
                                    val logId = bodyCompositionRepository.saveLog(
                                        com.fittracker.app.data.local.entities.BodyCompositionLog(
                                            date = LocalDate.now().toString(),
                                            weightKg = weightKg,
                                            bmi = bmi,
                                            bodyFatPercentage = fat,
                                            muscleMassKg = muscle,
                                            visceralFat = visceral,
                                            bmrKcal = bmr,
                                            bodyWaterPercentage = water,
                                            boneMassKg = 0.0,
                                            proteinPercentage = 0.0,
                                            notes = "Báscula inteligente escaneada por Asistente IA"
                                        )
                                    )
                                    executedTools.add("Registrado pesaje y composición: $weightKg kg ($fat% grasa, $muscle kg músculo)")
                                    responseMap["status"] = "success"
                                    responseMap["logId"] = logId
                                    responseMap["message"] = "Medición completa de báscula guardada: $weightKg kg, $fat% grasa, $muscle kg músculo."
                                } else {
                                    responseMap["status"] = "error"
                                    responseMap["message"] = "Peso de báscula inválido."
                                }
                            }

                            "log_body_weight" -> {
                                val weightKg = (toolArgs["weightKg"] as? Number)?.toDouble() ?: 0.0
                                val notes = toolArgs["notes"]?.toString() ?: "Registrado desde captura por Asistente IA"
                                if (weightKg > 0) {
                                    val logId = weightRepository.logWeight(
                                        weightKg = weightKg,
                                        date = LocalDate.now().toString(),
                                        notes = notes
                                    )
                                    executedTools.add("Registrado pesaje corporal: $weightKg kg")
                                    responseMap["status"] = "success"
                                    responseMap["logId"] = logId
                                    responseMap["message"] = "Peso de $weightKg kg registrado."
                                } else {
                                    responseMap["status"] = "error"
                                    responseMap["message"] = "Peso inválido."
                                }
                            }

                            "log_food" -> {
                                val foodName = toolArgs["foodName"]?.toString() ?: "Alimento"
                                val calories = (toolArgs["calories"] as? Number)?.toDouble() ?: 0.0
                                val protein = (toolArgs["protein"] as? Number)?.toDouble() ?: 0.0
                                val carbs = (toolArgs["carbs"] as? Number)?.toDouble() ?: 0.0
                                val fat = (toolArgs["fat"] as? Number)?.toDouble() ?: 0.0
                                val quantityG = (toolArgs["quantityG"] as? Number)?.toDouble() ?: 100.0
                                val mealType = toolArgs["mealType"]?.toString() ?: "Comida"

                                val logId = foodRepository.addFoodLog(
                                    date = LocalDate.now().toString(),
                                    foodName = foodName,
                                    calories = calories,
                                    protein = protein,
                                    carbs = carbs,
                                    fat = fat,
                                    quantityG = quantityG,
                                    mealType = mealType
                                )
                                executedTools.add("Registrado en diario ($mealType): $foodName (${calories.toInt()} kcal, ${protein.toInt()}g P, ${carbs.toInt()}g C, ${fat.toInt()}g G)")
                                responseMap["status"] = "success"
                                responseMap["logId"] = logId
                                responseMap["message"] = "Alimento '$foodName' registrado en el diario de hoy ($mealType)."
                            }

                            "log_water_intake" -> {
                                val amountMl = (toolArgs["amountMl"] as? Number)?.toInt() ?: 250
                                val logId = waterRepository.addWater(
                                    date = LocalDate.now().toString(),
                                    amountMl = amountMl
                                )
                                executedTools.add("Registrado agua: +$amountMl ml")
                                responseMap["status"] = "success"
                                responseMap["logId"] = logId
                                responseMap["message"] = "$amountMl ml de agua registrados con éxito."
                            }

                            "log_cardio_activity" -> {
                                val steps = (toolArgs["steps"] as? Number)?.toInt() ?: 0
                                val activeCalories = (toolArgs["activeCalories"] as? Number)?.toDouble() ?: 0.0
                                val exerciseMinutes = (toolArgs["exerciseMinutes"] as? Number)?.toInt() ?: 0
                                val exerciseType = toolArgs["exerciseType"]?.toString() ?: "Cardio"

                                activityRepository.saveActivitySummary(
                                    DailyActivitySummary(
                                        date = LocalDate.now().toString(),
                                        steps = steps,
                                        activeCalories = activeCalories,
                                        exerciseMinutes = exerciseMinutes,
                                        exerciseType = exerciseType
                                    )
                                )
                                executedTools.add("Registrado cardio: $steps pasos, ${activeCalories.toInt()} kcal, $exerciseMinutes min")
                                responseMap["status"] = "success"
                                responseMap["message"] = "Actividad registrada con éxito."
                            }

                            else -> {
                                responseMap["status"] = "Ok"
                            }
                        }

                        responseParts.add(
                            Part(
                                functionResponse = FunctionResponse(
                                    name = toolName,
                                    response = responseMap,
                                    id = functionCall.id
                                )
                            )
                        )
                    }

                    currentContents.add(
                        Content(
                            role = "user",
                            parts = responseParts
                        )
                    )
                } else {
                    val finalReply = candidateContent.parts
                        .mapNotNull { it.text }
                        .joinToString("\n")

                    return@withContext Result.success(
                        AssistantChatResult(
                            replyText = finalReply,
                            toolExecutions = executedTools,
                            routineProposal = proposedChange
                        )
                    )
                }
            }

            val replyText = currentContents.lastOrNull()?.parts?.mapNotNull { it.text }?.joinToString("\n")
                ?: "He procesado la captura correctamente."

            Result.success(
                AssistantChatResult(
                    replyText = replyText,
                    toolExecutions = executedTools,
                    routineProposal = proposedChange
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Analiza una fotografía de comida mediante Gemini Vision y calcula automáticamente los macronutrientes.
     */
    suspend fun analyzeFoodImage(base64Image: String): Result<FoodAnalysisResult> = withContext(Dispatchers.IO) {
        try {
            val key = getCurrentApiKey()
            if (key.isBlank()) {
                return@withContext Result.failure(IllegalStateException("Configura tu API key de Gemini para analizar fotos de comida."))
            }

            val promptText = """
                Eres un nutricionista profesional y experto en visión artificial para fitness.
                Analiza esta fotografía de comida o plato preparado con atención.
                Identifica los alimentos e ingredientes visibles, estima el tamaño de la ración aproximada en gramos y calcula con rigor nutricional sus calorías y macronutrientes.
                Debes responder EXCLUSIVAMENTE un objeto JSON válido (sin formato markdown, sin comillas invertidas ni explicaciones adicionales), con exactamente esta estructura:
                {
                  "foodName": "Nombre descriptivo del plato o alimento en español",
                  "portionGrams": 300,
                  "calories": 450.0,
                  "protein": 35.0,
                  "carbs": 42.0,
                  "fat": 12.0,
                  "mealType": "Comida",
                  "confidenceNotes": "Estimación calculada visualmente para ración de ~300g"
                }
                Los valores posibles para mealType son: 'Desayuno', 'Comida', 'Cena', 'Snack'.
            """.trimIndent()

            val parts = listOf(
                Part(text = promptText),
                Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Image))
            )

            val request = GeminiRequest(
                contents = listOf(Content(role = "user", parts = parts)),
                generationConfig = GenerationConfig(temperature = 0.2f)
            )

            var lastException: Exception? = null
            for (model in CANDIDATE_MODELS) {
                try {
                    val response = GeminiClient.api.generateContent(
                        model = model,
                        apiKey = key,
                        request = request
                    )

                    if (response.isSuccessful) {
                        val body = response.body()
                        val rawText = body?.candidates?.firstOrNull()?.content?.parts?.mapNotNull { it.text }?.joinToString("\n") ?: ""
                        if (rawText.isNotBlank()) {
                            val cleanJson = rawText
                                .replace(Regex("""^```json\s*""", RegexOption.IGNORE_CASE), "")
                                .replace(Regex("""^```\s*"""), "")
                                .replace(Regex("""\s*```$"""), "")
                                .trim()

                            val parsed = try {
                                gson.fromJson(cleanJson, Map::class.java)
                            } catch (e: Exception) {
                                null
                            }

                            if (parsed != null) {
                                val foodName = (parsed["foodName"] ?: "Plato analizado").toString()
                                val portion = (parsed["portionGrams"] as? Number)?.toDouble() ?: 250.0
                                val cal = (parsed["calories"] as? Number)?.toDouble() ?: 350.0
                                val p = (parsed["protein"] as? Number)?.toDouble() ?: 25.0
                                val c = (parsed["carbs"] as? Number)?.toDouble() ?: 30.0
                                val f = (parsed["fat"] as? Number)?.toDouble() ?: 10.0
                                val meal = (parsed["mealType"] ?: "Comida").toString()
                                val notes = (parsed["confidenceNotes"] ?: "").toString()

                                return@withContext Result.success(
                                    FoodAnalysisResult(
                                        foodName = foodName,
                                        portionGrams = portion,
                                        calories = cal,
                                        protein = p,
                                        carbs = c,
                                        fat = f,
                                        mealType = meal,
                                        confidenceNotes = notes
                                    )
                                )
                            }
                        }
                    } else {
                        val errorBody = response.errorBody()?.string().orEmpty()
                        lastException = RuntimeException("Error Gemini ($model): ${response.code()} - $errorBody")
                    }
                } catch (e: Exception) {
                    lastException = e
                }
            }

            Result.failure(lastException ?: IllegalStateException("No se pudo analizar la foto del plato."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private data class ParsedRoutineExercise(
        val name: String,
        val dayOfWeek: Int,
        val sets: Int,
        val reps: Int,
        val weightKg: Double
    )

    private fun parseExerciseItem(rawItem: Any, defaultDay: Int, defaultIndex: Int): ParsedRoutineExercise {
        if (rawItem is Map<*, *>) {
            val name = (rawItem["exerciseName"] ?: rawItem["name"] ?: rawItem["exerciseId"])?.toString() ?: "Ejercicio ${defaultIndex + 1}"
            val day = (rawItem["dayOfWeek"] as? Number)?.toInt() ?: (rawItem["day"] as? Number)?.toInt() ?: defaultDay
            val sets = (rawItem["sets"] as? Number)?.toInt() ?: 3
            val reps = (rawItem["reps"] as? Number)?.toInt() ?: 10
            val weight = (rawItem["weightKg"] as? Number)?.toDouble() ?: (rawItem["weight"] as? Number)?.toDouble() ?: 0.0
            return ParsedRoutineExercise(name, day, sets, reps, weight)
        }

        val text = rawItem.toString().trim()
        var day = defaultDay
        val lower = text.lowercase()
        if (lower.contains("lunes") || lower.contains("día 1") || lower.contains("dia 1")) day = 1
        else if (lower.contains("martes") || lower.contains("día 2") || lower.contains("dia 2")) day = 2
        else if (lower.contains("miércoles") || lower.contains("miercoles") || lower.contains("día 3") || lower.contains("dia 3")) day = 3
        else if (lower.contains("jueves") || lower.contains("día 4") || lower.contains("dia 4")) day = 4
        else if (lower.contains("viernes") || lower.contains("día 5") || lower.contains("dia 5")) day = 5
        else if (lower.contains("sábado") || lower.contains("sabado") || lower.contains("día 6") || lower.contains("dia 6")) day = 6
        else if (lower.contains("domingo") || lower.contains("día 7") || lower.contains("dia 7")) day = 7

        var sets = 3
        var reps = 10
        val setsRepsRegex = Regex("""(\d+)\s*(?:series?|sets?|\s*[xX])\s*(?:de\s*)?(\d+)""")
        val match = setsRepsRegex.find(text)
        if (match != null) {
            sets = match.groupValues[1].toIntOrNull() ?: 3
            reps = match.groupValues[2].toIntOrNull() ?: 10
        }

        var weight = 0.0
        val weightRegex = Regex("""(\d+(?:\.\d+)?)\s*(?:kg|kilos|kg\.)""", RegexOption.IGNORE_CASE)
        val weightMatch = weightRegex.find(text)
        if (weightMatch != null) {
            weight = weightMatch.groupValues[1].toDoubleOrNull() ?: 0.0
        }

        var cleanName = text
            .replace(Regex("""^(?:d[íi]a\s*\d+\s*[-:]?\s*[^:]*:\s*)""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""^\d+[\.\)\-]\s*"""), "")
            .replace(Regex("""[-–—:]\s*\d+\s*series?.*$""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""[-–—:]\s*\d+\s*[xX]\s*\d+.*$"""), "")
            .replace(Regex("""\(\s*\d+\s*[xX]\s*\d+.*\)"""), "")
            .trim()
            .ifBlank { "Ejercicio ${defaultIndex + 1}" }

        return ParsedRoutineExercise(cleanName, day, sets, reps, weight)
    }

    private fun detectMuscleGroup(name: String): String {
        val lower = name.lowercase()
        return when {
            lower.contains("pecho") || lower.contains("banca") || lower.contains("chest") || lower.contains("apertur") || lower.contains("cruce") || lower.contains("flexion") -> "chest"
            lower.contains("tricep") || lower.contains("fondo") || lower.contains("frances") || lower.contains("copa") || lower.contains("polea") -> "triceps"
            lower.contains("bicep") || lower.contains("curl") || lower.contains("martillo") || lower.contains("brazo") -> "biceps"
            lower.contains("espalda") || lower.contains("remo") || lower.contains("dominada") || lower.contains("jalon") || lower.contains("dorsal") || lower.contains("deadlift") || lower.contains("muerto") -> "back"
            lower.contains("hombro") || lower.contains("militar") || lower.contains("lateral") || lower.contains("pajaro") || lower.contains("deltoid") -> "shoulders"
            lower.contains("pierna") || lower.contains("sentadilla") || lower.contains("prensa") || lower.contains("cuadricep") || lower.contains("femoral") || lower.contains("gemelo") || lower.contains("zancada") || lower.contains("squat") -> "legs"
            lower.contains("abdom") || lower.contains("crunch") || lower.contains("plancha") || lower.contains("core") || lower.contains("rueda") -> "abs"
            lower.contains("cardio") || lower.contains("cinta") || lower.contains("bici") || lower.contains("elipt") || lower.contains("correr") -> "cardio"
            else -> "chest"
        }
    }
}

