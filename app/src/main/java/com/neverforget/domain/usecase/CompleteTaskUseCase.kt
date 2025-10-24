package com.neverforget.domain.usecase

import com.neverforget.data.repository.TaskRepository
import com.neverforget.notifications.NotificationScheduler

import kotlinx.datetime.*
import javax.inject.Inject

/**
 * Use case pour valider une tâche et calculer la prochaine échéance
 */
class CompleteTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val notificationScheduler: NotificationScheduler
) {
    
    /**
     * Valide une tâche avec la date actuelle
     */
    suspend fun execute(taskId: String): Result<Unit> {
        return execute(taskId, Clock.System.todayIn(TimeZone.currentSystemDefault()))
    }
    
    /**
     * Valide une tâche avec une date spécifique
     */
    suspend fun execute(taskId: String, completionDate: LocalDate): Result<Unit> {
        return try {
            
            
            val task = taskRepository.getTaskById(taskId)
            if (task == null) {
                
                return Result.failure(Exception("Tâche non trouvée"))
            }
            
            
            
            // Calculer la prochaine échéance
            val nextDueDate = calculateNextDueDate(completionDate, task.recurrenceDays)
            
            
            // Mettre à jour la tâche
            val updatedTask = task.copy(nextDueDate = nextDueDate)
            taskRepository.updateTask(updatedTask)
            
            
            // Ajouter à l'historique
            taskRepository.completeTask(taskId, completionDate)
            
            
            // Replanifier les notifications pour la nouvelle échéance
            notificationScheduler.scheduleTaskNotification(
                taskId = taskId,
                taskName = task.name,
                dueDate = nextDueDate
            )
            
            
            
            Result.success(Unit)
        } catch (e: Exception) {
            
            Result.failure(e)
        }
    }
    
    /**
     * Calcule la prochaine échéance basée sur la date de validation et la récurrence
     */
    private fun calculateNextDueDate(completionDate: LocalDate, recurrenceDays: Int): LocalDate {
        return completionDate.plus(recurrenceDays, DateTimeUnit.DAY)
    }
}