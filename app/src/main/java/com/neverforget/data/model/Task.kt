package com.neverforget.data.model

import kotlinx.datetime.LocalDate
import java.util.UUID

/**
 * Modèle de domaine représentant une tâche
 */
data class Task(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String,
    val recurrenceDays: Int,
    val nextDueDate: LocalDate,
    val createdAt: LocalDate,
    val reminderDelayDays: Int = 3,
    val status: TaskStatus = TaskStatus.OK,
    val daysUntilDue: Int = 0,
    val history: List<LocalDate> = emptyList()
)