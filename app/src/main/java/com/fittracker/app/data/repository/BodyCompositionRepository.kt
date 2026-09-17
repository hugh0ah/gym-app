package com.fittracker.app.data.repository

import com.fittracker.app.data.local.dao.BodyCompositionDao
import com.fittracker.app.data.local.dao.WeightLogDao
import com.fittracker.app.data.local.entities.BodyCompositionLog
import com.fittracker.app.data.local.entities.WeightLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class BodyCompositionRepository(
    private val bodyCompositionDao: BodyCompositionDao,
    private val weightLogDao: WeightLogDao,
    private val userProfileRepository: UserProfileRepository
) {
    val allLogs: Flow<List<BodyCompositionLog>> = bodyCompositionDao.getAllLogs()
    val chronologicalLogs: Flow<List<BodyCompositionLog>> = bodyCompositionDao.getLogsChronological()
    val latestLog: Flow<BodyCompositionLog?> = bodyCompositionDao.getLatestLog()

    suspend fun getLatestLogDirect(): BodyCompositionLog? {
        return bodyCompositionDao.getLatestLogDirect()
    }

    suspend fun saveLog(log: BodyCompositionLog): Long {
        val insertedId = bodyCompositionDao.insertLog(log)
        // Sincronizar también con el registro de peso y con el perfil de usuario
        weightLogDao.insertLog(
            WeightLog(
                date = log.date,
                weightKg = log.weightKg,
                notes = "Báscula inteligente: ${log.bodyFatPercentage}% grasa, ${log.muscleMassKg}kg músculo"
            )
        )
        userProfileRepository.syncProfileWeight(log.weightKg)
        return insertedId
    }

    suspend fun deleteLog(id: Long) {
        bodyCompositionDao.deleteLogById(id)
    }

    suspend fun getHistory(weeks: Int): List<BodyCompositionLog> {
        val sinceDate = LocalDate.now().minusWeeks(weeks.toLong()).toString()
        return bodyCompositionDao.getHistorySince(sinceDate)
    }

    suspend fun clearAllBodyCompositionLogs() {
        bodyCompositionDao.deleteAll()
    }
}
