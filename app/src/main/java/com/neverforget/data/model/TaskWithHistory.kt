package com.neverforget.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.neverforget.data.database.TaskEntity
import com.neverforget.data.database.TaskHistoryEntity

/**
 * Classe de relation Room pour récupérer une tâche avec son historique
 */
data class TaskWithHistory(
    @Embedded val task: TaskEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "taskId"
    )
    val history: List<TaskHistoryEntity>
)