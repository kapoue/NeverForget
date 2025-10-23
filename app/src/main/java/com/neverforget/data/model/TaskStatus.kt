package com.neverforget.data.model

/**
 * Énumération représentant le statut d'urgence d'une tâche
 */
enum class TaskStatus {
    /** Tâche OK, encore du temps avant l'échéance */
    OK,
    
    /** Tâche à faire aujourd'hui */
    DUE_TODAY,
    
    /** Tâche en retard */
    OVERDUE
}