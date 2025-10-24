package com.neverforget.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neverforget.data.model.*
import com.neverforget.data.repository.BackupRepository

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel pour la gestion des sauvegardes (export/import)
 */
@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupRepository: BackupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    /**
     * Exporte les données vers un fichier JSON et prépare le partage
     */
    fun exportData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, message = null, isError = false)
            
            backupRepository.exportToJson()
                .onSuccess { exportResult ->
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        exportResult = exportResult,
                        lastExportDate = "Aujourd'hui"
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        message = "Erreur lors de l'export: ${exception.message}",
                        isError = true
                    )
                }
        }
    }
    
    /**
     * Confirme que le partage a été effectué
     */
    fun onExportShared() {
        _uiState.value = _uiState.value.copy(
            exportResult = null,
            message = "Export partagé avec succès",
            isError = false
        )
    }

    /**
     * Importe les données depuis un fichier JSON (écrase tout)
     */
    fun importData(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true, message = null, isError = false)
            
            backupRepository.importFromJson(uri)
                .onSuccess { importResult ->
                    _uiState.value = _uiState.value.copy(
                        isImporting = false,
                        message = "Import réussi: ${importResult.importedTasks} tâches importées (toutes les données précédentes ont été écrasées)",
                        isError = false
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isImporting = false,
                        message = "Erreur lors de l'import: ${exception.message}",
                        isError = true
                    )
                }
        }
    }
}

/**
 * État de l'interface de sauvegarde
 */
data class BackupUiState(
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val lastExportDate: String? = null,
    val message: String? = null,
    val isError: Boolean = false,
    val exportResult: com.neverforget.data.model.ExportResult? = null
)