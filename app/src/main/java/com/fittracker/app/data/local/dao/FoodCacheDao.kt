package com.fittracker.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fittracker.app.data.local.entities.FoodCache

@Dao
interface FoodCacheDao {
    @Query("SELECT * FROM food_cache WHERE code = :code LIMIT 1")
    suspend fun getByCode(code: String): FoodCache?

    @Query("SELECT * FROM food_cache WHERE name LIKE '%' || :query || '%' ORDER BY cachedAt DESC LIMIT 20")
    suspend fun searchByName(query: String): List<FoodCache>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: FoodCache)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoods(foods: List<FoodCache>)
}
