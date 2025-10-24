package com.neverforget.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neverforget.data.model.Task
import com.neverforget.data.repository.CategoryRepository
import com.neverforget.data.repository.TaskRepository
import com.neverforget.domain.usecase.ValidateTaskFormUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel pour le formulaire de création/modification de tâche avec validation métier
 */
@HiltViewModel
class TaskFormViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val categoryRepository: CategoryRepository,
    private val validateTaskFormUseCase: ValidateTaskFormUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TaskFormUiState())
    val uiState: StateFlow<TaskFormUiState> = _uiState.asStateFlow()
    
    private var editingTaskId: String? = null
    
    init {
        loadCategories()
        initializeDefaultValues()
    }
    
    /**
     * Charge les catégories disponibles
     */
    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getAllCategories()
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = "Erreur lors du chargement des catégories: ${exception.message}"
                    )
                }
                .collect { categories ->
                    _uiState.value = _uiState.value.copy(
                        availableCategories = categories.map { it.name }
                    )
                }
        }
    }
    
    /**
     * Initialise les valeurs par défaut
     */
    private fun initializeDefaultValues() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val defaultRecurrence = 30
        _uiState.value = _uiState.value.copy(
            category = "Maison", // Catégorie par défaut
            recurrenceDays = defaultRecurrence,
            nextDueDate = today.plus(defaultRecurrence, DateTimeUnit.DAY) // Calculé automatiquement
        )
    }
    
    /**
     * Charge une tâche existante pour modification
     */
    fun loadTask(taskId: String) {
        editingTaskId = taskId
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val task = taskRepository.getTaskById(taskId)
                if (task != null) {
                    _uiState.value = _uiState.value.copy(
                        name = task.name,
                        category = task.category,
                        recurrenceDays = task.recurrenceDays,
                        nextDueDate = task.nextDueDate,
                        isLoading = false
                    )
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
     * Met à jour le nom de la tâche avec validation en temps réel
     */
    fun updateName(name: String) {
        val nameError = validateTaskFormUseCase.validateName(name)
        _uiState.value = _uiState.value.copy(
            name = name,
            nameError = nameError
        )
        updateCanSave()
    }
    
    /**
     * Met à jour la catégorie et suggère une récurrence
     */
    fun updateCategory(category: String) {
        val suggestedRecurrence = validateTaskFormUseCase.suggestRecurrence(category)
        _uiState.value = _uiState.value.copy(
            category = category,
            recurrenceDays = suggestedRecurrence
        )
        updateCanSave()
    }
    
    /**
     * Met à jour la récurrence avec validation et calcul automatique de l'échéance
     */
    fun updateRecurrence(recurrenceDays: Int) {
        val recurrenceError = validateTaskFormUseCase.validateRecurrence(recurrenceDays)
        
        // Calcul automatique de la prochaine échéance : aujourd'hui + récurrence
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val nextDueDate = today.plus(recurrenceDays, DateTimeUnit.DAY)
        
        _uiState.value = _uiState.value.copy(
            recurrenceDays = recurrenceDays,
            recurrenceError = recurrenceError,
            nextDueDate = nextDueDate
        )
        updateCanSave()
    }
    
    /**
     * Met à jour la prochaine échéance
     */
    fun updateNextDueDate(nextDueDate: LocalDate) {
        _uiState.value = _uiState.value.copy(nextDueDate = nextDueDate)
        updateCanSave()
    }
    
    /**
     * Met à jour l'état canSave selon la validation
     */
    private fun updateCanSave() {
        val currentState = _uiState.value
        val validationResult = validateTaskFormUseCase.execute(
            name = currentState.name,
            category = currentState.category,
            recurrenceDays = currentState.recurrenceDays,
            nextDueDate = currentState.nextDueDate
        )
        
        _uiState.value = currentState.copy(canSave = validationResult.isValid)
    }
    
    /**
     * Sauvegarde la tâche
     */
    fun saveTask() {
        viewModelScope.launch {
            val currentState = _uiState.value
            
            // Validation finale
            val validationResult = validateTaskFormUseCase.execute(
                name = currentState.name,
                category = currentState.category,
                recurrenceDays = currentState.recurrenceDays,
                nextDueDate = currentState.nextDueDate
            )
            
            if (!validationResult.isValid) {
                _uiState.value = currentState.copy(
                    nameError = validationResult.getError("name"),
                    recurrenceError = validationResult.getError("recurrence"),
                    error = "Veuillez corriger les erreurs avant de sauvegarder"
                )
                return@launch
            }
            
            _uiState.value = currentState.copy(isSaving = true, error = null)
            
            try {
                val task = if (editingTaskId != null) {
                    // Modification
                    val existingTask = taskRepository.getTaskById(editingTaskId!!)
                    existingTask?.copy(
                        name = currentState.name,
                        category = currentState.category,
                        recurrenceDays = currentState.recurrenceDays
                        // Note: nextDueDate n'est pas modifiable en édition
                    )
                } else {
                    // Création
                    Task(
                        id = UUID.randomUUID().toString(),
                        name = currentState.name,
                        category = currentState.category,
                        recurrenceDays = currentState.recurrenceDays,
                        nextDueDate = currentState.nextDueDate,
                        createdAt = Clock.System.todayIn(TimeZone.currentSystemDefault())
                    )
                }
                
                if (task != null) {
                    if (editingTaskId != null) {
                        taskRepository.updateTask(task)
                    } else {
                        taskRepository.insertTask(task)
                    }
                    
                    _uiState.value = currentState.copy(
                        isSaving = false,
                        isTaskSaved = true
                    )
                } else {
                    _uiState.value = currentState.copy(
                        isSaving = false,
                        error = "Erreur lors de la préparation de la tâche"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isSaving = false,
                    error = "Erreur lors de la sauvegarde: ${e.message}"
                )
            }
        }
    }
}

/**
 * État de l'interface utilisateur pour le formulaire de tâche
 */
data class TaskFormUiState(
    val name: String = "",
    val category: String = "",
    val recurrenceDays: Int = 0,
    val nextDueDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val availableCategories: List<String> = emptyList(),
    val nameError: String? = null,
    val recurrenceError: String? = null,
    val error: String? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val canSave: Boolean = false,
    val isTaskSaved: Boolean = false
)