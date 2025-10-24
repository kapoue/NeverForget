package com.neverforget.utils

import android.util.Log
import com.neverforget.BuildConfig

/**
 * Système de logs centralisé pour NeverForget
 * Les logs sont activés uniquement en mode debug
 */
object Logger {
    
    private const val TAG = "NeverForget"
    private val isDebug = BuildConfig.DEBUG
    
    /**
     * Log de debug
     */
    fun d(message: String, tag: String = TAG) {
        if (isDebug) {
            Log.d(tag, message)
        }
    }
    
    /**
     * Log d'information
     */
    fun i(message: String, tag: String = TAG) {
        if (isDebug) {
            Log.i(tag, message)
        }
    }
    
    /**
     * Log d'avertissement
     */
    fun w(message: String, tag: String = TAG) {
        if (isDebug) {
            Log.w(tag, message)
        }
    }
    
    /**
     * Log d'erreur
     */
    fun e(message: String, throwable: Throwable? = null, tag: String = TAG) {
        if (isDebug) {
            if (throwable != null) {
                Log.e(tag, message, throwable)
            } else {
                Log.e(tag, message)
            }
        }
    }
    
    /**
     * Log pour les opérations de base de données
     */
    fun database(operation: String, details: String = "") {
        d("DB: $operation${if (details.isNotEmpty()) " - $details" else ""}", "NeverForget-DB")
    }
    
    /**
     * Log pour les notifications
     */
    fun notification(action: String, taskId: String? = null, details: String = "") {
        val taskInfo = taskId?.let { " [Task: $it]" } ?: ""
        d("NOTIF: $action$taskInfo${if (details.isNotEmpty()) " - $details" else ""}", "NeverForget-Notif")
    }
    
    /**
     * Log pour la navigation
     */
    fun navigation(from: String, to: String, params: String = "") {
        d("NAV: $from -> $to${if (params.isNotEmpty()) " ($params)" else ""}", "NeverForget-Nav")
    }
    
    /**
     * Log pour les Use Cases
     */
    fun useCase(useCaseName: String, action: String, details: String = "") {
        d("UC: $useCaseName.$action${if (details.isNotEmpty()) " - $details" else ""}", "NeverForget-UC")
    }
    
    /**
     * Log pour les ViewModels
     */
    fun viewModel(viewModelName: String, action: String, state: String = "") {
        d("VM: $viewModelName.$action${if (state.isNotEmpty()) " -> $state" else ""}", "NeverForget-VM")
    }
    
    /**
     * Log pour les erreurs critiques (toujours affiché même en release)
     */
    fun critical(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e("$TAG-CRITICAL", message, throwable)
        } else {
            Log.e("$TAG-CRITICAL", message)
        }
    }
}