package com.fittracker.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fittracker.app.data.local.entities.BodyCompositionLog
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyCompositionDao {
    @Query("SELECT * FROM body_composition_logs ORDER BY date DESC, id DESC")
    fun getAllLogs(): Flow<List<BodyCompositionLog>>

    @Query("SELECT * FROM body_composition_logs ORDER BY date ASC, id ASC")
    fun getLogsChronological(): Flow<List<BodyCompositionLog>>

    @Query("SELECT * FROM body_composition_logs ORDER BY date DESC, id DESC LIMIT 1")
    fun getLatestLog(): Flow<BodyCompositionLog?>

    @Query("SELECT * FROM body_composition_logs ORDER BY date DESC, id DESC LIMIT 1")
    suspend fun getLatestLogDirect(): BodyCompositionLog?

    @Query("SELECT * FROM body_composition_logs WHERE date >= :sinceDate ORDER BY date ASC")
    suspend fun getHistorySince(sinceDate: String): List<BodyCompositionLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: BodyCompositionLog): Long

    @Delete
    suspend fun deleteLog(log: BodyCompositionLog)

    @Query("DELETE FROM body_composition_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("DELETE FROM body_composition_logs")
    suspend fun deleteAll()
}
