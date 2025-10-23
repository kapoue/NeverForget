package com.neverforget.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun NeverForgetNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "task_list"
    ) {
        composable("task_list") {
            // TaskListScreen(navController) - À implémenter en Phase 4
        }
        
        composable("task_detail/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            // TaskDetailScreen(navController, taskId) - À implémenter en Phase 4
        }
        
        composable("task_form") {
            // TaskFormScreen(navController) - À implémenter en Phase 4
        }
        
        composable("task_form/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            // TaskFormScreen(navController, taskId) - À implémenter en Phase 4
        }
        
        composable("settings") {
            // SettingsScreen(navController) - À implémenter en Phase 4
        }
        
        composable("backup") {
            // BackupScreen(navController) - À implémenter en Phase 4
        }
        
        composable("about") {
            // AboutScreen(navController) - À implémenter en Phase 4
        }
    }
}