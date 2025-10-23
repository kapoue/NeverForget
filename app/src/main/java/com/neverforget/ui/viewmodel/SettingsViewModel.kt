package com.neverforget.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neverforget.data.model.Category
import com.neverforget.data.repository.CategoryRepository
import com.neverforget.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel pour l'écran des paramètres
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
        loadCategories()
    }
    
    /**
     * Charge les paramètres
     */
    private fun loadSettings() {
        viewModelScope.launch {
            try {
                val notificationTime = settingsRepository.getNotificationTime()
                _uiState.value = _uiState.value.copy(
                    notificationTime = notificationTime
                )
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Erreur lors du chargement des paramètres : ${exception.message}"
                )
            }
        }
    }
    
    /**
     * Charge les catégories
     */
    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { categories ->
                _uiState.value = _uiState.value.copy(categories = categories)
            }
        }
    }
    
    /**
     * Met à jour l'heure de notification
     */
    fun updateNotificationTime(time: String) {
        viewModelScope.launch {
            try {
                settingsRepository.setNotificationTime(time)
                _uiState.value = _uiState.value.copy(notificationTime = time)
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Erreur lors de la mise à jour : ${exception.message}"
                )
            }
        }
    }
    
    /**
     * Ajoute une nouvelle catégorie
     */
    fun addCategory(name: String, icon: String) {
        viewModelScope.launch {
            try {
                // Vérifier que la catégorie n'existe pas déjà
                val existingCategory = categoryRepository.getCategoryByName(name)
                if (existingCategory != null) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Une catégorie avec ce nom existe déjà"
                    )
                    return@launch
                }
                
                categoryRepository.addCategory(name, icon)
                // Les catégories se rechargeront automatiquement via le Flow
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Erreur lors de l'ajout : ${exception.message}"
                )
            }
        }
    }
    
    /**
     * Supprime une catégorie
     */
    fun deleteCategory(categoryName: String) {
        viewModelScope.launch {
            try {
                categoryRepository.deleteCategory(categoryName)
                // Les catégories se rechargeront automatiquement via le Flow
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
 * État de l'interface utilisateur pour les paramètres
 */
data class SettingsUiState(
    val notificationTime: String = "11:00",
    val categories: List<Category> = emptyList(),
    val errorMessage: String? = null
)