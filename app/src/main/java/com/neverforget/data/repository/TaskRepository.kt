package com.neverforget.data.repository

import com.neverforget.data.database.TaskDao
import com.neverforget.data.database.TaskHistoryDao
import com.neverforget.data.database.TaskHistoryEntity
import com.neverforget.data.model.Task
import com.neverforget.data.model.toEntity
import com.neverforget.data.model.toTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository pour la gestion des tâches
 * Couche d'abstraction entre les DAOs et les ViewModels
 */
@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val taskHistoryDao: TaskHistoryDao
) {
    
    /**
     * Récupère toutes les tâches avec leur statut calculé
     */
    fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasksWithHistory().map { tasksWithHistory ->
            tasksWithHistory.map { it.toTask() }
        }
    }
    
    /**
     * Récupère une tâche par son ID avec son historique
     */
    suspend fun getTaskById(taskId: String): Task? {
        return taskDao.getTaskWithHistoryById(taskId)?.toTask()
    }
    
    /**
     * Récupère les tâches d'une catégorie spécifique
     */
    suspend fun getTasksByCategory(category: String): List<Task> {
        val taskEntities = taskDao.getTasksByCategory(category)
        return taskEntities.map { taskEntity ->
            val history = taskHistoryDao.getHistoryForTaskSync(taskEntity.id)
            taskEntity.toTask(history)
        }
    }
    
    /**
     * Insère une nouvelle tâche
     */
    suspend fun insertTask(task: Task) {
        taskDao.insertTask(task.toEntity())
    }
    
    /**
     * Met à jour une tâche existante
     */
    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task.toEntity())
    }
    
    /**
     * Supprime une tâche et son historique
     */
    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task.toEntity())
    }
    
    /**
     * Supprime une tâche par son ID
     */
    suspend fun deleteTaskById(taskId: String) {
        taskDao.deleteTaskById(taskId)
    }
    
    /**
     * Supprime une tâche par son nom
     */
    suspend fun deleteTaskByName(taskName: String) {
        taskDao.deleteTaskByName(taskName)
    }
    
    /**
     * Valide une tâche et calcule la prochaine échéance
     */
    suspend fun completeTask(taskId: String, completedDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())) {
        val task = taskDao.getTaskById(taskId) ?: return
        
        // Ajouter à l'historique
        val historyEntry = TaskHistoryEntity(
            taskId = taskId,
            completedDate = completedDate
        )
        taskHistoryDao.insertHistory(historyEntry)
        
        // Calculer la prochaine échéance
        val nextDueDate = completedDate.plus(task.recurrenceDays, DateTimeUnit.DAY)
        
        // Mettre à jour la tâche
        val updatedTask = task.copy(nextDueDate = nextDueDate)
        taskDao.updateTask(updatedTask)
    }
    
    /**
     * Met à jour la catégorie de toutes les tâches d'une ancienne catégorie
     */
    suspend fun updateTasksCategory(oldCategory: String, newCategory: String) {
        taskDao.updateTasksCategory(oldCategory, newCategory)
    }
    
    /**
     * Récupère le nombre total de tâches
     */
    suspend fun getTaskCount(): Int {
        return taskDao.getTaskCount()
    }
    
    /**
     * Récupère l'historique d'une tâche
     */
    fun getTaskHistory(taskId: String): Flow<List<LocalDate>> {
        return taskHistoryDao.getHistoryForTask(taskId).map { historyEntities ->
            historyEntities.map { it.completedDate }.sortedDescending()
        }
    }
    
    /**
     * Récupère tout l'historique de toutes les tâches
     */
    suspend fun getAllTaskHistory(): List<TaskHistoryEntity> {
        return taskDao.getAllTasksWithHistorySync().flatMap { it.history }
    }
}