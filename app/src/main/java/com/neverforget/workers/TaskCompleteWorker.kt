package com.neverforget.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.neverforget.data.repository.TaskRepository
import com.neverforget.notifications.NotificationScheduler
import com.neverforget.utils.Logger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * Worker pour valider une tâche depuis une notification
 */
@HiltWorker
class TaskCompleteWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val taskRepository: TaskRepository,
    private val notificationScheduler: NotificationScheduler
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val taskId = inputData.getString("task_id") ?: return Result.failure()
            
            
            
            // Récupérer la tâche
            val task = taskRepository.getTaskById(taskId)
            if (task == null) {
                
                return Result.success() // Tâche supprimée, pas d'erreur
            }
            
            // Valider la tâche avec la date d'aujourd'hui
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            taskRepository.completeTask(taskId, today)
            
            // Annuler toutes les notifications pour cette tâche
            notificationScheduler.cancelTaskNotifications(taskId)
            
            
            Result.success()
            
        } catch (e: Exception) {
            
            Result.failure()
        }
    }
}