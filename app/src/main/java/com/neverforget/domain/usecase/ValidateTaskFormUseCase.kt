package com.neverforget.domain.usecase

import kotlinx.datetime.LocalDate
import javax.inject.Inject

/**
 * Use case pour valider les données d'un formulaire de tâche
 */
class ValidateTaskFormUseCase @Inject constructor() {
    
    /**
     * Valide les données d'une tâche
     */
    fun execute(
        name: String,
        category: String,
        recurrenceDays: Int,
        nextDueDate: LocalDate?
    ): ValidationResult {
        val errors = mutableMapOf<String, String>()
        
        // Validation du nom
        when {
            name.isBlank() -> errors["name"] = "Le nom de la tâche est obligatoire"
            name.length > 100 -> errors["name"] = "Le nom ne peut pas dépasser 100 caractères"
        }
        
        // Validation de la catégorie
        if (category.isBlank()) {
            errors["category"] = "La catégorie est obligatoire"
        }
        
        // Validation de la récurrence
        when {
            recurrenceDays <= 0 -> errors["recurrence"] = "La récurrence doit être supérieure à 0"
            recurrenceDays > 3650 -> errors["recurrence"] = "La récurrence ne peut pas dépasser 10 ans"
        }
        
        // Validation de la date d'échéance (seulement pour la création)
        nextDueDate?.let { _ ->
            // Pas de validation stricte sur la date, on permet les dates passées
            // pour permettre de créer des tâches avec un historique
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
    
    /**
     * Valide spécifiquement le nom d'une tâche
     */
    fun validateName(name: String): String? {
        return when {
            name.isBlank() -> "Le nom de la tâche est obligatoire"
            name.length > 100 -> "Le nom ne peut pas dépasser 100 caractères"
            else -> null
        }
    }
    
    /**
     * Valide spécifiquement la récurrence
     */
    fun validateRecurrence(recurrenceDays: Int): String? {
        return when {
            recurrenceDays <= 0 -> "La récurrence doit être supérieure à 0"
            recurrenceDays > 3650 -> "La récurrence ne peut pas dépasser 10 ans"
            else -> null
        }
    }
    
    /**
     * Suggère des récurrences courantes selon la catégorie
     */
    fun suggestRecurrence(category: String): Int {
        return when (category.lowercase()) {
            "maison" -> 90 // 3 mois par défaut
            "voiture" -> 30 // 1 mois par défaut
            "scooter" -> 30 // 1 mois par défaut
            else -> 60 // 2 mois par défaut
        }
    }
}

/**
 * Résultat de validation
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: Map<String, String>
) {
    fun getError(field: String): String? = errors[field]
    
    val hasNameError: Boolean get() = errors.containsKey("name")
    val hasCategoryError: Boolean get() = errors.containsKey("category")
    val hasRecurrenceError: Boolean get() = errors.containsKey("recurrence")
    val hasDateError: Boolean get() = errors.containsKey("date")
}