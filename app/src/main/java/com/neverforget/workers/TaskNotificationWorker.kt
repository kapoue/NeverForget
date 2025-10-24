package com.neverforget.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.neverforget.MainActivity
import com.neverforget.R
import com.neverforget.data.repository.TaskRepository
import com.neverforget.receivers.TaskCompleteReceiver
import com.neverforget.receivers.TaskSnoozeReceiver
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * Worker pour envoyer les notifications de rappel de tâches
 */
@HiltWorker
class TaskNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val taskRepository: TaskRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val TASK_ID_KEY = "task_id"
        const val TASK_NAME_KEY = "task_name"
        const val NOTIFICATION_TYPE_KEY = "notification_type"
        
        const val NOTIFICATION_TYPE_DUE = "due"
        const val NOTIFICATION_TYPE_REMINDER = "reminder"
        
        const val CHANNEL_ID = "task_reminders"
        const val CHANNEL_NAME = "Rappels de tâches"
        
        private const val NOTIFICATION_ID_BASE = 1000
    }

    override suspend fun doWork(): Result {
        return try {
            val taskId = inputData.getString(TASK_ID_KEY) ?: return Result.failure()
            val taskName = inputData.getString(TASK_NAME_KEY) ?: return Result.failure()
            val notificationType = inputData.getString(NOTIFICATION_TYPE_KEY) ?: NOTIFICATION_TYPE_DUE
            
            // Vérifier que la tâche existe encore et est toujours due
            val task = taskRepository.getTaskById(taskId)
            if (task == null) {
                return Result.success() // Tâche supprimée, pas d'erreur
            }
            
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val isDue = task.nextDueDate <= today
            
            if (!isDue && notificationType == NOTIFICATION_TYPE_DUE) {
                return Result.success() // Tâche plus due, pas besoin de notifier
            }
            
            // Créer le canal de notification
            createNotificationChannel()
            
            // Envoyer la notification
            sendNotification(taskId, taskName, notificationType, task.nextDueDate < today)
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = applicationContext.getString(R.string.notification_channel_description)
                enableVibration(true)
                setShowBadge(true)
            }
            
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(taskId: String, taskName: String, type: String, isOverdue: Boolean) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Intent pour ouvrir l'application
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("task_id", taskId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Intent pour marquer comme fait
        val completeIntent = Intent(applicationContext, TaskCompleteReceiver::class.java).apply {
            putExtra("task_id", taskId)
        }
        
        val completePendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            taskId.hashCode() + 1,
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Intent pour reporter (snooze)
        val snoozeIntent = Intent(applicationContext, TaskSnoozeReceiver::class.java).apply {
            putExtra("task_id", taskId)
            putExtra("task_name", taskName)
        }
        
        val snoozePendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            taskId.hashCode() + 2,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Construire la notification
        val title = applicationContext.getString(R.string.notification_title)
        val message = when (type) {
            NOTIFICATION_TYPE_REMINDER -> {
                if (isOverdue) {
                    applicationContext.getString(R.string.notification_reminder_overdue, taskName)
                } else {
                    applicationContext.getString(R.string.notification_reminder, taskName)
                }
            }
            else -> applicationContext.getString(R.string.notification_message, taskName)
        }
        
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_check,
                applicationContext.getString(R.string.notification_action_complete),
                completePendingIntent
            )
            .addAction(
                R.drawable.ic_snooze,
                applicationContext.getString(R.string.notification_action_snooze),
                snoozePendingIntent
            )
            .build()
        
        val notificationId = NOTIFICATION_ID_BASE + taskId.hashCode()
        notificationManager.notify(notificationId, notification)
    }
}