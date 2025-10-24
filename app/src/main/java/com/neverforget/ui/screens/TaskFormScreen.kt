package com.neverforget.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neverforget.R
import com.neverforget.data.model.Category
import com.neverforget.ui.components.CategoryDropdown
import com.neverforget.ui.viewmodel.TaskFormViewModel
import kotlinx.datetime.LocalDate
import com.neverforget.utils.DateUtils

/**
 * Écran de création/modification d'une tâche
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFormScreen(
    taskId: String? = null,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TaskFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isEditing = taskId != null
    
    LaunchedEffect(taskId) {
        if (taskId != null) {
            viewModel.loadTask(taskId)
        }
    }
    
    // Observer les événements de navigation
    LaunchedEffect(uiState.isTaskSaved) {
        if (uiState.isTaskSaved) {
            onNavigateBack()
        }
    }
    
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) {
                            stringResource(R.string.edit_task)
                        } else {
                            stringResource(R.string.add_task)
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                else -> {
                    TaskFormContent(
                        uiState = uiState,
                        onNameChange = viewModel::updateName,
                        onCategoryChange = { category -> viewModel.updateCategory(category.name) },
                        onRecurrenceChange = viewModel::updateRecurrence,
                        onSave = viewModel::saveTask,
                        isEditing = isEditing
                    )
                }
            }
        }
    }
}

/**
 * Contenu principal du formulaire
 */
@Composable
private fun TaskFormContent(
    uiState: com.neverforget.ui.viewmodel.TaskFormUiState,
    onNameChange: (String) -> Unit,
    onCategoryChange: (Category) -> Unit,
    onRecurrenceChange: (Int) -> Unit,
    onSave: () -> Unit,
    isEditing: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Nom de la tâche
        OutlinedTextField(
            value = uiState.name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.task_name)) },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.nameError != null,
            supportingText = uiState.nameError?.let { { Text(it) } }
        )
        
        // Catégorie
        CategoryDropdown(
            selectedCategory = Category.fromString(uiState.category),
            onCategorySelected = onCategoryChange,
            label = stringResource(R.string.category)
        )
        
        // Récurrence
        RecurrenceField(
            recurrenceDays = uiState.recurrenceDays,
            onRecurrenceChange = onRecurrenceChange,
            error = uiState.recurrenceError
        )
        
        // Note: La prochaine échéance est calculée automatiquement
        // à partir de la récurrence (aujourd'hui + récurrence)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Bouton de sauvegarde
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.canSave && !uiState.isSaving,
            contentPadding = PaddingValues(16.dp)
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = if (isEditing) {
                    stringResource(R.string.save_changes)
                } else {
                    stringResource(R.string.create_task)
                }
            )
        }
        
        // Message d'erreur général
        uiState.error?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}


/**
 * Champ de saisie pour la récurrence
 */
@Composable
private fun RecurrenceField(
    recurrenceDays: Int,
    onRecurrenceChange: (Int) -> Unit,
    error: String?,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = if (recurrenceDays > 0) recurrenceDays.toString() else "",
        onValueChange = { value ->
            val days = value.toIntOrNull()
            if (days != null && days > 0) {
                onRecurrenceChange(days)
            } else if (value.isEmpty()) {
                onRecurrenceChange(0)
            }
        },
        label = { Text(stringResource(R.string.recurrence_label)) },
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        suffix = { Text(stringResource(R.string.days)) }
    )
}

// Fonctions de calendrier supprimées car l'échéance est maintenant calculée automatiquement