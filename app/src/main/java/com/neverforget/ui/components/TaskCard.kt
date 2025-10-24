package com.neverforget.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neverforget.R
import com.neverforget.data.model.Category
import com.neverforget.data.model.Task
import com.neverforget.data.model.TaskStatus
import com.neverforget.ui.theme.TaskStatusOk
import com.neverforget.ui.theme.TaskStatusDueToday
import com.neverforget.ui.theme.TaskStatusOverdue
import com.neverforget.utils.CategoryHelper

/**
 * Composant Card pour afficher une tâche dans la liste
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    task: Task,
    onTaskClick: (String) -> Unit,
    onCompleteClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { onTaskClick(task.id) },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icône de catégorie
            CategoryIcon(
                category = Category.fromString(task.category),
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Contenu principal
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Nom de la tâche
                Text(
                    text = task.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Badge de statut
                StatusBadge(
                    status = task.status,
                    daysUntilDue = task.daysUntilDue
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Bouton de validation
            FilledTonalIconButton(
                onClick = { onCompleteClick(task.id) },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.mark_as_done),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

/**
 * Badge de statut avec couleur selon l'urgence
 */
@Composable
fun StatusBadge(
    status: TaskStatus,
    daysUntilDue: Int,
    modifier: Modifier = Modifier
) {
    val (text, backgroundColor) = when (status) {
        TaskStatus.OVERDUE -> "A faire" to TaskStatusOverdue
        TaskStatus.DUE_TODAY -> "À faire aujourd'hui" to TaskStatusDueToday
        TaskStatus.OK -> "Dans $daysUntilDue jours" to TaskStatusOk
    }
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = backgroundColor,
            fontSize = 11.sp
        )
    }
}

/**
 * Icône de catégorie
 */
@Composable
fun CategoryIcon(
    category: Category,
    modifier: Modifier = Modifier
) {
    val icon = CategoryHelper.getCategoryVectorIcon(category)
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = CategoryHelper.getCategoryDisplayName(category),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}