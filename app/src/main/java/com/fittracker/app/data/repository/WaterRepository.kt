package com.fittracker.app.data.repository

import com.fittracker.app.data.local.dao.WaterLogDao
import com.fittracker.app.data.local.entities.WaterLog
import kotlinx.coroutines.flow.Flow

class WaterRepository(
    private val waterLogDao: WaterLogDao
) {
    fun getWaterForDate(date: String): Flow<Int> {
        return waterLogDao.getTotalWaterForDate(date)
    }

    fun getLogsForDate(date: String): Flow<List<WaterLog>> {
        return waterLogDao.getLogsByDate(date)
    }

    suspend fun addWater(date: String, amountMl: Int): Long {
        return waterLogDao.insertLog(
            WaterLog(
                date = date,
                amountMl = amountMl
            )
        )
    }

    suspend fun undoLastWater(date: String) {
        waterLogDao.deleteLastLogForDate(date)
    }

    suspend fun resetWaterForDate(date: String) {
        waterLogDao.clearWaterForDate(date)
    }

    suspend fun clearAll() {
        waterLogDao.deleteAll()
    }
}
