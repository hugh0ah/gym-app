package com.fittracker.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fittracker.app.data.local.entities.FoodLog
import kotlinx.coroutines.flow.Flow

data class DailyMacroTotals(
    val totalCalories: Double = 0.0,
    val totalProtein: Double = 0.0,
    val totalCarbs: Double = 0.0,
    val totalFat: Double = 0.0
)

@Dao
interface FoodLogDao {
    @Query("SELECT * FROM food_logs WHERE date = :date ORDER BY id DESC")
    fun getLogsByDate(date: String): Flow<List<FoodLog>>

    @Query("SELECT * FROM food_logs WHERE date = :date AND mealType = :mealType ORDER BY id DESC")
    fun getLogsByDateAndMeal(date: String, mealType: String): Flow<List<FoodLog>>

    @Query("""
        SELECT 
            COALESCE(SUM(calories), 0.0) AS totalCalories,
            COALESCE(SUM(protein), 0.0) AS totalProtein,
            COALESCE(SUM(carbs), 0.0) AS totalCarbs,
            COALESCE(SUM(fat), 0.0) AS totalFat
        FROM food_logs
        WHERE date = :date
    """)
    fun getMacroTotalsByDate(date: String): Flow<DailyMacroTotals>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(foodLog: FoodLog): Long

    @Delete
    suspend fun deleteLog(foodLog: FoodLog)

    @Query("DELETE FROM food_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("DELETE FROM food_logs")
    suspend fun deleteAll()
}
