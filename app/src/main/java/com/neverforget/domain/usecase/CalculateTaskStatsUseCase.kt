package com.neverforget.domain.usecase

import com.neverforget.data.repository.TaskRepository
import com.neverforget.utils.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject

/**
 * Use case pour calculer les statistiques des tâches
 */
class CalculateTaskStatsUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    
    /**
     * Calcule les statistiques globales des tâches
     */
    fun execute(): Flow<TaskStats> = flow {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        
        // Récupérer les données
        val tasks = taskRepository.getAllTasks()
        val allHistory = taskRepository.getAllTaskHistory()
        
        // Collecter les tâches une seule fois
        tasks.collect { taskList ->
            val totalTasks = taskList.size
            val overdueTasks = taskList.count { it.nextDueDate < today }
            val todayTasks = taskList.count { it.nextDueDate == today }
            val upcomingTasks = taskList.count { it.nextDueDate > today }
            
            // Statistiques d'historique
            val totalCompletions = allHistory.size
            val completionsThisMonth = allHistory.count { history ->
                val historyDate = history.completedDate
                historyDate.year == today.year && historyDate.monthNumber == today.monthNumber
            }
            
            // Tâche la plus/moins réalisée
            val taskCompletionCounts = allHistory.groupBy { it.taskId }
                .mapValues { (_, histories) -> histories.size }
            
            val mostCompletedTaskId = taskCompletionCounts.maxByOrNull { it.value }?.key
            val leastCompletedTaskId = taskCompletionCounts.minByOrNull { it.value }?.key
            
            val mostCompletedTask = mostCompletedTaskId?.let { id ->
                taskList.find { it.id == id }
            }
            val leastCompletedTask = leastCompletedTaskId?.let { id ->
                taskList.find { it.id == id }
            }
            
            // Taux de réalisation (approximatif)
            val averageCompletionRate = if (totalTasks > 0) {
                (totalCompletions.toFloat() / totalTasks.toFloat() * 100).toInt()
            } else 0
            
            emit(TaskStats(
                totalTasks = totalTasks,
                overdueTasks = overdueTasks,
                todayTasks = todayTasks,
                upcomingTasks = upcomingTasks,
                totalCompletions = totalCompletions,
                completionsThisMonth = completionsThisMonth,
                averageCompletionRate = averageCompletionRate,
                mostCompletedTask = mostCompletedTask,
                leastCompletedTask = leastCompletedTask
            ))
        }
    }
    
    /**
     * Calcule les statistiques pour une tâche spécifique
     */
    suspend fun getTaskStats(taskId: String): TaskSpecificStats? {
        val task = taskRepository.getTaskById(taskId) ?: return null
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        
        // Récupérer l'historique de manière synchrone
        var historyList: List<LocalDate> = emptyList()
        taskRepository.getTaskHistory(taskId).collect { history ->
            historyList = history
        }
        
        val totalCompletions = historyList.size
        val lastCompletion = historyList.maxOrNull()
        val averageDaysBetweenCompletions = if (historyList.size >= 2) {
            val sortedDates = historyList.sorted()
            val intervals = sortedDates.zipWithNext { a, b ->
                b.toEpochDays() - a.toEpochDays()
            }
            intervals.average().toInt()
        } else null
        
        val daysUntilDue = task.nextDueDate.toEpochDays() - today.toEpochDays()
        val isOnTrack = daysUntilDue >= 0
        
        return TaskSpecificStats(
            taskId = taskId,
            taskName = task.name,
            totalCompletions = totalCompletions,
            lastCompletion = lastCompletion,
            averageDaysBetweenCompletions = averageDaysBetweenCompletions,
            daysUntilDue = daysUntilDue.toInt(),
            isOnTrack = isOnTrack,
            recurrenceDays = task.recurrenceDays
        )
    }
}

/**
 * Statistiques globales des tâches
 */
data class TaskStats(
    val totalTasks: Int,
    val overdueTasks: Int,
    val todayTasks: Int,
    val upcomingTasks: Int,
    val totalCompletions: Int,
    val completionsThisMonth: Int,
    val averageCompletionRate: Int,
    val mostCompletedTask: com.neverforget.data.model.Task?,
    val leastCompletedTask: com.neverforget.data.model.Task?
)

/**
 * Statistiques spécifiques à une tâche
 */
data class TaskSpecificStats(
    val taskId: String,
    val taskName: String,
    val totalCompletions: Int,
    val lastCompletion: LocalDate?,
    val averageDaysBetweenCompletions: Int?,
    val daysUntilDue: Int,
    val isOnTrack: Boolean,
    val recurrenceDays: Int
)