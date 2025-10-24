package com.neverforget.utils

import kotlinx.datetime.*

/**
 * Utilitaires pour la gestion des dates dans l'application
 */
object DateUtils {
    
    /**
     * Obtient la date d'aujourd'hui
     */
    fun today(): LocalDate {
        return Clock.System.todayIn(TimeZone.currentSystemDefault())
    }
    
    /**
     * Calcule le nombre de jours entre deux dates
     */
    fun daysBetween(startDate: LocalDate, endDate: LocalDate): Int {
        return endDate.toEpochDays() - startDate.toEpochDays()
    }
    
    /**
     * Ajoute des jours à une date
     */
    fun addDays(date: LocalDate, days: Int): LocalDate {
        return date.plus(days, DateTimeUnit.DAY)
    }
    
    /**
     * Ajoute des semaines à une date
     */
    fun addWeeks(date: LocalDate, weeks: Int): LocalDate {
        return date.plus(weeks * 7, DateTimeUnit.DAY)
    }
    
    /**
     * Ajoute des mois à une date
     */
    fun addMonths(date: LocalDate, months: Int): LocalDate {
        return date.plus(months, DateTimeUnit.MONTH)
    }
    
    /**
     * Vérifie si une date est dans le passé
     */
    fun isPast(date: LocalDate): Boolean {
        return date < today()
    }
    
    /**
     * Vérifie si une date est aujourd'hui
     */
    fun isToday(date: LocalDate): Boolean {
        return date == today()
    }
    
    /**
     * Vérifie si une date est dans le futur
     */
    fun isFuture(date: LocalDate): Boolean {
        return date > today()
    }
    
    /**
     * Formate une date en français
     */
    fun formatFrench(date: LocalDate): String {
        return "${date.dayOfMonth.toString().padStart(2, '0')}/${date.monthNumber.toString().padStart(2, '0')}/${date.year}"
    }
    
    /**
     * Formate une date en français court
     */
    fun formatFrenchShort(date: LocalDate): String {
        val shortYear = date.year.toString().takeLast(2)
        return "${date.dayOfMonth.toString().padStart(2, '0')}/${date.monthNumber.toString().padStart(2, '0')}/$shortYear"
    }
    
    /**
     * Calcule la prochaine échéance basée sur une date de validation et une récurrence
     */
    fun calculateNextDueDate(completionDate: LocalDate, recurrenceDays: Int): LocalDate {
        return completionDate.plus(recurrenceDays, DateTimeUnit.DAY)
    }
    
    /**
     * Calcule le statut d'urgence d'une tâche
     */
    fun calculateTaskUrgency(dueDate: LocalDate, today: LocalDate = today()): TaskUrgency {
        return when {
            dueDate < today -> TaskUrgency.OVERDUE
            dueDate == today -> TaskUrgency.DUE_TODAY
            daysBetween(today, dueDate) <= 3 -> TaskUrgency.DUE_SOON
            daysBetween(today, dueDate) <= 7 -> TaskUrgency.DUE_THIS_WEEK
            else -> TaskUrgency.OK
        }
    }
    
    /**
     * Convertit des jours en description lisible
     */
    fun formatRecurrenceDescription(days: Int): String {
        return when {
            days == 1 -> "Tous les jours"
            days == 7 -> "Toutes les semaines"
            days == 14 -> "Toutes les 2 semaines"
            days == 30 -> "Tous les mois"
            days == 60 -> "Tous les 2 mois"
            days == 90 -> "Tous les 3 mois"
            days == 180 -> "Tous les 6 mois"
            days == 365 -> "Tous les ans"
            days % 365 == 0 -> "Tous les ${days / 365} ans"
            days % 30 == 0 -> "Tous les ${days / 30} mois"
            days % 7 == 0 -> "Toutes les ${days / 7} semaines"
            else -> "Tous les $days jours"
        }
    }
    
    /**
     * Obtient le début de la semaine (lundi)
     */
    fun startOfWeek(date: LocalDate = today()): LocalDate {
        val dayOfWeek = date.dayOfWeek.ordinal // 0 = Monday
        return date.minus(dayOfWeek, DateTimeUnit.DAY)
    }
    
    /**
     * Obtient le début du mois
     */
    fun startOfMonth(date: LocalDate = today()): LocalDate {
        return LocalDate(date.year, date.month, 1)
    }
    
    /**
     * Obtient la fin du mois
     */
    fun endOfMonth(date: LocalDate = today()): LocalDate {
        return LocalDate(date.year, date.month, date.month.length(date.year % 4 == 0))
    }
    
    /**
     * Formate une date au format ISO (YYYY-MM-DD) pour l'export JSON
     */
    fun formatDate(date: LocalDate): String {
        return date.toString()
    }
    
    /**
     * Parse une date depuis le format ISO (YYYY-MM-DD)
     */
    fun parseDate(dateString: String): LocalDate {
        return LocalDate.parse(dateString)
    }
    
    /**
     * Convertit un timestamp en LocalDate
     */
    fun timestampToLocalDate(timestamp: Long): LocalDate {
        return Instant.fromEpochMilliseconds(timestamp)
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
    }
    
    /**
     * Convertit une LocalDate en timestamp
     */
    fun localDateToTimestamp(date: LocalDate): Long {
        return date.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    }
}

/**
 * Énumération pour l'urgence des tâches
 */
enum class TaskUrgency(val priority: Int) {
    OVERDUE(0),      // En retard
    DUE_TODAY(1),    // À faire aujourd'hui
    DUE_SOON(2),     // À faire dans les 3 jours
    DUE_THIS_WEEK(3), // À faire cette semaine
    OK(4)            // Pas urgent
}