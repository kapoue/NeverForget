package com.neverforget.utils

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Build
import androidx.compose.ui.graphics.vector.ImageVector
import com.neverforget.R
import com.neverforget.data.model.Category

/**
 * Helper pour la gestion des catégories et leurs icônes
 */
object CategoryHelper {
    
    /**
     * Obtient l'icône drawable pour une catégorie
     */
    @DrawableRes
    fun getCategoryIcon(category: Category): Int {
        return when (category.name) {
            "Maison" -> R.drawable.ic_home
            "Voiture" -> R.drawable.ic_car
            "Scooter" -> R.drawable.ic_scooter
            else -> R.drawable.ic_other
        }
    }
    
    /**
     * Obtient l'icône vectorielle pour une catégorie (pour Compose)
     */
    fun getCategoryVectorIcon(category: Category): ImageVector {
        return when (category.name) {
            "Maison" -> Icons.Default.Home
            "Voiture" -> Icons.Default.Build // Utiliser Build temporairement
            "Scooter" -> Icons.Default.Info
            else -> Icons.Default.Info
        }
    }
    
    /**
     * Obtient l'ID de la string pour une catégorie
     */
    @StringRes
    fun getCategoryStringRes(category: Category): Int {
        return when (category.name) {
            "Maison" -> R.string.category_home
            "Voiture" -> R.string.category_car
            "Scooter" -> R.string.category_scooter
            else -> R.string.category_other
        }
    }
    
    /**
     * Obtient le nom localisé d'une catégorie
     */
    fun getCategoryDisplayName(category: Category, context: Context): String {
        return context.getString(getCategoryStringRes(category))
    }
    
    /**
     * Obtient le nom localisé d'une catégorie (version simple pour Compose)
     */
    fun getCategoryDisplayName(category: Category): String {
        return category.name
    }
    
    /**
     * Obtient toutes les catégories disponibles
     */
    fun getAllCategories(): List<Category> {
        return Category.DEFAULT_CATEGORIES
    }
    
    /**
     * Parse une chaîne en catégorie
     */
    fun parseCategory(categoryString: String?): Category {
        return Category.DEFAULT_CATEGORIES.find { it.name.equals(categoryString, ignoreCase = true) }
            ?: Category.DEFAULT_CATEGORIES.last() // "Autre" par défaut
    }
}