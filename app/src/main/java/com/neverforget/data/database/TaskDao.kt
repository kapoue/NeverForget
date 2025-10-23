package com.neverforget.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) pour les opérations sur les tâches
 */
@Dao
interface TaskDao {
    
    @Query("SELECT * FROM tasks ORDER BY nextDueDate ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): TaskEntity?
    
    @Query("SELECT * FROM tasks WHERE category = :category")
    suspend fun getTasksByCategory(category: String): List<TaskEntity>
    
    @Insert
    suspend fun insertTask(task: TaskEntity)
    
    @Insert
    suspend fun insertTasks(tasks: List<TaskEntity>)
    
    @Update
    suspend fun updateTask(task: TaskEntity)
    
    @Delete
    suspend fun deleteTask(task: TaskEntity)
    
    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)
    
    @Query("UPDATE tasks SET category = :newCategory WHERE category = :oldCategory")
    suspend fun updateTasksCategory(oldCategory: String, newCategory: String)
    
    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun getTaskCount(): Int
}