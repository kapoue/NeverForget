package com.neverforget.domain.usecase

import com.neverforget.data.model.Task
import com.neverforget.data.model.TaskStatus
import com.neverforget.data.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.*
import javax.inject.Inject

/**
 * Use case pour récupérer les tâches avec leur statut calculé et tri par urgence
 */
class GetTasksWithStatusUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    
    /**
     * Récupère toutes les tâches avec leur statut calculé, triées par urgence
     */
    fun execute(): Flow<List<Task>> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        
        return taskRepository.getAllTasks().map { tasks ->
            tasks
                .map { task -> task.withCalculatedStatus(today) }
                .sortedWith(compareBy<Task> { it.status.priority }.thenBy { it.daysUntilDue })
        }
    }
    
    /**
     * Récupère les tâches en retard
     */
    fun getOverdueTasks(): Flow<List<Task>> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        
        return taskRepository.getAllTasks().map { tasks ->
            tasks
                .map { task -> task.withCalculatedStatus(today) }
                .filter { it.status.isOverdue }
                .sortedBy { it.daysUntilDue } // Plus en retard en premier
        }
    }
    
    /**
     * Récupère les tâches à faire aujourd'hui
     */
    fun getTodayTasks(): Flow<List<Task>> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        
        return taskRepository.getAllTasks().map { tasks ->
            tasks
                .map { task -> task.withCalculatedStatus(today) }
                .filter { it.status.isDueToday }
        }
    }
    
    /**
     * Récupère les tâches à venir dans les X prochains jours
     */
    fun getUpcomingTasks(daysAhead: Int = 7): Flow<List<Task>> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val futureDate = today.plus(daysAhead, DateTimeUnit.DAY)
        
        return taskRepository.getAllTasks().map { tasks ->
            tasks
                .map { task -> task.withCalculatedStatus(today) }
                .filter { task -> 
                    task.nextDueDate > today && task.nextDueDate <= futureDate 
                }
                .sortedBy { it.nextDueDate }
        }
    }
}

/**
 * Extensions pour calculer le statut d'une tâche
 */
private fun Task.withCalculatedStatus(today: LocalDate): Task {
    val daysUntilDue = calculateDaysUntilDue(today, nextDueDate)
    val status = calculateTaskStatus(today, nextDueDate)
    
    return this.copy(
        status = status,
        daysUntilDue = kotlin.math.abs(daysUntilDue)
    )
}

/**
 * Calcule le nombre de jours jusqu'à l'échéance (négatif si en retard)
 */
private fun calculateDaysUntilDue(today: LocalDate, dueDate: LocalDate): Int {
    return dueDate.toEpochDays() - today.toEpochDays()
}

/**
 * Calcule le statut d'une tâche selon sa date d'échéance
 */
private fun calculateTaskStatus(today: LocalDate, dueDate: LocalDate): TaskStatus {
    return when {
        dueDate < today -> TaskStatus.OVERDUE
        dueDate == today -> TaskStatus.DUE_TODAY
        else -> TaskStatus.OK
    }
}

/**
 * Extensions pour les propriétés de statut
 */
private val TaskStatus.priority: Int
    get() = when (this) {
        TaskStatus.OVERDUE -> 0
        TaskStatus.DUE_TODAY -> 1
        TaskStatus.OK -> 2
    }

private val TaskStatus.isOverdue: Boolean
    get() = this == TaskStatus.OVERDUE

private val TaskStatus.isDueToday: Boolean
    get() = this == TaskStatus.DUE_TODAY