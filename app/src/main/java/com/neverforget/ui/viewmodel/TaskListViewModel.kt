package com.neverforget.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neverforget.data.model.Task
import com.neverforget.domain.usecase.CompleteTaskUseCase
import com.neverforget.domain.usecase.GetTasksWithStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel pour l'écran de liste des tâches avec logique métier intégrée
 */
@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val getTasksWithStatusUseCase: GetTasksWithStatusUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TaskListUiState())
    val uiState: StateFlow<TaskListUiState> = _uiState.asStateFlow()
    
    init {
        loadTasks()
    }
    
    /**
     * Charge toutes les tâches avec statut calculé et tri par urgence
     */
    private fun loadTasks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            getTasksWithStatusUseCase.execute()
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Erreur lors du chargement des tâches: ${exception.message}"
                    )
                }
                .collect { tasks ->
                    _uiState.value = _uiState.value.copy(
                        tasks = tasks,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }
    
    /**
     * Valide une tâche avec calcul automatique de la prochaine échéance
     */
    fun completeTask(taskId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCompletingTask = true)
            
            completeTaskUseCase.execute(taskId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isCompletingTask = false,
                        completionMessage = "Tâche validée avec succès !"
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
    val error: String? = null,
    val isCompletingTask: Boolean = false,
    val completionMessage: String? = null
)