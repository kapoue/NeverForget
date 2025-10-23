package com.neverforget.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neverforget.data.model.Task
import com.neverforget.data.model.TaskStatus
import com.neverforget.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import javax.inject.Inject

/**
 * ViewModel pour l'écran de liste des tâches
 */
@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TaskListUiState())
    val uiState: StateFlow<TaskListUiState> = _uiState.asStateFlow()
    
    init {
        loadTasks()
    }
    
    /**
     * Charge toutes les tâches et les trie par urgence
     */
    private fun loadTasks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            taskRepository.getAllTasks()
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message
                    )
                }
                .collect { tasks ->
                    val sortedTasks = sortTasksByUrgency(tasks)
                    _uiState.value = _uiState.value.copy(
                        tasks = sortedTasks,
                        isLoading = false,
                        errorMessage = null
                    )
                }
        }
    }
    
    /**
     * Trie les tâches par urgence : en retard > aujourd'hui > futur
     */
    private fun sortTasksByUrgency(tasks: List<Task>): List<Task> {
        return tasks.sortedWith(compareBy<Task> { task ->
            when (task.status) {
                TaskStatus.OVERDUE -> 0
                TaskStatus.DUE_TODAY -> 1
                TaskStatus.OK -> 2
            }
        }.thenBy { it.nextDueDate })
    }
    
    /**
     * Valide une tâche (marque comme terminée)
     */
    fun completeTask(taskId: String) {
        viewModelScope.launch {
            try {
                taskRepository.completeTask(taskId)
                // Les tâches se rechargeront automatiquement via le Flow
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Erreur lors de la validation : ${exception.message}"
                )
            }
        }
    }
    
    /**
     * Supprime une tâche
     */
    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            try {
                taskRepository.deleteTaskById(taskId)
                // Les tâches se rechargeront automatiquement via le Flow
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
    
    /**
     * Rafraîchit la liste des tâches
     */
    fun refreshTasks() {
        loadTasks()
    }
}

/**
 * État de l'interface utilisateur pour la liste des tâches
 */
data class TaskListUiState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)