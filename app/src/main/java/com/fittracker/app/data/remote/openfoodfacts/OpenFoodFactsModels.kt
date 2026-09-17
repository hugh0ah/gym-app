package com.fittracker.app.data.remote.openfoodfacts

import com.google.gson.annotations.SerializedName

data class OpenFoodSearchResponse(
    @SerializedName("count") val count: Int? = null,
    @SerializedName("page") val page: Int? = null,
    @SerializedName("products") val products: List<OpenFoodProduct>? = null
)

data class OpenFoodProductResponse(
    @SerializedName("status") val status: Int? = null,
    @SerializedName("code") val code: String? = null,
    @SerializedName("product") val product: OpenFoodProduct? = null
)

data class OpenFoodProduct(
    @SerializedName("code") val code: String? = null,
    @SerializedName("product_name") val productName: String? = null,
    @SerializedName("product_name_es") val productNameEs: String? = null,
    @SerializedName("brands") val brands: String? = null,
    @SerializedName("nutriments") val nutriments: OpenFoodNutriments? = null,
    @SerializedName("image_url") val imageUrl: String? = null
) {
    val displayName: String
        get() = productNameEs?.takeIf { it.isNotBlank() }
            ?: productName?.takeIf { it.isNotBlank() }
            ?: "Alimento sin nombre"

    val caloriesPer100g: Double
        get() = nutriments?.energyKcal100g
            ?: nutriments?.energyKcal
            ?: (nutriments?.energy100g?.let { it / 4.184 })
            ?: 0.0

    val proteinPer100g: Double
        get() = nutriments?.proteins100g ?: nutriments?.proteins ?: 0.0

    val carbsPer100g: Double
        get() = nutriments?.carbohydrates100g ?: nutriments?.carbohydrates ?: 0.0

    val fatPer100g: Double
        get() = nutriments?.fat100g ?: nutriments?.fat ?: 0.0
}

data class OpenFoodNutriments(
    @SerializedName("energy-kcal_100g") val energyKcal100g: Double? = null,
    @SerializedName("energy-kcal") val energyKcal: Double? = null,
    @SerializedName("energy_100g") val energy100g: Double? = null,
    @SerializedName("proteins_100g") val proteins100g: Double? = null,
    @SerializedName("proteins") val proteins: Double? = null,
    @SerializedName("carbohydrates_100g") val carbohydrates100g: Double? = null,
    @SerializedName("carbohydrates") val carbohydrates: Double? = null,
    @SerializedName("fat_100g") val fat100g: Double? = null,
    @SerializedName("fat") val fat: Double? = null
)
