package com.neverforget.data.repository

import android.content.Context
import android.net.Uri
import com.neverforget.data.database.TaskDao
import com.neverforget.data.database.TaskHistoryDao
import com.neverforget.data.database.TaskEntity
import com.neverforget.data.database.TaskHistoryEntity
import com.neverforget.data.model.*
import com.neverforget.utils.DateUtils

import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository pour la gestion des exports/imports JSON
 */
@Singleton
class BackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val taskDao: TaskDao,
    private val taskHistoryDao: TaskHistoryDao,
    private val taskRepository: TaskRepository
) {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    /**
     * Exporte toutes les tâches vers un fichier JSON et retourne les données pour partage
     */
    suspend fun exportToJson(): Result<ExportResult> = withContext(Dispatchers.IO) {
        try {
            // Récupérer toutes les tâches avec leur historique
            val tasksWithHistory = taskDao.getAllTasksWithHistorySync()
            
            // Convertir vers le format d'export
            val exportTasks = tasksWithHistory.map { taskWithHistory ->
                val task = taskWithHistory.task
                val history = taskWithHistory.history.map { historyItem ->
                    DateUtils.formatDate(historyItem.completedDate)
                }.sortedDescending() // Plus récent en premier
                
                ExportTask(
                    id = task.id,
                    name = task.name,
                    category = task.category,
                    recurrenceDays = task.recurrenceDays,
                    nextDueDate = DateUtils.formatDate(task.nextDueDate),
                    history = history
                )
            }
            
            // Créer les données d'export
            val exportData = ExportData(
                version = "1.0",
                exportDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString(),
                tasks = exportTasks
            )
            
            // Sérialiser en JSON
            val jsonString = json.encodeToString(exportData)
            
            // Créer le nom de fichier avec la date et l'heure
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val dateStamp = DateUtils.formatDate(now.date).replace("-", "")
            val timeStamp = String.format("%02d%02d", now.hour, now.minute)
            val fileName = "neverforget_backup_${dateStamp}_$timeStamp.json"
            
            Result.success(ExportResult(fileName, jsonString))
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Importe les tâches depuis un fichier JSON (écrase tout)
     */
    suspend fun importFromJson(uri: Uri): Result<ImportResult> = withContext(Dispatchers.IO) {
        try {
            // Lire le fichier JSON
            val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Impossible d'ouvrir le fichier"))
            
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            
            // Désérialiser le JSON
            val exportData = json.decodeFromString<ExportData>(jsonString)
            
            // Supprimer toutes les tâches existantes
            taskDao.deleteAllTasks()
            
            // Importer toutes les nouvelles tâches
            val importedCount = importTasksDirectly(exportData.tasks)
            
            Result.success(ImportResult(
                success = true,
                importedTasks = importedCount
            ))
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Résout les conflits et importe les tâches
     */
    suspend fun resolveConflictsAndImport(
        exportData: ExportData,
        resolutions: List<ConflictResolutionChoice>
    ): Result<ImportResult> = withContext(Dispatchers.IO) {
        try {
            val resolutionMap = resolutions.associateBy { it.taskName }
            var importedCount = 0
            
            exportData.tasks.forEach { importedTask ->
                val resolution = resolutionMap[importedTask.name]
                
                when (resolution?.resolution) {
                    ConflictResolution.OVERWRITE -> {
                        // Supprimer la tâche existante et importer la nouvelle
                        taskRepository.deleteTaskByName(importedTask.name)
                        importTask(importedTask)
                        importedCount++
                    }
                    ConflictResolution.MERGE -> {
                        // Fusionner les historiques
                        mergeTaskHistory(importedTask)
                        importedCount++
                    }
                    ConflictResolution.SKIP -> {
                        // Ignorer cette tâche
                    }
                    null -> {
                        // Pas de conflit, importer directement
                        importTask(importedTask)
                        importedCount++
                    }
                }
            }
            
            Result.success(ImportResult(
                success = true,
                importedTasks = importedCount
            ))
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Importe les tâches directement (sans conflits)
     */
    private suspend fun importTasksDirectly(tasks: List<ExportTask>): Int {
        var count = 0
        tasks.forEach { task ->
            importTask(task)
            count++
        }
        return count
    }

    /**
     * Importe une tâche individuelle
     */
    private suspend fun importTask(exportTask: ExportTask) {
        // Créer la tâche
        val taskEntity = TaskEntity(
            id = exportTask.id,
            name = exportTask.name,
            category = exportTask.category,
            recurrenceDays = exportTask.recurrenceDays,
            nextDueDate = DateUtils.parseDate(exportTask.nextDueDate),
            createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
            reminderDelayDays = 3 // Valeur par défaut
        )
        
        taskDao.insertTask(taskEntity)
        
        // Importer l'historique
        exportTask.history.forEach { dateString ->
            val historyEntity = TaskHistoryEntity(
                taskId = exportTask.id,
                completedDate = DateUtils.parseDate(dateString)
            )
            taskHistoryDao.insertHistory(historyEntity)
        }
    }

    /**
     * Fusionne l'historique d'une tâche
     */
    private suspend fun mergeTaskHistory(importedTask: ExportTask) {
        val existingTask = taskDao.getTaskByName(importedTask.name) ?: return
        val existingHistory = taskHistoryDao.getHistoryForTaskSync(existingTask.id)
            .map { DateUtils.formatDate(it.completedDate) }
            .toSet()
        
        // Ajouter seulement les nouvelles dates d'historique
        importedTask.history.forEach { dateString ->
            if (dateString !in existingHistory) {
                val historyEntity = TaskHistoryEntity(
                    taskId = existingTask.id,
                    completedDate = DateUtils.parseDate(dateString)
                )
                taskHistoryDao.insertHistory(historyEntity)
            }
        }
    }
}