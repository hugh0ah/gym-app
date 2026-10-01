package com.fittracker.app.data.repository

import com.fittracker.app.data.local.dao.DailyActivitySummaryDao
import com.fittracker.app.data.local.entities.DailyActivitySummary
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class ActivityRepository(
    private val dailyActivitySummaryDao: DailyActivitySummaryDao
) {
    fun getActivityForDate(date: String): Flow<DailyActivitySummary?> {
        return dailyActivitySummaryDao.getActivityForDate(date)
    }

    suspend fun saveActivitySummary(summary: DailyActivitySummary) {
        dailyActivitySummaryDao.upsertActivitySummary(summary)
    }

    suspend fun getActivityHistory(weeks: Int): List<DailyActivitySummary> {
        val sinceDate = LocalDate.now().minusWeeks(weeks.toLong()).toString()
        return dailyActivitySummaryDao.getActivityHistorySince(sinceDate)
    }

    fun getActivityHistoryFlow(weeks: Int): Flow<List<DailyActivitySummary>> {
        val sinceDate = LocalDate.now().minusWeeks(weeks.toLong()).toString()
        return dailyActivitySummaryDao.getActivityHistorySinceFlow(sinceDate)
    }

    fun getAllActivitiesFlow(): Flow<List<DailyActivitySummary>> {
        return dailyActivitySummaryDao.getAllActivitiesFlow()
    }

    suspend fun clearAllActivities() {
        dailyActivitySummaryDao.deleteAll()
    }
}
