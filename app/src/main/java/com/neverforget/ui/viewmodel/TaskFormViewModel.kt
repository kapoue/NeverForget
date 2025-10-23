package com.neverforget.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neverforget.data.model.Category
import com.neverforget.data.model.Task
import com.neverforget.data.repository.CategoryRepository
import com.neverforget.data.repository.TaskRepository
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
 * ViewModel pour l'écran de création/modification de tâche
 */
@HiltViewModel
class TaskFormViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TaskFormUiState())
    val uiState: StateFlow<TaskFormUiState> = _uiState.asStateFlow()
    
    init {
        loadCategories()
    }
    
    /**
     * Charge les catégories disponibles
     */
    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { categories ->
                _uiState.value = _uiState.value.copy(
                    availableCategories = categories
                )
                
                // Sélectionner "Maison" par défaut si aucune catégorie n'est sélectionnée
                if (_uiState.value.selectedCategory.isEmpty() && categories.isNotEmpty()) {
                    val defaultCategory = categories.find { it.name == "Maison" } ?: categories.first()
                    updateSelectedCategory(defaultCategory.name)
                }
            }
        }
    }
    
    /**
     * Charge une tâche existante pour modification
     */
    fun loadTask(taskId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val task = taskRepository.getTaskById(taskId)
                if (task != null) {
                    _uiState.value = _uiState.value.copy(
                        taskId = task.id,
                        taskName = task.name,
                        selectedCategory = task.category,
                        recurrenceValue = task.recurrenceDays,
                        recurrenceUnit = getRecurrenceUnit(task.recurrenceDays),
                        isEditMode = true,
                        isLoading = false
                    )
                    updateCalculatedDueDate()
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
     * Met à jour le nom de la tâche
     */
    fun updateTaskName(name: String) {
        _uiState.value = _uiState.value.copy(
            taskName = name,
            nameError = if (name.isBlank()) "Le nom est obligatoire" else null
        )
    }
    
    /**
     * Met à jour la catégorie sélectionnée
     */
    fun updateSelectedCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }
    
    /**
     * Met à jour la valeur de récurrence
     */
    fun updateRecurrenceValue(value: Int) {
        if (value > 0) {
            _uiState.value = _uiState.value.copy(recurrenceValue = value)
            updateCalculatedDueDate()
        }
    }
    
    /**
     * Met à jour l'unité de récurrence
     */
    fun updateRecurrenceUnit(unit: RecurrenceUnit) {
        _uiState.value = _uiState.value.copy(recurrenceUnit = unit)
        updateCalculatedDueDate()
    }
    
    /**
     * Calcule et met à jour la date d'échéance
     */
    private fun updateCalculatedDueDate() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val recurrenceDays = calculateRecurrenceDays()
        val dueDate = today.plusDays(recurrenceDays.toLong())
        
        _uiState.value = _uiState.value.copy(calculatedDueDate = dueDate)
    }
    
    /**
     * Calcule le nombre de jours de récurrence
     */
    private fun calculateRecurrenceDays(): Int {
        val state = _uiState.value
        return when (state.recurrenceUnit) {
            RecurrenceUnit.DAYS -> state.recurrenceValue
            RecurrenceUnit.WEEKS -> state.recurrenceValue * 7
            RecurrenceUnit.MONTHS -> state.recurrenceValue * 30
            RecurrenceUnit.YEARS -> state.recurrenceValue * 365
        }
    }
    
    /**
     * Détermine l'unité de récurrence à partir du nombre de jours
     */
    private fun getRecurrenceUnit(days: Int): RecurrenceUnit {
        return when {
            days % 365 == 0 -> RecurrenceUnit.YEARS
            days % 30 == 0 -> RecurrenceUnit.MONTHS
            days % 7 == 0 -> RecurrenceUnit.WEEKS
            else -> RecurrenceUnit.DAYS
        }
    }
    
    /**
     * Sauvegarde la tâche
     */
    fun saveTask(onSaved: () -> Unit) {
        val state = _uiState.value
        
        // Validation
        if (state.taskName.isBlank()) {
            _uiState.value = state.copy(nameError = "Le nom est obligatoire")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true)
            
            try {
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                val recurrenceDays = calculateRecurrenceDays()
                
                val task = if (state.isEditMode) {
                    // Modification
                    Task(
                        id = state.taskId,
                        name = state.taskName,
                        category = state.selectedCategory,
                        recurrenceDays = recurrenceDays,
                        nextDueDate = state.calculatedDueDate,
                        createdAt = today // Sera ignoré lors de la mise à jour
                    )
                } else {
                    // Création
                    Task(
                        name = state.taskName,
                        category = state.selectedCategory,
                        recurrenceDays = recurrenceDays,
                        nextDueDate = state.calculatedDueDate,
                        createdAt = today
                    )
                }
                
                if (state.isEditMode) {
                    taskRepository.updateTask(task)
                } else {
                    taskRepository.insertTask(task)
                }
                
                onSaved()
            } catch (exception: Exception) {
                _uiState.value = state.copy(
                    isLoading = false,
                    errorMessage = "Erreur lors de la sauvegarde : ${exception.message}"
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
 * Unités de récurrence disponibles
 */
enum class RecurrenceUnit(val displayName: String) {
    DAYS("jours"),
    WEEKS("semaines"),
    MONTHS("mois"),
    YEARS("années")
}

/**
 * État de l'interface utilisateur pour le formulaire de tâche
 */
data class TaskFormUiState(
    val taskId: String = "",
    val taskName: String = "",
    val selectedCategory: String = "",
    val recurrenceValue: Int = 1,
    val recurrenceUnit: RecurrenceUnit = RecurrenceUnit.MONTHS,
    val calculatedDueDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val availableCategories: List<Category> = emptyList(),
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val nameError: String? = null,
    val errorMessage: String? = null
)