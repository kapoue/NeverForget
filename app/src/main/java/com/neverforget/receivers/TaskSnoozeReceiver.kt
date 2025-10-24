package com.neverforget.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.neverforget.utils.Logger
import com.neverforget.workers.TaskSnoozeWorker

/**
 * BroadcastReceiver pour reporter (snooze) une notification de tâche
 */
class TaskSnoozeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getStringExtra("task_id") ?: return
        val taskName = intent.getStringExtra("task_name") ?: "Tâche inconnue"
        
        
        
        // Annuler la notification actuelle immédiatement
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = 1000 + taskId.hashCode()
        notificationManager.cancel(notificationId)
        
        
        
        // Lancer un Worker pour programmer le report
        val workRequest = OneTimeWorkRequestBuilder<TaskSnoozeWorker>()
            .setInputData(workDataOf(
                "task_id" to taskId,
                "task_name" to taskName
            ))
            .build()
        
        WorkManager.getInstance(context).enqueue(workRequest)
        
        
    }
}