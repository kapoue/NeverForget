package com.neverforget.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neverforget.R
import com.neverforget.ui.components.CategoryIcon
import com.neverforget.ui.components.CategoryIndicator
import com.neverforget.ui.components.StatusBadge
import com.neverforget.ui.viewmodel.TaskDetailViewModel
import com.neverforget.utils.CategoryHelper
import kotlinx.datetime.LocalDate
import com.neverforget.utils.DateUtils

/**
 * Écran de détail d'une tâche avec historique
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TaskDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(taskId) {
        viewModel.loadTask(taskId)
    }
    
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = uiState.task?.name ?: stringResource(R.string.task_detail))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    if (uiState.task != null) {
                        IconButton(onClick = { onNavigateToEdit(taskId) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.edit_task)
                            )
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete_task),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
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
                
                uiState.error != null -> {
                    val error = uiState.error!!
                    ErrorMessage(
                        message = error,
                        onRetry = { viewModel.loadTask(taskId) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                uiState.task != null -> {
                    val task = uiState.task!!
                    TaskDetailContent(
                        task = task,
                        history = uiState.history,
                        onCompleteTask = {
                            viewModel.completeTask()
                        }
                    )
                }
            }
        }
    }
    
    // Dialogue de confirmation de suppression
    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            taskName = uiState.task?.name ?: "",
            onConfirm = {
                viewModel.deleteTask()
                showDeleteDialog = false
                onNavigateBack()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

/**
 * Contenu principal de l'écran de détail
 */
@Composable
private fun TaskDetailContent(
    task: com.neverforget.data.model.Task,
    history: List<LocalDate>,
    onCompleteTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // En-tête avec icône et nom
        item {
            TaskHeader(task = task)
        }
        
        // Informations de la tâche
        item {
            TaskInfoCard(task = task)
        }
        
        // Bouton de validation
        item {
            Button(
                onClick = onCompleteTask,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.mark_as_done),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        
        // Section historique
        item {
            Text(
                text = stringResource(R.string.history),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        if (history.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.no_history),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            items(history) { date ->
                HistoryItem(date = date)
            }
        }
    }
}

/**
 * En-tête avec icône et nom de la tâche
 */
@Composable
private fun TaskHeader(
    task: com.neverforget.data.model.Task,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryIcon(
            category = com.neverforget.data.model.Category.fromString(task.category),
            modifier = Modifier.size(48.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            StatusBadge(
                status = task.status,
                daysUntilDue = task.daysUntilDue
            )
        }
    }
}

/**
 * Card avec les informations de la tâche
 */
@Composable
private fun TaskInfoCard(
    task: com.neverforget.data.model.Task,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CategoryInfoRow(
                label = stringResource(R.string.category),
                category = com.neverforget.data.model.Category.fromString(task.category)
            )
            
            InfoRow(
                label = stringResource(R.string.recurrence),
                value = stringResource(R.string.every_x_days, task.recurrenceDays)
            )
            
            InfoRow(
                label = stringResource(R.string.next_due_date),
                value = DateUtils.formatDate(task.nextDueDate)
            )
        }
    }
}

/**
 * Ligne d'information avec label et valeur
 */
@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Ligne d'information avec catégorie
 */
@Composable
private fun CategoryInfoRow(
    label: String,
    category: com.neverforget.data.model.Category,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        CategoryIndicator(
            category = category,
            showLabel = true
        )
    }
}

/**
 * Item d'historique
 */
@Composable
private fun HistoryItem(
    date: LocalDate,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(
            text = DateUtils.formatDate(date),
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Message d'erreur avec bouton de retry
 */
@Composable
private fun ErrorMessage(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = onRetry) {
            Text(text = stringResource(R.string.retry))
        }
    }
}

/**
 * Dialogue de confirmation de suppression
 */
@Composable
private fun DeleteConfirmationDialog(
    taskName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.delete_task_title))
        },
        text = {
            Text(text = stringResource(R.string.delete_task_message, taskName))
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.delete),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}