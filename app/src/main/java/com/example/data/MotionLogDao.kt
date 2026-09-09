package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MotionLogDao {
    @Query("SELECT * FROM motion_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<MotionLog>>

    @Query("SELECT * FROM motion_logs WHERE eventType = :type ORDER BY timestamp DESC")
    fun getLogsByType(type: MotionEventType): Flow<List<MotionLog>>

    @Query("SELECT * FROM motion_logs WHERE details LIKE '%' || :query || '%' OR sensitivity LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchLogs(query: String): Flow<List<MotionLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: MotionLog): Long

    @Query("DELETE FROM motion_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("DELETE FROM motion_logs")
    suspend fun clearAllLogs()

    @Query("SELECT COUNT(*) FROM motion_logs WHERE eventType != 'BACKGROUND_IGNORED'")
    fun getThreatCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM motion_logs WHERE eventType = 'BACKGROUND_IGNORED'")
    fun getIgnoredCount(): Flow<Int>
}
