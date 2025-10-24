package com.neverforget.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neverforget.data.model.Task
import com.neverforget.data.model.TaskStatus
import com.neverforget.data.repository.TaskRepository
import com.neverforget.domain.usecase.CompleteTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject

/**
 * ViewModel pour l'écran de détail d'une tâche avec logique métier
 */
@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val completeTaskUseCase: CompleteTaskUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TaskDetailUiState())
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()
    
    private var currentTaskId: String? = null
    
    /**
     * Charge une tâche et son historique
     */
    fun loadTask(taskId: String) {
        currentTaskId = taskId
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val task = taskRepository.getTaskById(taskId)
                
                if (task != null) {
                    // Calculer le statut actuel de la tâche
                    val taskWithStatus = calculateTaskStatus(task)
                    
                    // Collecter l'historique
                    taskRepository.getTaskHistory(taskId).collect { historyList ->
                        _uiState.value = _uiState.value.copy(
                            task = taskWithStatus,
                            history = historyList.sortedDescending(),
                            isLoading = false,
                            error = null
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Tâche non trouvée"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erreur lors du chargement: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Valide la tâche avec calcul automatique de la prochaine échéance
     */
    fun completeTask() {
        val taskId = currentTaskId ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCompletingTask = true)
            
            completeTaskUseCase.execute(taskId)
                .onSuccess {
                    // Recharger la tâche pour voir les changements
                    loadTask(taskId)
                    _uiState.value = _uiState.value.copy(
                        isCompletingTask = false,
                        completionMessage = "Tâche validée ! Prochaine échéance calculée automatiquement."
                    )
                    
                    // Effacer le message après 3 secondes
                    kotlinx.coroutines.delay(3000)
                    _uiState.value = _uiState.value.copy(completionMessage = null)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isCompletingTask = false,
                        error = "Erreur lors de la validation: ${exception.message}"
                    )
                }
        }
    }
    
    /**
     * Supprime la tâche
     */
    fun deleteTask() {
        val taskId = currentTaskId ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeletingTask = true)
            
            try {
                taskRepository.deleteTaskById(taskId)
                _uiState.value = _uiState.value.copy(
                    isDeletingTask = false,
                    isTaskDeleted = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDeletingTask = false,
                    error = "Erreur lors de la suppression: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Calcule le statut actuel d'une tâche
     */
    private fun calculateTaskStatus(task: Task): Task {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val daysUntilDue = task.nextDueDate.toEpochDays() - today.toEpochDays()
        
        val status = when {
            task.nextDueDate < today -> TaskStatus.OVERDUE
            task.nextDueDate == today -> TaskStatus.DUE_TODAY
            else -> TaskStatus.OK
        }
        
        return task.copy(
            status = status,
            daysUntilDue = kotlin.math.abs(daysUntilDue)
        )
    }
    
    /**
     * Efface le message d'erreur
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Efface le message de confirmation
     */
    fun clearCompletionMessage() {
        _uiState.value = _uiState.value.copy(completionMessage = null)
    }
}

/**
 * État de l'interface utilisateur pour le détail d'une tâche
 */
data class TaskDetailUiState(
    val task: Task? = null,
    val history: List<LocalDate> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCompletingTask: Boolean = false,
    val isDeletingTask: Boolean = false,
    val completionMessage: String? = null,
    val isTaskDeleted: Boolean = false
)