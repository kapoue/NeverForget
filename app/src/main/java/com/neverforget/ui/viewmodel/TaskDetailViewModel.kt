package com.neverforget.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neverforget.data.model.Task
import com.neverforget.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import javax.inject.Inject

/**
 * ViewModel pour l'écran de détail d'une tâche
 */
@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TaskDetailUiState())
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()
    
    /**
     * Charge les détails d'une tâche
     */
    fun loadTask(taskId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val task = taskRepository.getTaskById(taskId)
                if (task != null) {
                    _uiState.value = _uiState.value.copy(
                        task = task,
                        isLoading = false,
                        errorMessage = null
                    )
                    
                    // Charger l'historique
                    loadTaskHistory(taskId)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Tâche introuvable"
                    )
                }
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message
                )
            }
        }
    }
    
    /**
     * Charge l'historique d'une tâche
     */
    private fun loadTaskHistory(taskId: String) {
        viewModelScope.launch {
            taskRepository.getTaskHistory(taskId).collect { history ->
                _uiState.value = _uiState.value.copy(history = history)
            }
        }
    }
    
    /**
     * Valide une tâche avec une date personnalisée
     */
    fun completeTask(taskId: String, completedDate: LocalDate = kotlinx.datetime.Clock.System.todayIn(kotlinx.datetime.TimeZone.currentSystemDefault())) {
        viewModelScope.launch {
            try {
                taskRepository.completeTask(taskId, completedDate)
                // Recharger la tâche pour voir les changements
                loadTask(taskId)
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Erreur lors de la validation : ${exception.message}"
                )
            }
        }
    }
    
    /**
     * Supprime la tâche
     */
    fun deleteTask(taskId: String, onDeleted: () -> Unit) {
        viewModelScope.launch {
            try {
                taskRepository.deleteTaskById(taskId)
                onDeleted()
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Erreur lors de la suppression : ${exception.message}"
                )
            }
        }
    }
    
    /**
     * Efface le message d'erreur
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

/**
 * État de l'interface utilisateur pour le détail d'une tâche
 */
data class TaskDetailUiState(
    val task: Task? = null,
    val history: List<LocalDate> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)