package com.neverforget.data.repository

import com.neverforget.data.database.CategoryDao
import com.neverforget.data.database.TaskDao
import com.neverforget.data.model.Category
import com.neverforget.data.model.toCategory
import com.neverforget.data.model.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository pour la gestion des catégories
 */
@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao,
    private val taskDao: TaskDao
) {
    
    /**
     * Récupère toutes les catégories
     */
    fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories().map { entities ->
            entities.map { it.toCategory() }
        }
    }
    
    /**
     * Récupère une catégorie par son nom
     */
    suspend fun getCategoryByName(name: String): Category? {
        return categoryDao.getCategoryByName(name)?.toCategory()
    }
    
    /**
     * Ajoute une nouvelle catégorie
     */
    suspend fun addCategory(name: String, icon: String) {
        val category = Category(
            name = name,
            icon = icon,
            isDeletable = true
        )
        categoryDao.insertCategory(category.toEntity())
    }
    
    /**
     * Met à jour une catégorie
     */
    suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category.toEntity())
    }
    
    /**
     * Supprime une catégorie et migre les tâches vers "Maison"
     */
    suspend fun deleteCategory(categoryName: String) {
        // Vérifier que la catégorie est supprimable
        val category = categoryDao.getCategoryByName(categoryName)
        if (category?.isDeletable == true) {
            // Migrer les tâches vers "Maison"
            taskDao.updateTasksCategory(categoryName, "Maison")
            
            // Supprimer la catégorie
            categoryDao.deleteCategoryByName(categoryName)
        }
    }
    
    /**
     * Récupère le nombre de catégories
     */
    suspend fun getCategoryCount(): Int {
        return categoryDao.getCategoryCount()
    }
}