package com.fittracker.app.data.repository

import com.fittracker.app.data.local.dao.DailyMacroTotals
import com.fittracker.app.data.local.dao.FoodCacheDao
import com.fittracker.app.data.local.dao.FoodLogDao
import com.fittracker.app.data.local.dao.UserTargetsDao
import com.fittracker.app.data.local.entities.FoodCache
import com.fittracker.app.data.local.entities.FoodLog
import com.fittracker.app.data.local.entities.UserTargets
import com.fittracker.app.data.remote.openfoodfacts.OpenFoodFactsClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FoodRepository(
    private val foodLogDao: FoodLogDao,
    private val foodCacheDao: FoodCacheDao,
    private val userTargetsDao: UserTargetsDao
) {
    val userTargets: Flow<UserTargets?> = userTargetsDao.getUserTargetsFlow()

    fun getLogsForDate(date: String): Flow<List<FoodLog>> {
        return foodLogDao.getLogsByDate(date)
    }

    fun getMacroTotalsForDate(date: String): Flow<DailyMacroTotals> {
        return foodLogDao.getMacroTotalsByDate(date)
    }

    suspend fun addFoodLog(
        date: String,
        foodName: String,
        calories: Double,
        protein: Double,
        carbs: Double,
        fat: Double,
        quantityG: Double,
        mealType: String = "Comida"
    ): Long {
        return foodLogDao.insertLog(
            FoodLog(
                date = date,
                foodName = foodName,
                calories = calories,
                protein = protein,
                carbs = carbs,
                fat = fat,
                quantityG = quantityG,
                mealType = mealType
            )
        )
    }

    suspend fun deleteFoodLog(id: Long) {
        foodLogDao.deleteLogById(id)
    }

    suspend fun updateUserTargets(targets: UserTargets) {
        userTargetsDao.upsertUserTargets(targets)
    }

    suspend fun clearAllFoodLogs() {
        foodLogDao.deleteAll()
    }

    /**
     * Busca alimentos mediante Open Food Facts y los guarda en food_cache.
     * En caso de error de conexión u offline, consulta la base de datos local food_cache o el catálogo integrado.
     */
    suspend fun searchFood(query: String): List<FoodCache> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()

        // Prioridad 1: si coincide con alimentos comunes fitness de alta calidad
        val matchedCommon = COMMON_FOODS.filter { it.name.contains(query, ignoreCase = true) }
        if (matchedCommon.isNotEmpty()) {
            return@withContext matchedCommon
        }

        try {
            val response = OpenFoodFactsClient.api.searchProducts(terms = query)
            if (response.isSuccessful) {
                val products = response.body()?.products.orEmpty()
                if (products.isNotEmpty()) {
                    val cachedList = products.mapNotNull { product ->
                        val code = product.code ?: return@mapNotNull null
                        FoodCache(
                            code = code,
                            name = product.displayName,
                            caloriesPer100g = product.caloriesPer100g,
                            proteinPer100g = product.proteinPer100g,
                            carbsPer100g = product.carbsPer100g,
                            fatPer100g = product.fatPer100g,
                            brand = product.brands,
                            cachedAt = System.currentTimeMillis()
                        )
                    }
                    if (cachedList.isNotEmpty()) {
                        foodCacheDao.insertFoods(cachedList)
                        return@withContext cachedList
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Fallback a caché local de Room
        val localCached = foodCacheDao.searchByName(query)
        if (localCached.isNotEmpty()) {
            return@withContext localCached
        }

        matchedCommon
    }

    companion object {
        val COMMON_FOODS = listOf(
            // --- PROTEÍNAS MAGRAS Y CARNES ---
            FoodCache("com_pechuga_pollo", "Pechuga de pollo (cruda)", 120.0, 22.5, 0.0, 2.6, "Proteínas"),
            FoodCache("com_pechuga_pollo_plancha", "Pechuga de pollo a la plancha", 165.0, 31.0, 0.0, 3.6, "Proteínas"),
            FoodCache("com_solomillo_pavo", "Solomillo de pavo / Pechuga de pavo", 105.0, 24.0, 0.0, 1.0, "Proteínas"),
            FoodCache("com_ternera_magra", "Ternera magra picada / filete", 143.0, 21.0, 0.0, 6.5, "Proteínas"),
            FoodCache("com_lomo_embuchado", "Lomo embuchado extra magro", 195.0, 38.0, 0.5, 4.5, "Proteínas"),
            FoodCache("com_huevo", "Huevo entero campero", 143.0, 12.6, 0.7, 9.5, "Proteínas"),
            FoodCache("com_claras", "Claras de huevo pasteurizadas", 52.0, 11.0, 0.7, 0.2, "Proteínas"),
            FoodCache("com_atun", "Atún al natural en lata (escurrido)", 108.0, 25.0, 0.0, 0.9, "Pescados"),
            FoodCache("com_salmon", "Salmón fresco / al horno", 208.0, 20.0, 0.0, 13.0, "Pescados"),
            FoodCache("com_merluza", "Merluza fresca / al vapor", 82.0, 17.5, 0.0, 1.2, "Pescados"),
            FoodCache("com_langostinos", "Gambas / Langostinos cocidos", 99.0, 21.0, 0.5, 1.5, "Pescados"),
            FoodCache("com_tofu_firme", "Tofu firme natural", 115.0, 12.0, 2.0, 6.0, "Vegano"),
            FoodCache("com_seitan", "Seitán natural", 140.0, 25.0, 4.0, 2.0, "Vegano"),

            // --- SUPLEMENTOS Y BARRITAS ---
            FoodCache("com_whey_protein", "Proteína Whey en polvo (100% aislada/concentrada)", 390.0, 80.0, 4.0, 3.5, "Suplementos"),
            FoodCache("com_caseina", "Caseína micelar en polvo", 360.0, 78.0, 4.5, 1.5, "Suplementos"),
            FoodCache("com_barrita_proteina", "Barrita de proteína fitness (60g ~20g P)", 340.0, 33.0, 30.0, 11.0, "Suplementos"),

            // --- LÁCTEOS Y DERIVADOS SALUDABLES ---
            FoodCache("com_yogur_griego", "Yogur griego natural 0% grasa", 59.0, 10.0, 3.6, 0.2, "Lácteos"),
            FoodCache("com_queso_batido", "Queso fresco batido 0% (Hacendado / Pro)", 46.0, 8.5, 3.5, 0.1, "Lácteos"),
            FoodCache("com_cottage", "Queso Cottage bajo en grasa", 85.0, 12.0, 3.0, 2.5, "Lácteos"),
            FoodCache("com_requeson", "Requesón bajo en grasa", 98.0, 13.0, 3.0, 3.5, "Lácteos"),
            FoodCache("com_leche_desnatada", "Leche de vaca desnatada", 35.0, 3.4, 4.8, 0.2, "Lácteos"),
            FoodCache("com_bebida_soja", "Bebida de soja sin azúcares añadidos", 38.0, 3.3, 1.2, 1.8, "Bebidas"),
            FoodCache("com_bebida_almendras", "Bebida de almendras zero azúcar", 14.0, 0.5, 0.2, 1.1, "Bebidas"),

            // --- CARBOHIDRATOS COMPLEJOS Y CEREALES ---
            FoodCache("com_arroz_blanco", "Arroz blanco (en seco)", 360.0, 7.0, 79.0, 0.9, "Carbohidratos"),
            FoodCache("com_arroz_cocido", "Arroz blanco cocido", 130.0, 2.7, 28.0, 0.3, "Carbohidratos"),
            FoodCache("com_arroz_integral", "Arroz integral (en seco)", 355.0, 7.5, 75.0, 2.5, "Carbohidratos"),
            FoodCache("com_avena", "Copos de avena integrales", 370.0, 13.5, 58.7, 7.0, "Carbohidratos"),
            FoodCache("com_harina_avena", "Harina de avena sabor neutro/vainilla", 375.0, 12.0, 65.0, 6.0, "Carbohidratos"),
            FoodCache("com_patata_cocida", "Patata cocida o al microondas", 87.0, 2.0, 20.0, 0.1, "Tubérculos"),
            FoodCache("com_boniato", "Boniato / Batata al horno", 90.0, 2.0, 21.0, 0.2, "Tubérculos"),
            FoodCache("com_pasta_integral", "Pasta integral (en seco)", 348.0, 12.5, 65.0, 2.2, "Carbohidratos"),
            FoodCache("com_pasta_cocida", "Pasta integral cocida", 124.0, 5.0, 25.0, 0.8, "Carbohidratos"),
            FoodCache("com_quinoa", "Quinoa en grano (en seco)", 368.0, 14.0, 64.0, 6.0, "Carbohidratos"),
            FoodCache("com_pan_integral", "Pan 100% integral de centeno o espelta", 245.0, 9.0, 45.0, 2.5, "Panes"),
            FoodCache("com_tortitas_arroz", "Tortitas de arroz o maíz", 380.0, 7.8, 81.0, 1.9, "Snacks"),
            FoodCache("com_legumbres_lentejas", "Lentejas cocidas de bote", 90.0, 7.0, 12.0, 0.5, "Legumbres"),
            FoodCache("com_garbanzos_cocidos", "Garbanzos cocidos de bote", 120.0, 7.5, 17.0, 2.5, "Legumbres"),

            // --- GRASAS SALUDABLES Y FRUTOS SECOS ---
            FoodCache("com_aceite_oliva", "Aceite de oliva virgen extra (AOVE)", 884.0, 0.0, 0.0, 100.0, "Grasas"),
            FoodCache("com_aguacate", "Aguacate fresco Hass", 160.0, 2.0, 8.5, 15.0, "Grasas"),
            FoodCache("com_almendras", "Almendras crudas sin sal", 579.0, 21.0, 22.0, 49.0, "Frutos Secos"),
            FoodCache("com_nueces", "Nueces peladas crudas", 654.0, 15.0, 14.0, 65.0, "Frutos Secos"),
            FoodCache("com_crema_cacahuete", "Crema de cacahuete 100% natural", 588.0, 25.0, 20.0, 50.0, "Grasas"),
            FoodCache("com_semillas_chia", "Semillas de chía", 486.0, 17.0, 42.0, 31.0, "Superalimentos"),
            FoodCache("com_chocolate_negro", "Chocolate negro 85% cacao", 580.0, 9.0, 20.0, 50.0, "Snacks"),

            // --- FRUTAS FRESCAS ---
            FoodCache("com_platano", "Plátano maduro", 89.0, 1.1, 22.8, 0.3, "Frutas"),
            FoodCache("com_manzana", "Manzana con piel", 52.0, 0.3, 14.0, 0.2, "Frutas"),
            FoodCache("com_arandanos", "Arándanos frescos", 57.0, 0.7, 14.5, 0.3, "Frutas"),
            FoodCache("com_fresas", "Fresas frescas", 33.0, 0.7, 7.7, 0.3, "Frutas"),
            FoodCache("com_kiwi", "Kiwi verde", 61.0, 1.1, 15.0, 0.5, "Frutas"),
            FoodCache("com_naranja", "Naranja dulce", 47.0, 0.9, 12.0, 0.1, "Frutas"),
            FoodCache("com_sandia", "Sandía fresca", 30.0, 0.6, 7.5, 0.2, "Frutas"),

            // --- VERDURAS Y HORTALIZAS ---
            FoodCache("com_brocoli", "Brócoli fresco o al vapor", 34.0, 2.8, 7.0, 0.4, "Verduras"),
            FoodCache("com_espinacas", "Espinacas frescas", 23.0, 2.9, 3.6, 0.4, "Verduras"),
            FoodCache("com_esparragos", "Espárragos verdes trigueros", 20.0, 2.2, 3.9, 0.1, "Verduras"),
            FoodCache("com_tomate", "Tomate natural de ensalada", 18.0, 0.9, 3.9, 0.2, "Verduras"),
            FoodCache("com_calabacin", "Calabacín fresco", 17.0, 1.2, 3.1, 0.3, "Verduras"),
            FoodCache("com_ensalada_mixta", "Ensalada mixta verde sin aliñar", 15.0, 1.2, 2.5, 0.2, "Verduras"),

            // --- PLATOS FITNESS POPULARES PREPARADOS ---
            FoodCache("com_plato_arroz_pollo", "Plato Fitness: Arroz con pollo y brócoli (150g pollo, 150g arroz cocido)", 140.0, 16.0, 15.0, 1.8, "Platos"),
            FoodCache("com_plato_tortilla_avena", "Pancake / Tortita de avena y claras", 160.0, 14.0, 22.0, 2.0, "Platos"),
            FoodCache("com_plato_batido_proteico", "Batido Proteína Whey con 1 plátano y leche (300ml)", 110.0, 10.0, 13.0, 1.5, "Platos"),
            FoodCache("com_plato_fajitas_pollo", "Fajita integral de pollo con pimientos y cebolla", 185.0, 15.0, 18.0, 5.0, "Platos"),
            FoodCache("com_plato_ensalada_atun", "Ensalada de pasta con atún, huevo cocido y maíz", 155.0, 9.0, 20.0, 4.0, "Platos")
        )
    }
}
