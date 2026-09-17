package com.fittracker.app.data.remote.gemini

import com.google.gson.annotations.SerializedName

data class GeminiRequest(
    val contents: List<Content>,
    val tools: List<Tool>? = null,
    @SerializedName("system_instruction") val systemInstruction: Content? = null,
    @SerializedName("generationConfig") val generationConfig: GenerationConfig? = null
)

data class GenerationConfig(
    val temperature: Float? = 0.4f,
    val topK: Int? = 40,
    val topP: Float? = 0.95f,
    @SerializedName("response_mime_type") val responseMimeType: String? = null
)

data class Content(
    val role: String? = null, // "user", "model", "function"
    val parts: List<Part>
)

data class Part(
    val text: String? = null,
    @SerializedName("inline_data") val inlineData: InlineData? = null,
    @SerializedName("functionCall") val functionCall: FunctionCall? = null,
    @SerializedName("functionResponse") val functionResponse: FunctionResponse? = null,
    @SerializedName("thoughtSignature") val thoughtSignature: String? = null
)

data class InlineData(
    @SerializedName("mime_type") val mimeType: String,
    val data: String // Base64 encoded
)

data class FunctionCall(
    val name: String,
    val args: Map<String, Any>? = null,
    val id: String? = null
)

data class FunctionResponse(
    val name: String,
    val response: Map<String, Any>,
    val id: String? = null
)

data class Tool(
    val functionDeclarations: List<FunctionDeclaration>
)

data class FunctionDeclaration(
    val name: String,
    val description: String,
    val parameters: FunctionParameters
)

data class FunctionParameters(
    val type: String = "OBJECT",
    val properties: Map<String, ParameterProperty>,
    val required: List<String>? = null
)

data class ParameterProperty(
    val type: String, // "STRING", "INTEGER", "NUMBER", "BOOLEAN", "ARRAY", "OBJECT"
    val description: String,
    val enum: List<String>? = null,
    val items: ParameterProperty? = null,
    val properties: Map<String, ParameterProperty>? = null,
    val required: List<String>? = null
)

data class GeminiResponse(
    val candidates: List<Candidate>? = null,
    val error: GeminiError? = null
)

data class Candidate(
    val content: Content? = null,
    val finishReason: String? = null
)

data class GeminiError(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null
)

data class ActivityExtractionResult(
    val date: String,
    val steps: Int,
    val activeCalories: Double,
    val exerciseMinutes: Int,
    val exerciseType: String
)

data class SmartScaleExtractionResult(
    val date: String = "",
    val weightKg: Double = 0.0,
    val bmi: Double = 0.0,
    val bodyFatPercentage: Double = 0.0,
    val muscleMassKg: Double = 0.0,
    val visceralFat: Int = 0,
    val bmrKcal: Double = 0.0,
    val bodyWaterPercentage: Double = 0.0,
    val boneMassKg: Double = 0.0,
    val proteinPercentage: Double = 0.0
)

data class RoutineChangeProposal(
    val routineId: Long,
    val exerciseId: String,
    val exerciseName: String,
    val proposedSets: Int,
    val proposedReps: Int,
    val proposedWeight: Double,
    val dayOfWeek: Int,
    val justification: String
)
