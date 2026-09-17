package com.fittracker.app.data.remote.gemini

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: GeminiApi = retrofit.create(GeminiApi::class.java)

    /**
     * Declara las herramientas disponibles para el asistente de entrenamiento y recomposición.
     */
    fun createAssistantTools(): List<Tool> {
        val getRoutineDeclaration = FunctionDeclaration(
            name = "get_routine",
            description = "Devuelve los ejercicios, series, repeticiones objetivo, peso y días de la semana de la rutina especificada.",
            parameters = FunctionParameters(
                type = "OBJECT",
                properties = mapOf(
                    "routineId" to ParameterProperty(
                        type = "INTEGER",
                        description = "ID numérico de la rutina a consultar."
                    )
                ),
                required = listOf("routineId")
            )
        )

        val getWorkoutHistoryDeclaration = FunctionDeclaration(
            name = "get_workout_history",
            description = "Devuelve el progreso real de series, repeticiones y peso registrado para un ejercicio en las últimas semanas.",
            parameters = FunctionParameters(
                type = "OBJECT",
                properties = mapOf(
                    "exerciseId" to ParameterProperty(
                        type = "STRING",
                        description = "Identificador del ejercicio (ej. 'barbell_bench_press', 'barbell_squat', 'pull_up')."
                    ),
                    "weeks" to ParameterProperty(
                        type = "INTEGER",
                        description = "Número de semanas de historial a consultar (por defecto 4)."
                    )
                ),
                required = listOf("exerciseId")
            )
        )

        val getActivityHistoryDeclaration = FunctionDeclaration(
            name = "get_activity_history",
            description = "Devuelve el historial reciente de actividad diaria del usuario (pasos, calorías activas, minutos de ejercicio y tipo de cardio como running o natación).",
            parameters = FunctionParameters(
                type = "OBJECT",
                properties = mapOf(
                    "weeks" to ParameterProperty(
                        type = "INTEGER",
                        description = "Número de semanas hacia atrás para inspeccionar la actividad (por defecto 2)."
                    )
                ),
                required = listOf("weeks")
            )
        )

        val getBodyCompositionDeclaration = FunctionDeclaration(
            name = "get_body_composition_history",
            description = "Devuelve el historial de composición corporal de la báscula inteligente: peso, IMC, % grasa corporal, masa muscular en kg, grasa visceral, agua corporal y TMB.",
            parameters = FunctionParameters(
                type = "OBJECT",
                properties = mapOf(
                    "weeks" to ParameterProperty(
                        type = "INTEGER",
                        description = "Número de semanas de historial de composición corporal (por defecto 4)."
                    )
                ),
                required = listOf("weeks")
            )
        )

        val proposeRoutineChangeDeclaration = FunctionDeclaration(
            name = "propose_routine_change",
            description = "Propone una modificación concreta a un ejercicio de la rutina (aumento/disminución de series, reps o peso) basada en el progreso, estancamiento o composición corporal. NO aplica el cambio directamente; genera una propuesta para que el usuario la acepte.",
            parameters = FunctionParameters(
                type = "OBJECT",
                properties = mapOf(
                    "routineId" to ParameterProperty(
                        type = "INTEGER",
                        description = "ID de la rutina a modificar."
                    ),
                    "exerciseId" to ParameterProperty(
                        type = "STRING",
                        description = "ID del ejercicio a modificar."
                    ),
                    "proposedSets" to ParameterProperty(
                        type = "INTEGER",
                        description = "Nuevo número propuesto de series."
                    ),
                    "proposedReps" to ParameterProperty(
                        type = "INTEGER",
                        description = "Nuevo número propuesto de repeticiones."
                    ),
                    "proposedWeight" to ParameterProperty(
                        type = "NUMBER",
                        description = "Nuevo peso propuesto en kg."
                    ),
                    "dayOfWeek" to ParameterProperty(
                        type = "INTEGER",
                        description = "Día de la semana (1 = Lunes .. 7 = Domingo)."
                    ),
                    "justification" to ParameterProperty(
                        type = "STRING",
                        description = "Explicación concisa y fundamentada del cambio."
                    )
                ),
                required = listOf("routineId", "exerciseId", "proposedSets", "proposedReps", "proposedWeight", "dayOfWeek", "justification")
            )
        )

        val createRoutineDeclaration = FunctionDeclaration(
            name = "create_routine",
            description = "Crea inmediatamente una nueva rutina completa en la base de datos de la app con su nombre y ejercicios organizados por días.",
            parameters = FunctionParameters(
                type = "OBJECT",
                properties = mapOf(
                    "name" to ParameterProperty(type = "STRING", description = "Nombre de la rutina (ej. 'Full-Body 3 Días', 'Torso Pierna', etc.)"),
                    "exercises" to ParameterProperty(
                        type = "ARRAY",
                        description = "Lista de ejercicios con nombre, día (1=Lun..7=Dom), series, reps y peso inicial sugerido.",
                        items = ParameterProperty(
                            type = "STRING",
                            description = "Detalle del ejercicio en formato texto o JSON: nombre, día, series, reps, peso (ej. 'Press de banca plano con barra | Día 1 | 4x8 | 70kg')"
                        )
                    )
                ),
                required = listOf("name", "exercises")
            )
        )

        val logFoodDeclaration = FunctionDeclaration(
            name = "log_food",
            description = "Registra una comida o alimento en el diario de nutrición del usuario para el día de hoy con sus calorías y macronutrientes.",
            parameters = FunctionParameters(
                type = "OBJECT",
                properties = mapOf(
                    "foodName" to ParameterProperty(type = "STRING", description = "Nombre del alimento o comida (ej. 'Avena con leche y plátano', 'Pechuga de pollo con arroz')."),
                    "calories" to ParameterProperty(type = "NUMBER", description = "Calorías totales del alimento en kcal."),
                    "protein" to ParameterProperty(type = "NUMBER", description = "Gramos de proteína."),
                    "carbs" to ParameterProperty(type = "NUMBER", description = "Gramos de carbohidratos."),
                    "fat" to ParameterProperty(type = "NUMBER", description = "Gramos de grasa."),
                    "quantityG" to ParameterProperty(type = "NUMBER", description = "Cantidad en gramos (por defecto 100)."),
                    "mealType" to ParameterProperty(type = "STRING", description = "Momento del día o tipo de comida: 'Desayuno', 'Comida', 'Cena' o 'Snack'.")
                ),
                required = listOf("foodName", "calories", "protein", "carbs", "fat")
            )
        )

        val logBodyWeightDeclaration = FunctionDeclaration(
            name = "log_body_weight",
            description = "Anota un nuevo pesaje corporal en kg para el usuario en la fecha actual.",
            parameters = FunctionParameters(
                type = "OBJECT",
                properties = mapOf(
                    "weightKg" to ParameterProperty(type = "NUMBER", description = "Peso corporal en kilogramos (ej. 75.5)."),
                    "notes" to ParameterProperty(type = "STRING", description = "Notas opcionales sobre el pesaje (ej. 'En ayunas', 'Tras entreno').")
                ),
                required = listOf("weightKg")
            )
        )

        val logScaleMeasurementDeclaration = FunctionDeclaration(
            name = "log_smart_scale_measurement",
            description = "Registra una medición de báscula inteligente con peso y bioimpedancia (grasa %, músculo kg, agua %, IMC, etc.).",
            parameters = FunctionParameters(
                type = "OBJECT",
                properties = mapOf(
                    "weightKg" to ParameterProperty(type = "NUMBER", description = "Peso en kg."),
                    "bodyFatPercentage" to ParameterProperty(type = "NUMBER", description = "% de grasa corporal (ej. 16.5)."),
                    "muscleMassKg" to ParameterProperty(type = "NUMBER", description = "Masa muscular en kg (ej. 60.2)."),
                    "bmi" to ParameterProperty(type = "NUMBER", description = "Índice de masa corporal IMC."),
                    "visceralFat" to ParameterProperty(type = "INTEGER", description = "Nivel de grasa visceral 1-20."),
                    "bodyWaterPercentage" to ParameterProperty(type = "NUMBER", description = "% de agua corporal."),
                    "bmrKcal" to ParameterProperty(type = "NUMBER", description = "Tasa metabólica basal en kcal.")
                ),
                required = listOf("weightKg")
            )
        )

        val logWorkoutDeclaration = FunctionDeclaration(
            name = "log_workout_set",
            description = "Registra un entrenamiento o serie de fuerza completada en el gimnasio.",
            parameters = FunctionParameters(
                type = "OBJECT",
                properties = mapOf(
                    "exerciseId" to ParameterProperty(type = "STRING", description = "Nombre o ID del ejercicio (ej. 'Press de banca', 'Sentadilla con barra')."),
                    "sets" to ParameterProperty(type = "INTEGER", description = "Número de series realizadas."),
                    "reps" to ParameterProperty(type = "INTEGER", description = "Número de repeticiones realizadas por serie."),
                    "weightKg" to ParameterProperty(type = "NUMBER", description = "Carga o peso utilizado en kg."),
                    "notes" to ParameterProperty(type = "STRING", description = "Notas de sensaciones, RPE o descanso.")
                ),
                required = listOf("exerciseId", "sets", "reps", "weightKg")
            )
        )

        val logCardioDeclaration = FunctionDeclaration(
            name = "log_cardio_activity",
            description = "Registra pasos, calorías activas y minutos de ejercicio cardio para hoy.",
            parameters = FunctionParameters(
                type = "OBJECT",
                properties = mapOf(
                    "steps" to ParameterProperty(type = "INTEGER", description = "Número de pasos caminados."),
                    "activeCalories" to ParameterProperty(type = "NUMBER", description = "Calorías activas quemadas."),
                    "exerciseMinutes" to ParameterProperty(type = "INTEGER", description = "Minutos de entrenamiento."),
                    "exerciseType" to ParameterProperty(type = "STRING", description = "Tipo de ejercicio (ej. 'Caminar', 'Running', 'Natación').")
                ),
                required = listOf("steps", "activeCalories", "exerciseMinutes")
            )
        )

        val updateTargetsDeclaration = FunctionDeclaration(
            name = "update_nutrition_targets",
            description = "Actualiza los objetivos diarios de calorías y macronutrientes del usuario.",
            parameters = FunctionParameters(
                type = "OBJECT",
                properties = mapOf(
                    "calorieTarget" to ParameterProperty(type = "NUMBER", description = "Meta diaria de calorías en kcal."),
                    "proteinTarget" to ParameterProperty(type = "NUMBER", description = "Meta diaria de proteínas en gramos."),
                    "carbTarget" to ParameterProperty(type = "NUMBER", description = "Meta diaria de carbohidratos en gramos."),
                    "fatTarget" to ParameterProperty(type = "NUMBER", description = "Meta diaria de grasas en gramos.")
                ),
                required = listOf("calorieTarget", "proteinTarget", "carbTarget", "fatTarget")
            )
        )

        val clearDataDeclaration = FunctionDeclaration(
            name = "clear_all_data",
            description = "Reinicia y borra todos los registros y datos del usuario para empezar completamente desde cero cuando el usuario lo solicite expresamente.",
            parameters = FunctionParameters(
                type = "OBJECT",
                properties = mapOf(
                    "confirm" to ParameterProperty(type = "BOOLEAN", description = "Confirmación obligatoria (true) para borrar los datos.")
                ),
                required = listOf("confirm")
            )
        )

        val logWaterDeclaration = FunctionDeclaration(
            name = "log_water_intake",
            description = "Registra agua consumida en mililitros para hoy (ej. 250 para un vaso, 500 para una botella de medio litro).",
            parameters = FunctionParameters(
                type = "OBJECT",
                properties = mapOf(
                    "amountMl" to ParameterProperty(type = "INTEGER", description = "Mililitros de agua ingeridos (ej. 250, 500).")
                ),
                required = listOf("amountMl")
            )
        )

        return listOf(
            Tool(
                functionDeclarations = listOf(
                    getRoutineDeclaration,
                    getWorkoutHistoryDeclaration,
                    getActivityHistoryDeclaration,
                    getBodyCompositionDeclaration,
                    proposeRoutineChangeDeclaration,
                    createRoutineDeclaration,
                    logFoodDeclaration,
                    logWaterDeclaration,
                    logBodyWeightDeclaration,
                    logScaleMeasurementDeclaration,
                    logWorkoutDeclaration,
                    logCardioDeclaration,
                    updateTargetsDeclaration,
                    clearDataDeclaration
                )
            )
        )
    }

    fun createSystemInstruction(): Content {
        return Content(
            role = "system",
            parts = listOf(
                Part(
                    text = """
                    Eres un Asistente y Entrenador Personal Autónomo de Élite integrado directamente en la app FitTracker.
                    
                    ¡TIENES AUTORIDAD Y PERMISOS COMPLETOS PARA EJECUTAR CUALQUIER ACCIÓN EN LA BASE DE DATOS DE LA APP!
                    El usuario ha autorizado expresamente la ejecución de todas las acciones ("allow all / no te limites a respuestas"). Por lo tanto, NUNCA te limites a dar consejos teóricos si puedes ejecutar la acción real llamando a las herramientas correspondientes:
                    
                    HERRAMIENTAS DE ACCIÓN INMEDIATA:
                    1. Si el usuario te pide crear o diseñar una rutina ("hazme una rutina", "crea una rutina de hipertrofia", etc.):
                       - LLAMA a `create_routine` enviando el nombre de la rutina y la lista completa de ejercicios con series, reps, peso inicial y día de la semana (1=Lun..7=Dom).
                       - Además, en tu respuesta explica los ejercicios, técnica, tiempos de descanso (2-3 min en multiarticulares, 60-90s en accesorios) y sobrecarga progresiva.
                    2. Si el usuario te indica qué ha comido o consumido ("me comí 200g de pechuga con arroz", "desayuné avena con proteína"):
                       - Calcula o estima las calorías y macronutrientes (proteína, carbohidratos, grasas).
                       - LLAMA a `log_food` para registrarlo inmediatamente en su diario nutricional.
                    3. Si el usuario te dice su peso ("peso 75.3 kg", "hoy di 80 en la báscula"):
                       - LLAMA a `log_body_weight` para guardarlo en el registro de pesaje.
                    4. Si el usuario te dice qué ejercicios entrenó ("hice press de banca 4x8 con 80kg"):
                       - LLAMA a `log_workout_set` para guardar el registro de levantamiento.
                    5. Si el usuario reporta pasos, cardio o actividad ("hice 8000 pasos y quemé 350 kcal"):
                       - LLAMA a `log_cardio_activity` para guardarlo en la actividad diaria.
                    6. Si el usuario quiere cambiar sus objetivos nutricionales ("quiero subir a 2500 kcal y 160g proteína"):
                       - LLAMA a `update_nutrition_targets` para actualizar sus metas en su perfil.
                    7. Si el usuario pide reiniciar la app, borrar registros o empezar de cero:
                       - LLAMA a `clear_all_data` con confirm=true para limpiar la base de datos a cero.
                    8. Si el usuario te consulta su progreso o estado físico:
                       - LLAMA a `get_routine`, `get_workout_history`, `get_activity_history` o `get_body_composition_history` para analizar sus datos reales.
                       - Si detectas que debe subir o bajar peso en algún ejercicio, LLAMA a `propose_routine_change`.
                    
                    Comunícate siempre en español, con un tono motivador, técnico, cercano, profesional y con formato markdown limpio. ¡Haz que las cosas sucedan en la app!
                    """.trimIndent()
                )
            )
        )
    }
}
