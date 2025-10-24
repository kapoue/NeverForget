package com.neverforget.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.neverforget.data.model.Category
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.*

/**
 * Base de données Room principale de l'application
 */
@Database(
    entities = [
        TaskEntity::class,
        TaskHistoryEntity::class,
        CategoryEntity::class,
        SettingsEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class NeverForgetDatabase : RoomDatabase() {
    
    abstract fun taskDao(): TaskDao
    abstract fun taskHistoryDao(): TaskHistoryDao
    abstract fun categoryDao(): CategoryDao
    abstract fun settingsDao(): SettingsDao
    
    companion object {
        @Volatile
        private var INSTANCE: NeverForgetDatabase? = null
        
        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): NeverForgetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NeverForgetDatabase::class.java,
                    "neverforget_database"
                )
                .addCallback(DatabaseCallback(scope))
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
        
        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database)
                    }
                }
            }
        }
        
        /**
         * Peuple la base de données avec les données par défaut
         */
        private suspend fun populateDatabase(database: NeverForgetDatabase) {
            val categoryDao = database.categoryDao()
            val taskDao = database.taskDao()
            val settingsDao = database.settingsDao()
            
            // Insérer les catégories par défaut
            val defaultCategories = Category.DEFAULT_CATEGORIES.map { category ->
                CategoryEntity(
                    name = category.name,
                    icon = category.icon,
                    isDeletable = category.isDeletable
                )
            }
            categoryDao.insertCategories(defaultCategories)
            
            // Insérer les paramètres par défaut
            val defaultSettings = listOf(
                SettingsEntity(
                    key = SettingsEntity.NOTIFICATION_TIME_KEY,
                    value = SettingsEntity.DEFAULT_NOTIFICATION_TIME
                )
            )
            settingsDao.insertSettings(defaultSettings)
            
            // Insérer les 6 tâches par défaut
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val defaultTasks = listOf(
                TaskEntity(
                    name = "Détecteurs de fumée",
                    category = "Maison",
                    recurrenceDays = 180, // 6 mois
                    nextDueDate = today.plus(180, DateTimeUnit.DAY),
                    createdAt = today
                ),
                TaskEntity(
                    name = "Ventilation salle de bain",
                    category = "Maison",
                    recurrenceDays = 90, // 3 mois
                    nextDueDate = today.plus(90, DateTimeUnit.DAY),
                    createdAt = today
                ),
                TaskEntity(
                    name = "Filtres aspirateur",
                    category = "Maison",
                    recurrenceDays = 60, // 2 mois
                    nextDueDate = today.plus(60, DateTimeUnit.DAY),
                    createdAt = today
                ),
                TaskEntity(
                    name = "Pression pneus voiture",
                    category = "Voiture",
                    recurrenceDays = 30, // 1 mois
                    nextDueDate = today.plus(30, DateTimeUnit.DAY),
                    createdAt = today
                ),
                TaskEntity(
                    name = "Niveau lave-glace",
                    category = "Voiture",
                    recurrenceDays = 180, // 6 mois
                    nextDueDate = today.plus(180, DateTimeUnit.DAY),
                    createdAt = today
                ),
                TaskEntity(
                    name = "Pression pneus scooter",
                    category = "Scooter",
                    recurrenceDays = 30, // 1 mois
                    nextDueDate = today.plus(30, DateTimeUnit.DAY),
                    createdAt = today
                )
            )
            taskDao.insertTasks(defaultTasks)
        }
    }
}