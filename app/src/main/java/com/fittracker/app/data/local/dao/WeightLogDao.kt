package com.fittracker.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fittracker.app.data.local.entities.WeightLog
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightLogDao {
    @Query("SELECT * FROM weight_logs ORDER BY date DESC, id DESC")
    fun getAllLogs(): Flow<List<WeightLog>>

    @Query("SELECT * FROM weight_logs ORDER BY date DESC, id DESC LIMIT 1")
    fun getLatestLog(): Flow<WeightLog?>

    @Query("SELECT * FROM weight_logs ORDER BY date DESC, id DESC LIMIT 1")
    suspend fun getLatestLogDirect(): WeightLog?

    @Query("SELECT * FROM weight_logs WHERE date = :date LIMIT 1")
    suspend fun getLogByDate(date: String): WeightLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: WeightLog): Long

    @Delete
    suspend fun deleteLog(log: WeightLog)

    @Query("DELETE FROM weight_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("DELETE FROM weight_logs")
    suspend fun deleteAll()
}
