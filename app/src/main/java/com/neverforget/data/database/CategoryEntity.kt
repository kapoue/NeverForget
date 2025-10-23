package com.neverforget.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entité Room représentant une catégorie de tâche
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey 
    val name: String,
    val icon: String,
    val isDeletable: Boolean = true
)