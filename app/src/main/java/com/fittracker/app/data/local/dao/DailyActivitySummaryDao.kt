package com.fittracker.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fittracker.app.data.local.entities.DailyActivitySummary
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyActivitySummaryDao {
    @Query("SELECT * FROM daily_activity_summary WHERE date = :date LIMIT 1")
    fun getActivityForDate(date: String): Flow<DailyActivitySummary?>

    @Query("SELECT * FROM daily_activity_summary WHERE date = :date LIMIT 1")
    suspend fun getActivityForDateDirect(date: String): DailyActivitySummary?

    @Query("SELECT * FROM daily_activity_summary WHERE date >= :sinceDate ORDER BY date ASC")
    suspend fun getActivityHistorySince(sinceDate: String): List<DailyActivitySummary>

    @Query("SELECT * FROM daily_activity_summary WHERE date >= :sinceDate ORDER BY date ASC")
    fun getActivityHistorySinceFlow(sinceDate: String): Flow<List<DailyActivitySummary>>

    @Query("SELECT * FROM daily_activity_summary ORDER BY date ASC")
    fun getAllActivitiesFlow(): Flow<List<DailyActivitySummary>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertActivitySummary(summary: DailyActivitySummary)

    @Query("DELETE FROM daily_activity_summary")
    suspend fun deleteAll()
}
