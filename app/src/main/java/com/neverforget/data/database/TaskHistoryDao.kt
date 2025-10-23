package com.neverforget.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO pour les opérations sur l'historique des tâches
 */
@Dao
interface TaskHistoryDao {
    
    @Query("SELECT * FROM task_history WHERE taskId = :taskId ORDER BY completedDate DESC")
    fun getHistoryForTask(taskId: String): Flow<List<TaskHistoryEntity>>
    
    @Query("SELECT * FROM task_history WHERE taskId = :taskId ORDER BY completedDate DESC")
    suspend fun getHistoryForTaskSync(taskId: String): List<TaskHistoryEntity>
    
    @Insert
    suspend fun insertHistory(history: TaskHistoryEntity)
    
    @Insert
    suspend fun insertHistories(histories: List<TaskHistoryEntity>)
    
    @Delete
    suspend fun deleteHistory(history: TaskHistoryEntity)
    
    @Query("DELETE FROM task_history WHERE taskId = :taskId")
    suspend fun deleteHistoryForTask(taskId: String)
    
    @Query("SELECT COUNT(*) FROM task_history WHERE taskId = :taskId")
    suspend fun getHistoryCountForTask(taskId: String): Int
}