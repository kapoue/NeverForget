package com.neverforget.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.neverforget.R
import com.neverforget.ui.screens.*
import kotlinx.coroutines.launch

/**
 * Navigation principale de l'application NeverForget
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeverForgetNavigation(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavigationDrawerContent(
                navController = navController,
                onCloseDrawer = {
                    scope.launch {
                        drawerState.close()
                    }
                }
            )
        },
        modifier = modifier
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.TaskList.route
        ) {
            composable(Screen.TaskList.route) {
                TaskListScreen(
                    onNavigateToTaskDetail = { taskId ->
                        navController.navigate(Screen.TaskDetail.createRoute(taskId))
                    },
                    onNavigateToTaskForm = {
                        navController.navigate(Screen.TaskForm.route)
                    },
                    onOpenDrawer = {
                        scope.launch {
                            drawerState.open()
                        }
                    }
                )
            }
            
            composable(Screen.TaskDetail.route) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
                TaskDetailScreen(
                    taskId = taskId,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToEdit = { taskId ->
                        navController.navigate(Screen.TaskForm.createRoute(taskId))
                    }
                )
            }
            
            composable(Screen.TaskForm.route) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId")
                TaskFormScreen(
                    taskId = taskId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToBackup = {
                        navController.navigate(Screen.Backup.route)
                    }
                )
            }
            
            composable(Screen.Backup.route) {
                BackupScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(Screen.About.route) {
                AboutScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

/**
 * Contenu du drawer de navigation
 */
@Composable
private fun NavigationDrawerContent(
    navController: NavHostController,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    ModalDrawerSheet(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp)
        ) {
            // En-tête du drawer
            DrawerHeader()
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Items de navigation
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.List, contentDescription = null) },
                label = { Text(stringResource(R.string.tasks)) },
                selected = currentDestination?.hierarchy?.any { it.route == Screen.TaskList.route } == true,
                onClick = {
                    navController.navigate(Screen.TaskList.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                    onCloseDrawer()
                },
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                label = { Text(stringResource(R.string.settings)) },
                selected = currentDestination?.hierarchy?.any { it.route == Screen.Settings.route } == true,
                onClick = {
                    navController.navigate(Screen.Settings.route)
                    onCloseDrawer()
                },
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Backup, contentDescription = null) },
                label = { Text(stringResource(R.string.backup)) },
                selected = currentDestination?.hierarchy?.any { it.route == Screen.Backup.route } == true,
                onClick = {
                    navController.navigate(Screen.Backup.route)
                    onCloseDrawer()
                },
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Info, contentDescription = null) },
                label = { Text(stringResource(R.string.about)) },
                selected = currentDestination?.hierarchy?.any { it.route == Screen.About.route } == true,
                onClick = {
                    navController.navigate(Screen.About.route)
                    onCloseDrawer()
                },
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}

/**
 * En-tête du drawer de navigation
 */
@Composable
private fun DrawerHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(R.string.app_tagline),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Définition des écrans de navigation
 */
sealed class Screen(val route: String) {
    object TaskList : Screen("task_list")
    object TaskDetail : Screen("task_detail/{taskId}") {
        fun createRoute(taskId: String) = "task_detail/$taskId"
    }
    object TaskForm : Screen("task_form?taskId={taskId}") {
        fun createRoute(taskId: String? = null) = if (taskId != null) {
            "task_form?taskId=$taskId"
        } else {
            "task_form"
        }
    }
    object Settings : Screen("settings")
    object Backup : Screen("backup")
    object About : Screen("about")
}