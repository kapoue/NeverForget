package com.neverforget.data.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * Modèles de données pour l'export/import JSON
 * Conforme à la structure définie dans la documentation
 */

@Serializable
data class ExportData(
    val version: String = "1.0",
    val exportDate: String, // Format ISO 8601
    val tasks: List<ExportTask>
)

@Serializable
data class ExportTask(
    val id: String,
    val name: String,
    val category: String,
    val recurrenceDays: Int,
    val nextDueDate: String, // Format YYYY-MM-DD
    val history: List<String> // Format YYYY-MM-DD
)

/**
 * Résultat de l'import avec gestion des conflits
 */
data class ImportResult(
    val success: Boolean,
    val importedTasks: Int,
    val conflicts: List<TaskConflict> = emptyList(),
    val error: String? = null
)

/**
 * Conflit détecté lors de l'import
 */
data class TaskConflict(
    val taskName: String,
    val existingTask: ExportTask,
    val importedTask: ExportTask
)

/**
 * Options de résolution des conflits
 */
enum class ConflictResolution {
    OVERWRITE,  // Écraser la tâche locale
    MERGE,      // Fusionner les historiques
    SKIP        // Ignorer la tâche importée
}

/**
 * Résolution d'un conflit spécifique
 */
data class ConflictResolutionChoice(
    val taskName: String,
    val resolution: ConflictResolution
)

/**
 * Résultat de l'export pour partage
 */
data class ExportResult(
    val fileName: String,
    val jsonContent: String
)