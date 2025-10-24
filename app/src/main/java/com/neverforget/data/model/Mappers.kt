package com.neverforget.data.model

import com.neverforget.data.database.TaskEntity
import com.neverforget.data.database.TaskHistoryEntity
import com.neverforget.data.database.CategoryEntity
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.math.abs

/**
 * Extensions pour convertir entre les entités Room et les modèles de domaine
 */

/**
 * Convertit une TaskEntity en Task avec calcul du statut
 */
fun TaskEntity.toTask(history: List<TaskHistoryEntity> = emptyList()): Task {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val daysUntilDue = nextDueDate.toEpochDays() - today.toEpochDays()
    
    val status = when {
        daysUntilDue < 0 -> TaskStatus.OVERDUE
        daysUntilDue == 0 -> TaskStatus.DUE_TODAY
        else -> TaskStatus.OK
    }
    
    // Pour les tâches en retard, on veut le nombre de jours de retard (positif)
    // Pour les tâches OK, on veut le nombre de jours restants (positif)
    val displayDays = when (status) {
        TaskStatus.OVERDUE -> abs(daysUntilDue) // Jours de retard
        TaskStatus.DUE_TODAY -> 0
        TaskStatus.OK -> daysUntilDue // Jours restants
    }
    
    return Task(
        id = id,
        name = name,
        category = category,
        recurrenceDays = recurrenceDays,
        nextDueDate = nextDueDate,
        createdAt = createdAt,
        reminderDelayDays = reminderDelayDays,
        status = status,
        daysUntilDue = displayDays,
        history = history.map { it.completedDate }.sortedDescending()
    )
}

/**
 * Convertit un Task en TaskEntity
 */
fun Task.toEntity(): TaskEntity {
    return TaskEntity(
        id = id,
        name = name,
        category = category,
        recurrenceDays = recurrenceDays,
        nextDueDate = nextDueDate,
        createdAt = createdAt,
        reminderDelayDays = reminderDelayDays
    )
}

/**
 * Convertit une TaskWithHistory en Task
 */
fun TaskWithHistory.toTask(): Task {
    return task.toTask(history)
}

/**
 * Convertit une CategoryEntity en Category
 */
fun CategoryEntity.toCategory(): Category {
    return Category(
        name = name,
        icon = icon,
        isDeletable = isDeletable
    )
}

/**
 * Convertit une Category en CategoryEntity
 */
fun Category.toEntity(): CategoryEntity {
    return CategoryEntity(
        name = name,
        icon = icon,
        isDeletable = isDeletable
    )
}