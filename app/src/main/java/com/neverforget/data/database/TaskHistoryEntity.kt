package com.neverforget.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

/**
 * Entité Room représentant l'historique des validations de tâches
 */
@Entity(
    tableName = "task_history",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TaskHistoryEntity(
    @PrimaryKey(autoGenerate = true) 
    val id: Int = 0,
    val taskId: String,
    val completedDate: LocalDate
)