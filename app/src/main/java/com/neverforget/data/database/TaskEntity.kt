package com.neverforget.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import java.util.UUID

/**
 * Entité Room représentant une tâche dans la base de données
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey 
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String,
    val recurrenceDays: Int,
    val nextDueDate: LocalDate,
    val createdAt: LocalDate,
    val reminderDelayDays: Int = 3
)