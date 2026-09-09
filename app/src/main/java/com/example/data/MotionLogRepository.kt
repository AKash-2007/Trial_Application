package com.example.data

import kotlinx.coroutines.flow.Flow

class MotionLogRepository(private val dao: MotionLogDao) {
    val allLogs: Flow<List<MotionLog>> = dao.getAllLogs()
    val threatCount: Flow<Int> = dao.getThreatCount()
    val ignoredCount: Flow<Int> = dao.getIgnoredCount()

    fun getLogsByType(type: MotionEventType): Flow<List<MotionLog>> {
        return dao.getLogsByType(type)
    }

    fun searchLogs(query: String): Flow<List<MotionLog>> {
        return dao.searchLogs(query)
    }

    suspend fun insertLog(log: MotionLog): Long {
        return dao.insertLog(log)
    }

    suspend fun deleteLogById(id: Long) {
        dao.deleteLogById(id)
    }

    suspend fun clearAllLogs() {
        dao.clearAllLogs()
    }
}
