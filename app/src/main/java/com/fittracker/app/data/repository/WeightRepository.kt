package com.fittracker.app.data.repository

import com.fittracker.app.data.local.dao.WeightLogDao
import com.fittracker.app.data.local.entities.WeightLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class WeightRepository(
    private val weightLogDao: WeightLogDao
) {
    val allLogs: Flow<List<WeightLog>> = weightLogDao.getAllLogs()
    val latestLog: Flow<WeightLog?> = weightLogDao.getLatestLog()

    suspend fun logWeight(weightKg: Double, date: String = LocalDate.now().toString(), notes: String? = null): Long {
        return weightLogDao.insertLog(
            WeightLog(
                date = date,
                weightKg = weightKg,
                notes = notes
            )
        )
    }

    suspend fun deleteWeightLog(id: Long) {
        weightLogDao.deleteLogById(id)
    }

    suspend fun clearAllWeightLogs() {
        weightLogDao.deleteAll()
    }
}
