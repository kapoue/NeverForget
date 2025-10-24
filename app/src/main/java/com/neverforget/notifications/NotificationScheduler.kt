package com.neverforget.notifications

import androidx.work.*
import com.neverforget.data.repository.SettingsRepository
import com.neverforget.utils.Logger
import com.neverforget.workers.TaskNotificationWorker
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestionnaire pour planifier et annuler les notifications de tâches
 */
@Singleton
class NotificationScheduler @Inject constructor(
    private val workManager: WorkManager,
    private val settingsRepository: SettingsRepository
) {
    
    companion object {
        private const val NOTIFICATION_HOUR = 9 // 9h du matin
        private const val DEFAULT_REMINDER_DELAY_DAYS = 3 // Rappel après 3 jours par défaut
    }
    
    /**
     * Planifie une notification pour une tâche
     */
    fun scheduleTaskNotification(taskId: String, taskName: String, dueDate: LocalDate, customReminderDelay: Int? = null) {
        
        
        // Annuler les notifications existantes pour cette tâche
        cancelTaskNotifications(taskId)
        
        // Planifier la notification principale (le jour J)
        
        scheduleNotification(
            taskId = taskId,
            taskName = taskName,
            targetDate = dueDate,
            notificationType = TaskNotificationWorker.NOTIFICATION_TYPE_DUE,
            workName = "notification_due_$taskId"
        )
        
        // Récupérer le délai de rappel depuis les paramètres ou utiliser celui fourni
        val reminderDelayDays = customReminderDelay ?: runBlocking {
            settingsRepository.getReminderDelayDays()
        }
        
        // Planifier le rappel (X jours après si pas fait)
        val reminderDate = dueDate.plus(reminderDelayDays, DateTimeUnit.DAY)
        
        scheduleNotification(
            taskId = taskId,
            taskName = taskName,
            targetDate = reminderDate,
            notificationType = TaskNotificationWorker.NOTIFICATION_TYPE_REMINDER,
            workName = "notification_reminder_$taskId"
        )
        
        
    }
    
    /**
     * Planifie une notification à une date spécifique
     */
    private fun scheduleNotification(
        taskId: String,
        taskName: String,
        targetDate: LocalDate,
        notificationType: String,
        workName: String
    ) {
        
        
        // Calculer le délai jusqu'à la notification
        val notificationTime = targetDate.atTime(NOTIFICATION_HOUR, 0)
        val notificationInstant = notificationTime.toInstant(TimeZone.currentSystemDefault())
        val currentInstant = kotlinx.datetime.Clock.System.now()
        
        val delayMillis = (notificationInstant - currentInstant).inWholeMilliseconds
        
        
        
        // Ne pas planifier si la date est déjà passée
        if (delayMillis <= 0) {
            
            return
        }
        
        // Créer les données d'entrée
        val inputData = workDataOf(
            TaskNotificationWorker.TASK_ID_KEY to taskId,
            TaskNotificationWorker.TASK_NAME_KEY to taskName,
            TaskNotificationWorker.NOTIFICATION_TYPE_KEY to notificationType
        )
        
        // Créer la requête de travail
        val workRequest = OneTimeWorkRequestBuilder<TaskNotificationWorker>()
            .setInputData(inputData)
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .addTag("task_notification")
            .addTag("task_$taskId")
            .build()
        
        // Planifier le travail
        workManager.enqueueUniqueWork(
            workName,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
        
        
    }
    
    /**
     * Annule toutes les notifications pour une tâche
     */
    fun cancelTaskNotifications(taskId: String) {
        
        workManager.cancelAllWorkByTag("task_$taskId")
    }
    
    /**
     * Annule toutes les notifications
     */
    fun cancelAllNotifications() {
        workManager.cancelAllWorkByTag("task_notification")
    }
    
    /**
     * Replanifie toutes les notifications (utile après redémarrage)
     */
    suspend fun rescheduleAllNotifications(tasks: List<Triple<String, String, LocalDate>>) {
        // Annuler toutes les notifications existantes
        cancelAllNotifications()
        
        // Replanifier pour chaque tâche
        tasks.forEach { (taskId, taskName, dueDate) ->
            scheduleTaskNotification(taskId, taskName, dueDate)
        }
    }
}