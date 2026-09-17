package com.fittracker.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fittracker.app.data.local.entities.WaterLog
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterLogDao {
    @Query("SELECT * FROM water_logs WHERE date = :date ORDER BY timestamp DESC")
    fun getLogsByDate(date: String): Flow<List<WaterLog>>

    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM water_logs WHERE date = :date")
    fun getTotalWaterForDate(date: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(waterLog: WaterLog): Long

    @Delete
    suspend fun deleteLog(waterLog: WaterLog)

    @Query("DELETE FROM water_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("DELETE FROM water_logs WHERE id = (SELECT id FROM water_logs WHERE date = :date ORDER BY timestamp DESC LIMIT 1)")
    suspend fun deleteLastLogForDate(date: String)

    @Query("DELETE FROM water_logs WHERE date = :date")
    suspend fun clearWaterForDate(date: String)

    @Query("DELETE FROM water_logs")
    suspend fun deleteAll()
}
