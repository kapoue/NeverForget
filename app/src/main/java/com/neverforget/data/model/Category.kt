package com.neverforget.data.model

/**
 * Modèle de domaine représentant une catégorie de tâche
 */
data class Category(
    val name: String,
    val icon: String,
    val isDeletable: Boolean = true
) {
    companion object {
        /**
         * Catégories par défaut de l'application
         */
        val DEFAULT_CATEGORIES = listOf(
            Category("Maison", "🏠", false), // Non supprimable
            Category("Voiture", "🚗", true),
            Category("Scooter", "🛵", true),
            Category("Autre", "⚙️", true)
        )
        
        /**
         * Trouve une catégorie par son nom ou retourne "Autre" par défaut
         */
        fun fromString(name: String): Category {
            return DEFAULT_CATEGORIES.find { it.name == name }
                ?: Category("Autre", "⚙️", true)
        }
    }
}