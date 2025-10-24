package com.neverforget.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.neverforget.R

/**
 * Écran À propos de l'application
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.about))
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo et nom de l'app
            AppHeader()
            
            // Informations de l'application
            AboutSection(
                title = stringResource(R.string.app_info),
                icon = Icons.Default.Info
            ) {
                AppInfoContent()
            }
            
            // Informations techniques
            AboutSection(
                title = stringResource(R.string.technical_info),
                icon = Icons.Default.Code
            ) {
                TechnicalInfoContent()
            }
            
            // Informations de version
            AboutSection(
                title = stringResource(R.string.version_info),
                icon = Icons.Default.Update
            ) {
                VersionInfoContent()
            }
        }
    }
}

/**
 * En-tête avec logo et nom de l'application
 */
@Composable
private fun AppHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Logo de l'application (placeholder)
        Surface(
            modifier = Modifier.size(80.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🧠",
                    style = MaterialTheme.typography.headlineLarge
                )
            }
        }
        
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = stringResource(R.string.app_tagline),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Section À propos avec titre et icône
 */
@Composable
private fun AboutSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            content()
        }
    }
}

/**
 * Contenu des informations de l'application
 */
@Composable
private fun AppInfoContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.app_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        InfoRow(
            label = stringResource(R.string.developer),
            value = stringResource(R.string.developer_name)
        )
        
        InfoRow(
            label = stringResource(R.string.platform),
            value = stringResource(R.string.android_only)
        )
        
        InfoRow(
            label = stringResource(R.string.language),
            value = stringResource(R.string.french)
        )
        
        InfoRow(
            label = stringResource(R.string.license),
            value = stringResource(R.string.open_source)
        )
    }
}

/**
 * Contenu des informations techniques
 */
@Composable
private fun TechnicalInfoContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        InfoRow(
            label = stringResource(R.string.framework),
            value = "Jetpack Compose"
        )
        
        InfoRow(
            label = stringResource(R.string.architecture),
            value = "MVVM + Repository"
        )
        
        InfoRow(
            label = stringResource(R.string.database),
            value = "Room (SQLite)"
        )
        
        InfoRow(
            label = stringResource(R.string.notifications),
            value = "WorkManager"
        )
        
        InfoRow(
            label = stringResource(R.string.dependency_injection),
            value = "Hilt (Dagger)"
        )
    }
}

/**
 * Contenu des informations de version
 */
@Composable
private fun VersionInfoContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        InfoRow(
            label = stringResource(R.string.version),
            value = "1.0.0"
        )
        
        InfoRow(
            label = stringResource(R.string.build_date),
            value = "Octobre 2025"
        )
        
        InfoRow(
            label = stringResource(R.string.min_android_version),
            value = "Android 7.0 (API 24)"
        )
        
        InfoRow(
            label = stringResource(R.string.target_android_version),
            value = "Android 14 (API 34)"
        )
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}